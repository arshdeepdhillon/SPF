package com.spf.app.util

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.spf.app.data.RouteInfo
import com.spf.app.ui.ShowRoutesActivity.Companion.DRAG_STATE_CHANGED

class DiffCallBackRouteInfo(
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
                && oldList[oldItemPos].dragState == newList[newItemPos].dragState
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        val diffBundle = Bundle()
        diffBundle.apply {
            if (oldItem.dragState != newItem.dragState)
                putBoolean(DRAG_STATE_CHANGED, newItem.dragState)

        }
        Log.d("DIFF_CALLBACK_ROUTE_INFO", "getChangePayload: $diffBundle")
        return diffBundle
    }
}