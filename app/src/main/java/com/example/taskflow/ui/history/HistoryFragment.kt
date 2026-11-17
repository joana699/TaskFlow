package com.example.taskflow.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflow.data.local.ExecutionItem
import com.example.taskflow.databinding.FragmentHistoryBinding
import com.example.taskflow.databinding.ItemExecutionBinding
import com.example.taskflow.ui.UiFormat
import com.example.taskflow.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

/** Reto 3: pantalla "Historial" con las ejecuciones del Worker. */
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModel.Factory(requireContext())
    }
    private val adapter = ExecutionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = adapter
        binding.buttonClearHistory.setOnClickListener { viewModel.clearHistory() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.history.collect { items ->
                    adapter.submitList(items)
                    binding.textViewHistoryEmpty.visibility =
                        if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.recyclerViewHistory.adapter = null
        _binding = null
        super.onDestroyView()
    }
}

private class ExecutionAdapter :
    ListAdapter<ExecutionItem, ExecutionAdapter.ViewHolder>(Diff) {

    class ViewHolder(private val binding: ItemExecutionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExecutionItem) {
            binding.textViewExecutionDate.text = UiFormat.date(item.executedAt)
            binding.textViewExecutionTitle.text = item.taskTitle ?: "(tarea eliminada)"
            binding.textViewExecutionStatus.text = item.status
            binding.textViewExecutionStatus.setTextColor(
                UiFormat.stateColor(binding.root.context, item.status)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemExecutionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private object Diff : DiffUtil.ItemCallback<ExecutionItem>() {
        override fun areItemsTheSame(a: ExecutionItem, b: ExecutionItem) = a.id == b.id
        override fun areContentsTheSame(a: ExecutionItem, b: ExecutionItem) = a == b
    }
}
