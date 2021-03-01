package com.afares.emergency.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.afares.emergency.data.model.Request
import com.afares.emergency.databinding.HistoryRowLayoutBinding


class HistoryAdapter : PagingDataAdapter<Request, HistoryAdapter.HistoryViewHolder>(Companion) {

    class HistoryViewHolder(private val binding: HistoryRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: Request) {
            binding.request = request
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): HistoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HistoryRowLayoutBinding.inflate(layoutInflater, parent, false)
                return HistoryViewHolder(binding)
            }
        }

    }

    companion object : DiffUtil.ItemCallback<Request>() {
        override fun areItemsTheSame(oldItem: Request, newItem: Request): Boolean {
            return oldItem.created == newItem.created
        }

        override fun areContentsTheSame(oldItem: Request, newItem: Request): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = getItem(position) ?: return
        holder.bind(currentItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder.from(parent)
    }
}