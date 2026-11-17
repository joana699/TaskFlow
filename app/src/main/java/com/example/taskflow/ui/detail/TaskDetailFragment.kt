package com.example.taskflow.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taskflow.data.local.TaskEntity
import com.example.taskflow.data.local.TaskState
import com.example.taskflow.databinding.FragmentTaskDetailBinding
import com.example.taskflow.viewmodel.TaskViewModel
import com.example.taskflow.worker.AutomationRule
import com.example.taskflow.worker.TaskScheduler
import kotlinx.coroutines.launch

/** Crear (taskId = -1) o editar una tarea. */
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModel.Factory(requireContext())
    }

    private var editingTask: TaskEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = arguments?.getInt("taskId", -1) ?: -1
        if (taskId != -1) loadTask(taskId)

        val refresh = { updateRuleSummary() }
        binding.switchNetwork.setOnCheckedChangeListener { _, _ -> refresh() }
        binding.switchCharging.setOnCheckedChangeListener { _, _ -> refresh() }
        binding.switchBatteryNotLow.setOnCheckedChangeListener { _, _ -> refresh() }
        updateRuleSummary()

        binding.buttonSave.setOnClickListener { save() }
    }

    private fun loadTask(id: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val task = viewModel.getTask(id) ?: return@launch
            editingTask = task
            binding.editTitle.setText(task.title)
            binding.editDescription.setText(task.description)
            binding.editInterval.setText(task.repeatIntervalMinutes.toString())
            binding.switchOneTime.isChecked = task.oneTime
            binding.switchNetwork.isChecked = task.requiresNetwork
            binding.switchCharging.isChecked = task.requiresCharging
            binding.switchBatteryNotLow.isChecked = task.requiresBatteryNotLow
            binding.buttonSave.text = "Guardar cambios"
            updateRuleSummary()
        }
    }

    private fun currentRule() = AutomationRule(
        requiresInternet = binding.switchNetwork.isChecked,
        batteryNotLow = binding.switchBatteryNotLow.isChecked,
        requiresCharging = binding.switchCharging.isChecked
    )

    private fun updateRuleSummary() {
        binding.textViewRuleSummary.text = currentRule().describe()
    }

    private fun save() {
        val title = binding.editTitle.text?.toString()?.trim().orEmpty()
        val description = binding.editDescription.text?.toString()?.trim().orEmpty()
        val interval = binding.editInterval.text?.toString()?.trim()?.toLongOrNull()
        val oneTime = binding.switchOneTime.isChecked

        binding.inputLayoutTitle.error = null
        binding.inputLayoutInterval.error = null

        if (title.isEmpty()) {
            binding.inputLayoutTitle.error = "El nombre es obligatorio"
            return
        }
        val minimum = if (oneTime) 1L else TaskScheduler.MIN_PERIODIC_MINUTES
        if (interval == null || interval < minimum) {
            binding.inputLayoutInterval.error = "Mínimo $minimum minuto(s)"
            return
        }

        val rule = currentRule()
        val base = editingTask
        val task = if (base != null) {
            // Al editar se conserva "enabled"; el estado se recalcula (sale de ERROR/COMPLETADA)
            base.copy(
                title = title,
                description = description,
                repeatIntervalMinutes = interval,
                oneTime = oneTime,
                requiresNetwork = rule.requiresInternet,
                requiresCharging = rule.requiresCharging,
                requiresBatteryNotLow = rule.batteryNotLow,
                state = if (base.enabled) TaskState.ACTIVA else TaskState.PAUSADA
            )
        } else {
            TaskEntity(
                title = title,
                description = description,
                repeatIntervalMinutes = interval,
                oneTime = oneTime,
                requiresNetwork = rule.requiresInternet,
                requiresCharging = rule.requiresCharging,
                requiresBatteryNotLow = rule.batteryNotLow
            )
        }

        viewModel.save(task)
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
