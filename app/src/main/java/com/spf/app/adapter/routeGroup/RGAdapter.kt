package com.spf.app.adapter.routeGroup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.spf.app.MainActivity
import com.spf.app.data.RouteGroup
import com.spf.app.databinding.RouteGroupItemBinding

interface IRouteGroupListener {
    /** Broadcasts the group id that was clicked */
    fun groupClicked(groupId: Long)

    /** Broadcasts the group id that was swiped for deletion */
    fun swipedForDeletion(groupId: Long)
}

class RouteGroupAdapter(private val listener: IRouteGroupListener) :
    RecyclerView.Adapter<RouteGroupAdapter.RouteViewHolder>() {
    private val TAG = "RouteGroupAdapter"
    private var currentList: ArrayList<RouteGroup> = arrayListOf()

    inner class RouteViewHolder(val binding: RouteGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            setListeners()
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setListeners() {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val routeGroupId = currentList[bindingAdapterPosition].groupId
                    listener.groupClicked(routeGroupId)
                } else {
                    Log.d(TAG, "Skipped handle item click: $bindingAdapterPosition")
                }
            }
        }

        /**
         * Binds all items in ViewHolder
         */
        fun bind(routeGroup: RouteGroup) {
            Log.d(TAG, "bind: ")
            binding.routeGroupTitleLayoutText.text = routeGroup.title

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding =
            RouteGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteGroupAdapter.RouteViewHolder, position: Int) {
        // Binds all views when we don't know which item changed in ViewHolder
        holder.bind(currentList[position])
    }

    override fun onBindViewHolder(
        holder: RouteGroupAdapter.RouteViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        // If payload is empty then bind all views otherwise only those whose content changed
        if (payloads.isNotEmpty()) {
            (payloads[0] as Bundle).let { payload ->
                for (key in payload.keySet()) {
                    when (key) {
                        MainActivity.GROUP_TITLE_CHANGED -> {
                            holder.binding.routeGroupTitleLayoutText.text = payload.getString(key)
                        }
                    }
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun submitList(newtList: List<RouteGroup>) {
        val diffUtil = RGDiffCallBack(currentList, newtList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        currentList = newtList as ArrayList<RouteGroup>
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    /**
     * When item has been swiped for deletion, hide it in UI.
     */
    fun swipedForDeletion(vh: RecyclerView.ViewHolder) {
        listener.swipedForDeletion(currentList[vh.bindingAdapterPosition].groupId)
        currentList.removeAt(vh.absoluteAdapterPosition)
        notifyItemRemoved(vh.bindingAdapterPosition)
    }
}