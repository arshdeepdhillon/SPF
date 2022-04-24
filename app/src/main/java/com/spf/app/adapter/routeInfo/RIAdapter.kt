package com.spf.app.adapter.routeInfo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.spf.app.data.RouteInfo
import com.spf.app.databinding.RouteInfoItemBinding
import com.spf.app.ui.RoutesActivity.Companion.DRAG_STATE_CHANGED
import com.spf.app.ui.RoutesActivity.Companion.START_ANIM
import com.spf.app.ui.RoutesActivity.Companion.STOP_ANIM
import java.util.BitSet
import java.util.Collections
import kotlin.collections.ArrayList

interface IRouteListener {
    /** Broadcasts that address has changed */
    fun addressChanged(addressId: Long, changedAddress: String)

    /** Broadcasts the id of address to delete */
    fun deleteAddress(id: Long)

    /** Broadcasts an event when an address view is touched */
    // TODO create appropriate event class?
    fun handleTouch(event: BitSet, routeViewHolder: RecyclerView.ViewHolder, fromItem: RouteInfo? = null, toPos: Int? = null)
}

class RouteInfoAdapter(private val listener: IRouteListener) : RecyclerView.Adapter<RouteInfoAdapter.RouteViewHolder>() {
    private val TAG = "RouteAdapter"
    private var currentList: List<RouteInfo> = mutableListOf()
    private var fromPosItem: RouteInfo? = null
    private var toPos: Int? = null

    inner class RouteViewHolder(val binding: RouteInfoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            setListeners()
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setListeners() {

            // TODO improve this logic
            // On focus lost save changes
            binding.routeInfoInputEditText.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        val currRouteId = currentList[bindingAdapterPosition].routeId
                        listener.addressChanged(currRouteId, binding.routeInfoInputEditText.text.toString())
                    }
                }
            }
            binding.deleteAddressButton.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val currRouteId = currentList[bindingAdapterPosition].routeId
                    listener.deleteAddress(currRouteId)
                }
            }
            binding.dragButton.setOnTouchListener { v, event ->
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        fromPosItem = currentList[bindingAdapterPosition]
                        toPos = null
                        listener.handleTouch(START_ANIM, this)
                    }
                }
                false
            }
        }

        /**
         * Binds all items in ViewHolder
         */
        fun bind(routeInfo: RouteInfo) {
            binding.routeInfoInputEditText.setText(routeInfo.address)
            binding.deleteAddressButton.visibility = if (routeInfo.dragState) View.INVISIBLE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RouteInfoItemBinding.inflate(layoutInflater, parent, false)
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        // Binds all views when we don't know which item changed in ViewHolder
        holder.bind(currentList[position])
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int, payloads: MutableList<Any>) {
        // If payload is empty then bind all views otherwise only those whose content changed
        if (payloads.isNotEmpty()) {
            (payloads[0] as Bundle).let { payload ->
                for (key in payload.keySet()) {
                    when (key) {
                        DRAG_STATE_CHANGED -> {
                            // Hide delete button if drag state is true
                            if (payload.getBoolean(key))
                                holder.binding.deleteAddressButton.visibility = View.INVISIBLE
                            else
                                holder.binding.deleteAddressButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    fun submitList(newtList: List<RouteInfo>) {
        Log.d(TAG, "submitList: got new list")
        val diffUtil = RIDiffCallBack(currentList, newtList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        currentList = newtList as ArrayList<RouteInfo>
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Notify listeners that user has finished dragging the address view.
     * NOTE: Do not make these method UI thread intensive.
     */
    fun onDragFinish(viewHolder: RecyclerView.ViewHolder) {
        listener.handleTouch(STOP_ANIM, viewHolder, fromPosItem, toPos)
    }

    fun moveItem(fromPos: Int, toPos: Int) {
        Log.d(TAG, "moveItem: ${fromPos}->${toPos} : ${currentList[fromPos].address} ${currentList[fromPos].optIndex}->${currentList[toPos].optIndex} ${currentList[toPos].address}")
        this.toPos = toPos
        if (fromPos < toPos) {
            for (i in fromPos until toPos) {
                Collections.swap(currentList, i, i + 1)
            }
        } else {
            for (i in fromPos downTo toPos + 1) {
                Collections.swap(currentList, i, i - 1)
            }
        }
    }
}