package com.spf.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.spf.app.MainActivity.Companion.GROUP_ID
import com.spf.app.RouteApplication
import com.spf.app.adapter.RouteInfoAdapter
import com.spf.app.data.RouteInfo
import com.spf.app.databinding.ActivityShowRoutesBinding
import com.spf.app.viewModel.RouteVM
import com.spf.app.viewModel.RouteVMFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EditRoutesActivity : AppCompatActivity(), RouteInfoAdapter.IRouteListener {


    private lateinit var binding: ActivityShowRoutesBinding
    private val invalidId: Long = -1L
    private var groupId: Long = invalidId
    private val viewModel: RouteVM by viewModels {
        RouteVMFactory((application as RouteApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(GROUP_ID)) groupId = intent.getLongExtra(GROUP_ID, invalidId)
        val adapter = RouteInfoAdapter(this)
        binding.routesRecycler.adapter = adapter
        binding.routesRecycler.layoutManager = LinearLayoutManager(this)
        viewModel.setRouteGroupId(groupId)

        // TODO On configuration, get data from RouteVM.cache instead of DB
        viewModel.viewModelScope.launch { viewModel.triggerInitRoutesInGroupEvent() }
        viewModel.allRoutesInGroup.observe(this) { groups -> adapter.submitList(groups) }

        // Handle VMEvents triggered by setRouteGroupId()
        lifecycleScope.launchWhenStarted {
            viewModel.vmEventFlow.collect { event ->
                when (event) {
                    is RouteVM.VMEvent.RouteInfoEvent -> {
                        adapter.initData(event.routeInfoList as ArrayList<RouteInfo>)
                    }
                    is RouteVM.VMEvent.RouteGroupEvent -> {
                        binding.routeGroupTitleEditText.setText(event.routeGroup.title)
                    }
                }
            }
        }
        binding.routeGroupTitleEditText.setOnFocusChangeListener { v, hasFocus ->
            // On focus lost save changes
            if (!hasFocus) {
                viewModel.updateGroupTitle(groupId,
                    binding.routeGroupTitleEditText.text.toString())
            }
        }

    }

    override fun onRouteInfoClicked(id: Long) {
        // Do nothing
    }

    override fun onAddressChanged(currAddressId: Long, changedAddress: String) {
        viewModel.updateRouteAddress(currAddressId, changedAddress)
    }
}