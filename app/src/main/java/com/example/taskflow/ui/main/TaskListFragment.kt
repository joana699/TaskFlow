package com.example.taskflow.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskflow.R
import com.example.taskflow.databinding.FragmentTaskListBinding
import com.example.taskflow.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModel.Factory(requireContext())
    }
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeTasks()
        setupListeners()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onSwitchChanged = { task, enabled -> viewModel.setEnabled(task, enabled) },
            onDeleteClicked = { task -> viewModel.delete(task) },
            onItemClicked = { task -> openDetail(task.id) }
        )
        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    taskAdapter.submitList(tasks)
                    binding.textViewTaskCount.text = "Tareas: ${tasks.size}"
                    binding.textViewEmpty.visibility =
                        if (tasks.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener { openDetail(-1) }
        binding.buttonHistory.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }
    }

    private fun openDetail(taskId: Int) {
        findNavController().navigate(R.id.taskDetailFragment, bundleOf("taskId" to taskId))
    }

    override fun onDestroyView() {
        binding.recyclerViewTasks.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
