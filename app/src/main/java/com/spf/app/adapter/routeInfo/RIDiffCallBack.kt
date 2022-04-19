package com.spf.app.adapter.routeInfo

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.spf.app.data.RouteInfo
import com.spf.app.ui.RoutesActivity.Companion.DRAG_STATE_CHANGED
import com.spf.app.ui.RoutesActivity.Companion.OPT_INDEX_CHANGED

/**
 * RouteInfo DiffCallBack
 */
class RIDiffCallBack(
    private val oldList: List<RouteInfo>,
    private val newList: List<RouteInfo>,
) : DiffUtil.Callback() {
    private val TAG = "RIDiffCallBack"

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
            if (oldItem.optIndex != newItem.optIndex)
                putLong(OPT_INDEX_CHANGED, newItem.optIndex)
        }
        Log.d(TAG, "getChangePayload: $diffBundle")
        return diffBundle
    }
}