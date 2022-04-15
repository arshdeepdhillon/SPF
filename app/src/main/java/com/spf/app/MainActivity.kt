package com.spf.app

import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.spf.app.adapter.routeGroup.IRouteGroupListener
import com.spf.app.adapter.routeGroup.RouteGroupAdapter
import com.spf.app.data.DataState
import com.spf.app.data.RouteGroup
import com.spf.app.databinding.ActivityMainBinding
import com.spf.app.ui.ShowRoutesActivity
import com.spf.app.util.RouteGroupDialog
import com.spf.app.viewModel.RouteVM
import com.spf.app.viewModel.RouteVMFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class MainActivity : AppCompatActivity(), IRouteGroupListener,
    RouteGroupDialog.RGDialogListener {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: RouteGroupAdapter
    private val viewModel: RouteVM by viewModels {
        RouteVMFactory((application as RouteApplication).repository)
    }
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(TAG, "result.resultCode: ${result.resultCode}")
        }

    /** Allows us to swipe items in RecyclerView */
    private val itemTouchCallBack: ItemTouchHelper by lazy {
        val simpleItemTouchHelper =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when (direction) {
                        ItemTouchHelper.RIGHT -> {
                            adapter.deleteSwipedItem(viewHolder)
                        }
                        ItemTouchHelper.LEFT -> {
                            //TODO launch navigation
                            Log.d(TAG, "onSwiped: Left: ${viewHolder.absoluteAdapterPosition}")
                        }
                    }
                }

                override fun onChildDraw(
                    c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                    actionState: Int, isCurrentlyActive: Boolean,
                ) {
                    RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.swipe_delete_color))
                        .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.swipe_navigation_color))
                        .addSwipeLeftActionIcon(R.drawable.ic_round_navigate_24)
                        .create()
                        .decorate()
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        ItemTouchHelper(simpleItemTouchHelper)
    }

    companion object {
        const val GROUP_ID = "group_id"

        /** IDs of views that changed */
        const val GROUP_TITLE_CHANGED = "GROUP_TITLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RouteGroupAdapter(this)
        binding.groupsRecycler.adapter = adapter
        binding.groupsRecycler.layoutManager = LinearLayoutManager(this)
        viewModel.allGroups.observe(this) { groups ->
            Log.d(TAG, "Groups changed, sending new data")
            adapter.submitList(groups)
        }
        itemTouchCallBack.attachToRecyclerView(binding.groupsRecycler)
        binding.fabAddRoute.setOnClickListener {
            RouteGroupDialog().show(supportFragmentManager, "add_route_group")
        }
    }

    override fun onSave(groupTitle: String) {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val id = viewModel.createGroup(groupTitle)
            launchAddRoutesActivity(id)
        }
    }

    /** @see com.spf.app.adapter.routeGroup.IRouteGroupListener.groupClicked */
    override fun groupClicked(groupId: Long) {
        launchAddRoutesActivity(groupId)
    }

    private fun launchAddRoutesActivity(groupId: Long) {
        Intent(applicationContext, ShowRoutesActivity::class.java).also {
            it.putExtra(GROUP_ID, groupId)
            resultLauncher.launch(it)
        }
    }

    override fun removeGroup(groupId: Long) {
        viewModel.updateGroupState(groupId, DataState.HIDE)
    }

    override fun addGroup(removedItem: RouteGroup) {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.createGroup(removedItem)
        }
    }

    //TODO refactor this so we can handle delete or other swipe action.
    override fun showSBOnDelete(bindingPost: Int, groupId: Long) {
        // On each item removed, create a new Snackbar.
        Snackbar.make(binding.mainActivityLayout, getString(R.string.snackbar_delete_group), Snackbar.LENGTH_LONG)
            .setAction("UNDO") { viewModel.updateGroupState(groupId, DataState.SHOW) }
            .setBehavior(BaseTransientBottomBar.Behavior().apply { setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY) })
            .setAnchorView(binding.fabAddRoute)
            .addCallback(
                object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        // Snackbar action wasn't clicked, so delete the item
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
                            viewModel.updateGroupState(groupId, DataState.DELETE)
                    }
                })
            .show()
    }
}