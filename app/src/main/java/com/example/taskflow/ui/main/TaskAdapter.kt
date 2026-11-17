package com.example.taskflow.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflow.data.local.TaskEntity
import com.example.taskflow.databinding.ItemTaskBinding
import com.example.taskflow.ui.UiFormat
import com.example.taskflow.worker.toRule

class TaskAdapter(
    private val onSwitchChanged: (TaskEntity, Boolean) -> Unit,
    private val onDeleteClicked: (TaskEntity) -> Unit,
    private val onItemClicked: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(DiffCallback) {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskEntity) {
            val context = binding.root.context
            binding.textViewTaskTitle.text = task.title
            binding.textViewTaskInterval.text = UiFormat.interval(task)
            binding.textViewTaskRule.text = task.toRule().describe()
            binding.textViewTaskState.text = "[${task.state}]"
            binding.textViewTaskState.setTextColor(UiFormat.stateColor(context, task.state))

            // Quitar el listener antes de cambiar isChecked, para no disparar el evento al reciclar vistas
            binding.switchEnabled.setOnCheckedChangeListener(null)
            binding.switchEnabled.isChecked = task.enabled
            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
                onSwitchChanged(task, checked)
            }

            binding.buttonDelete.setOnClickListener { onDeleteClicked(task) }
            binding.root.setOnClickListener { onItemClicked(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity) =
            oldItem == newItem
    }
}
