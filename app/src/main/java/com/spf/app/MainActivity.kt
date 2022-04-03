package com.spf.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.spf.app.adapter.RouteGroupAdapter
import com.spf.app.data.RouteGroup
import com.spf.app.databinding.ActivityMainBinding
import com.spf.app.ui.ShowRoutesActivity
import com.spf.app.util.RouteGroupDialog
import com.spf.app.viewModel.RouteVM
import com.spf.app.viewModel.RouteVMFactory
import kotlinx.coroutines.launch

/**
 * TODO combine add and edit activities, they are basically doing the same thing
 * TODO connect this code:: take user to map after getting routes from image
 */
class MainActivity : AppCompatActivity(), RouteGroupAdapter.IRouteListener,
    RouteGroupDialog.RGDialogListener {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var py: Python
    private lateinit var wazeRouteCalculator: PyObject
    private val viewModel: RouteVM by viewModels {
        RouteVMFactory((application as RouteApplication).repository)
    }
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(TAG, "result.resultCode: ${result.resultCode}")
        }

    companion object {
        const val GROUP_ID = "group_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = RouteGroupAdapter(this)
        binding.groupsRecycler.adapter = adapter
        binding.groupsRecycler.layoutManager = LinearLayoutManager(this)
        viewModel.allGroups.observe(this) { groups -> adapter.submitList(groups) }

        binding.fabAddRoute.setOnClickListener {
//            val intent = Intent(this, AddRoutesActivity::class.java)
//            resultLauncher.launch(intent)
            //TODO on success result open AddRoutesActivity!
//            val intent = Intent(this, RouteGroupDialog::class.java)
//            resultLauncher.launch(intent)
            RouteGroupDialog().show(supportFragmentManager, "add_route_group")
        }
    }

    override fun onSave(data: RouteGroup) {
        viewModel.viewModelScope.launch {
            Log.d(TAG, "onSave: createGroup")
            val id = viewModel.createGroup(data)
            Log.d(TAG, "onSave: launching AddRoutes")
            launchAddRoutesActivity(id)
        }
    }

    private fun launchAddRoutesActivity(id: Long) {
        val intent = Intent(applicationContext, ShowRoutesActivity::class.java)
        intent.putExtra(GROUP_ID, id)
        resultLauncher.launch(intent)
    }

    override fun onRouteGroupClicked(id: Long) {
//        viewModel.triggerInitRoutesInGroupEvent()
        Intent(this, ShowRoutesActivity::class.java).also {
            it.putExtra(GROUP_ID, id)
            resultLauncher.launch(it)
        }
    }
}