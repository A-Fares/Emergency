package com.afares.emergency.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.afares.emergency.data.model.Request
import com.afares.emergency.databinding.RequestRowLayoutBinding

class RequestAdapter : PagingDataAdapter<Request, RequestAdapter.RequestViewHolder>(Companion) {

    class RequestViewHolder(private val binding: RequestRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: Request) {
            binding.request = request
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RequestViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RequestRowLayoutBinding.inflate(layoutInflater, parent, false)
                return RequestViewHolder(binding)
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

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val currentItem = getItem(position) ?: return
        holder.bind(currentItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        return RequestViewHolder.from(parent)
    }
}