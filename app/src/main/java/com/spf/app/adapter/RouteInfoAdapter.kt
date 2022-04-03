package com.spf.app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.spf.app.data.RouteInfo
import com.spf.app.databinding.RouteInfoItemBinding
import com.spf.app.util.DIFF_CALLBACK_ROUTE_INFO
import android.text.Editable

import android.text.TextWatcher
import android.view.View
import androidx.core.widget.doOnTextChanged


class RouteInfoAdapter(
    private val listener: IRouteListener,
) : RecyclerView.Adapter<RouteInfoAdapter.RouteViewHolder>() {
    private val TAG = "RouteAdapter"
    private var currentList: ArrayList<RouteInfo> = arrayListOf()

    companion object {
        val DIFF_CALLBACK:
                DiffUtil.ItemCallback<RouteInfo> = object : DiffUtil.ItemCallback<RouteInfo>() {
            override fun areItemsTheSame(oldItem: RouteInfo, newItem: RouteInfo): Boolean {
                return oldItem.routeId == newItem.routeId
            }

            //TODO add rest of columns
            override fun areContentsTheSame(oldItem: RouteInfo, newItem: RouteInfo): Boolean {
                return oldItem.address == newItem.address && oldItem.groupId == newItem.groupId
            }
        }
    }

    interface IRouteListener {
        fun onRouteInfoClicked(id: Long)
        fun onAddressChanged(currAddressId: Long, changedAddress: String)
    }

    inner class RouteViewHolder(val binding: RouteInfoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val routeInfoId = currentList[bindingAdapterPosition].routeId
                    listener.onRouteInfoClicked(routeInfoId)
                } else {
                    Log.d(TAG, "Skipped handle item click: $bindingAdapterPosition")
                }
            }
        }

        fun setListeners() {
            binding.routeInfoInputEditText.setOnFocusChangeListener { v, hasFocus ->
                // On focus lost
                if (!hasFocus) {
                    val currRouteId = currentList[bindingAdapterPosition].routeId
                    listener.onAddressChanged(currRouteId,
                        binding.routeInfoInputEditText.text.toString())
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding =
            RouteInfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = RouteViewHolder(binding)
        viewHolder.setListeners()
        return viewHolder
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.binding.apply {
            val currRoute = currentList[position]
            routeInfoInput.editText?.setText(currRoute.address)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    fun submitList(newtList: List<RouteInfo>) {
        val diffUtil = DIFF_CALLBACK_ROUTE_INFO(currentList, newtList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        currentList = newtList as ArrayList<RouteInfo>
        diffResult.dispatchUpdatesTo(this)
    }

    fun initData(data: ArrayList<RouteInfo>) {
        Log.d(TAG, "initData: should be called once")
        currentList.clear()
        currentList = data
    }

}