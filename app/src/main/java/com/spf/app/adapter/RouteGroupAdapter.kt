package com.spf.app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spf.app.data.RouteGroup
import com.spf.app.databinding.RouteGroupItemBinding


class RouteGroupAdapter(private val listener: IRouteListener) :
    ListAdapter<RouteGroup, RouteGroupAdapter.RouteViewHolder>(DIFF_CALLBACK) {
    private val TAG = "RouteAdapter"

    companion object {
        val DIFF_CALLBACK:
                DiffUtil.ItemCallback<RouteGroup> = object : DiffUtil.ItemCallback<RouteGroup>() {
            override fun areItemsTheSame(oldItem: RouteGroup, newItem: RouteGroup): Boolean {
                return oldItem.groupId == newItem.groupId
            }

            //TODO add rest of columns
            override fun areContentsTheSame(oldItem: RouteGroup, newItem: RouteGroup): Boolean {
                return oldItem.title == newItem.title
            }
        }
    }

    interface IRouteListener {
        fun onRouteGroupClicked(id: Long)
    }

    inner class RouteViewHolder(val binding: RouteGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val routeGroupId = currentList[bindingAdapterPosition].groupId
                    listener.onRouteGroupClicked(routeGroupId)
                } else {
                    Log.d(TAG, "Skipped handle item click: $bindingAdapterPosition")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding =
            RouteGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.binding.apply {
            val currRoute = getItem(position)
            routeGroupTitleLayoutText.text = currRoute.title
        }
    }

}