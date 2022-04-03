package com.spf.app.util

import androidx.recyclerview.widget.DiffUtil
import com.spf.app.data.RouteInfo

class DIFF_CALLBACK_ROUTE_INFO(
    private val oldList: List<RouteInfo>,
    private val newList: List<RouteInfo>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].routeId == newList[newItemPosition].routeId
    }

    override fun areContentsTheSame(oldItemPos: Int, newItemPos: Int): Boolean {
        return oldList[oldItemPos].routeId == newList[newItemPos].routeId
                && oldList[oldItemPos].address == newList[newItemPos].address
                && oldList[oldItemPos].groupId == newList[newItemPos].groupId
                && oldList[oldItemPos].optIndex == newList[newItemPos].optIndex
    }
}