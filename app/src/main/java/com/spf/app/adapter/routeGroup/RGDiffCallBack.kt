package com.spf.app.adapter.routeGroup

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.spf.app.MainActivity.Companion.GROUP_TITLE_CHANGED
import com.spf.app.data.RouteGroup

/**
 * RouteGroup DiffCallBack
 */
class RGDiffCallBack(
    private val oldList: List<RouteGroup>,
    private val newList: List<RouteGroup>,
) : DiffUtil.Callback() {
    private val TAG = "RGDiffCallBack"

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].groupId == newList[newItemPosition].groupId
    }

    override fun areContentsTheSame(oldItemPos: Int, newItemPos: Int): Boolean {
        return oldList[oldItemPos].groupId == newList[newItemPos].groupId
                && oldList[oldItemPos].title == newList[newItemPos].title
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        val diffBundle = Bundle()
        diffBundle.apply {
            if (oldItem.title != newItem.title) putString(GROUP_TITLE_CHANGED, newItem.title)
        }
        Log.d(TAG, "getChangePayload: $diffBundle")
        return diffBundle
    }
}