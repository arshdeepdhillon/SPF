package com.spf.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.spf.app.MainActivity.Companion.GROUP_ID
import com.spf.app.RouteApplication
import com.spf.app.adapter.RouteInfoAdapter
import com.spf.app.databinding.ActivityShowRoutesBinding
import com.spf.app.util.LK
import com.spf.app.viewModel.RouteVM
import com.spf.app.viewModel.RouteVMFactory
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.ItemTouchHelper.DOWN

import androidx.recyclerview.widget.RecyclerView
import com.spf.app.adapter.IRouteListener
import java.util.BitSet

class ShowRoutesActivity : AppCompatActivity(), IRouteListener {
    private val TAG = "AddRoutesActivity"
    private val camReqCode: Int = 100
    private lateinit var wazeRouteCalculator: PyObject
    private lateinit var py: Python
    private lateinit var binding: ActivityShowRoutesBinding
    private lateinit var adapter: RouteInfoAdapter
    private val invalidId: Long = -1L
    private var groupId: Long = invalidId
    private val viewModel: RouteVM by viewModels {
        RouteVMFactory((application as RouteApplication).repository)
    }

    /** Allows us to drag items in RecyclerView */
    private val itemTouchCallBack: ItemTouchHelper by lazy {
        val simpleItemTouchHelper = object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                // Move item in `fromPos` to `toPos` in adapter.
                adapter.notifyItemMoved(fromPos, toPos)

                Log.d(TAG, "onMove: $fromPos -> $toPos")
                return true
            }

            // Bypass long press so we can immediately execute drag events on touch
            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            // Not implementing, ignore it
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            // Not implementing, so do not allow left and right swipes
            override fun getSwipeDirs(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            ) = 0

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ) {
                Log.d(TAG, "clearView: ")
                adapter.onDrop()
                super.clearView(recyclerView, viewHolder)
            }
        }
        ItemTouchHelper(simpleItemTouchHelper)
    }

    companion object {
        /** IDs of views that changed */
        const val DRAG_STATE_CHANGED = "DRAG_STATE"

        /** User interaction with a view has started */
        val START_ANIM: BitSet = BitSet(2) //0x00

        /** User interaction with a view is over */
        val STOP_ANIM: BitSet = BitSet(2).apply { set(0) } //0x01

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        // TODO lazily use these objects!
        py = Python.getInstance()
        wazeRouteCalculator = py.getModule("WazeRouteCalculator")

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                camReqCode)
        }

        if (intent.hasExtra(GROUP_ID)) groupId = intent.getLongExtra(GROUP_ID, invalidId)
        adapter = RouteInfoAdapter(this)
        binding.routesRecycler.adapter = adapter
        binding.routesRecycler.layoutManager = LinearLayoutManager(this)
        itemTouchCallBack.attachToRecyclerView(binding.routesRecycler)
        viewModel.setRouteGroupId(groupId)
        showNavButton()
        // TODO On configuration, get data from RouteVM.cache instead of DB
//        viewModel.viewModelScope.launch { viewModel.triggerInitRoutesInGroupEvent() }
        viewModel.allRoutesInGroup.observe(this) { groups ->
            Log.d(TAG, "Addresses changed, sending new data")
            adapter.submitList(groups)
        }

        // Handle VMEvents triggered by setRouteGroupId()
//        lifecycleScope.launchWhenStarted {
//            viewModel.vmEventFlow.collect { event ->
//                when (event) {
//                    is RouteVM.VMEvent.RouteInfoEvent -> {
//                        Log.d(TAG, "onCreate: Handle address event")
//                        adapter.initData(event.routeInfoList as ArrayList<RouteInfo>)
//                    }
//                    is RouteVM.VMEvent.RouteGroupEvent -> {
//                        binding.routeGroupTitleEditText.setText(event.routeGroup.title)
//                    }
//                }
//            }
//        }

        binding.fabTakeImage.setOnClickListener {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this)
        }
        binding.fabNavigate.setOnClickListener {
            viewModel.viewModelScope.launch(Dispatchers.Default) {
                // Get all addresses from db
                // Get distances
                // Get matrix distance
                // Get optimal order list
                // Update optimal index
                // Build google uri from optimal order
                // Launch navigation

                val origAddrs = viewModel.getRoutesInGroupByOpt(groupId)
                val addresses: MutableList<String> = mutableListOf()
                origAddrs.forEach { addresses.add(it.address) }

                // TODO use a DP? (also try 2/3-opt ???) for TSP algo, then pass the distance matrix from getDistanceMatrix() to that algo
                val matrix = getDistanceMatrix(addresses)
                val optRoute = LK(matrix).solve()
                addresses.clear()
                optRoute.optTour
                    .forEachIndexed { optIndex, addrsIndex ->
                        Log.d(TAG, "onCreate: optIndex, addrsIndex: $optIndex, $addrsIndex")
                        viewModel.updateOpt(origAddrs[addrsIndex].routeId, optIndex.toLong())
                    }

                viewModel.getRoutesInGroupByOptWithoutCurrLocation(groupId)
                    .forEach { addresses.add(it.address) }
                val mapUrlStr = buildGoogleMapUrl(addresses)
                launchMap(mapUrlStr)
            }

        }
        binding.routeGroupTitleEditText.setOnFocusChangeListener { v, hasFocus ->
            // On focus lost, save changes
            if (!hasFocus) {
                viewModel.updateGroupTitle(groupId,
                    binding.routeGroupTitleEditText.text.toString())
            }
        }
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.addressUiState.collect { state ->
//                    Log.d(TAG, "onCreate: ${state}")
//                    when (state) {
//                        is UiState.AddressDragUiState -> {
//                            adapter.newUiState(state)
//                        }
//                    }
//                }
//            }
//        }

        when {
            savedInstanceState != null -> {
                savedInstanceState.let {
                    binding.routeGroupTitleEditText.setText(savedInstanceState.getString("title"))
                    groupId = savedInstanceState.getLong(GROUP_ID)

                    Log.d(TAG, "onCreate: Trigger init address event")
                    // TODO On configuration, get data from RouteVM.cache instead of DB
                    // viewModel.viewModelScope.launch { viewModel.triggerInitRoutesInGroupEvent() }
                }
            }
            groupId != invalidId -> {
                setupViewInit(groupId)
            }
            else -> {
                if (groupId == invalidId) {
                    Log.e(TAG, "Invalid Route Group ID passed: $groupId")
                    Toast.makeText(applicationContext,
                        "Unable to open this route group",
                        Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun launchMap(mapUrlStr: String) {
        val gmmIntentUri = Uri.parse(mapUrlStr)
        val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        //TODO since we are using Google map URL, don't need this. Let the device decide where to show directions (Google map app or a browser)
        //intent.setPackage("com.google.android.apps.maps")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            try {
                val unrestrictedIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                startActivity(unrestrictedIntent)
            } catch (innerEx: ActivityNotFoundException) {
                Toast.makeText(this, "Please install a maps application", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    /** @see com.spf.app.adapter.IRouteListener.addressChanged */
    override fun addressChanged(addressId: Long, changedAddress: String) {
        viewModel.updateRouteAddress(addressId, changedAddress)
    }

    /** @see com.spf.app.adapter.IRouteListener.deleteAddress */
    override fun deleteAddress(id: Long) {
        viewModel.deleteRoute(id)
    }

    /** @see com.spf.app.adapter.IRouteListener.handleTouch */
    override fun handleTouch(event: BitSet, routeViewHolder: RouteInfoAdapter.RouteViewHolder?) {
        when (event) {
            START_ANIM -> {
                Log.d(TAG, "handleTouch: START_ANIM")
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    viewModel.updateAddressUiState(groupId)
                    withContext(Dispatchers.Main) {
                        itemTouchCallBack.startDrag(routeViewHolder!!)
                    }
                }
            }
            STOP_ANIM -> {
                Log.d(TAG, "handleTouch: STOP_ANIM")
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    viewModel.updateAddressUiState(groupId)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Log.d(TAG, "onSaveInstanceState: curr =")
        // Save current of data
        outState.putString("title", binding.routeGroupTitleEditText.text.toString())
        outState.putLong(GROUP_ID, groupId)
    }

    /**
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                CropImage.getActivityResult(data).let {
                    val bitMap: Bitmap =
                        MediaStore.Images.Media.getBitmap(contentResolver, it.uri)
                    //TODO add addresses to db
                    val capturedAddresses = getTextFromBitmap(bitMap)
                    if (capturedAddresses.isEmpty()) {
                        Toast.makeText(this, "No addresses found", Toast.LENGTH_LONG).show()
                    } else {
                        insertAddresses(capturedAddresses.split("\n"))
                    }
                }
            }
        }
    }

    //TODO shouldn't need this. Utilize livedata
    /** Initializes the views */
    private fun setupViewInit(id: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            val routeGroup = viewModel.getGroup(id)
            withContext(Dispatchers.Main) {
                if (routeGroup != null) {
                    Log.d(TAG, "setupView: Setup route title")
                    binding.routeGroupTitleEditText.setText(routeGroup.title)
                    //TODO initialize address
                } else {
                    Log.e(TAG, "Route Group ID not found in DB: $id")
                    Toast.makeText(applicationContext,
                        "Failed to load addresses",
                        Toast.LENGTH_LONG).show()
                    val replyIntent = Intent()
                    setResult(RESULT_CANCELED, replyIntent)
                    finish()
                }
            }
        }
    }

    /** Inserts address retrieved from captured image */
    private fun insertAddresses(addrs: List<String>) {
        if (!addrs.isNullOrEmpty()) {
            addrs.forEach { viewModel.createRoute(groupId, it) }
            showNavButton()
        }
    }

    private fun hideNavButton() {
        if (binding.fabNavigate.isEnabled) {
            binding.fabNavigate.isEnabled = false
            binding.fabNavigate.visibility = View.INVISIBLE
        }
    }

    private fun showNavButton() {
        if (!binding.fabNavigate.isEnabled) {
            binding.fabNavigate.isEnabled = true
            binding.fabNavigate.visibility = View.VISIBLE
        }
    }

    private fun getTextFromBitmap(bm: Bitmap): String {
        val recognizer = TextRecognizer.Builder(this).build()
        val str = StringBuilder()
        if (recognizer.isOperational) {
            val frame: Frame = Frame.Builder().setBitmap(bm).build()
            val txtBlockSparseArray: SparseArray<TextBlock> = recognizer.detect(frame)
            for (i in 0 until txtBlockSparseArray.size()) {
                val txtBlock = txtBlockSparseArray.valueAt(i)
                str.append(txtBlock.value)
                str.append("\n")
            }
//            _direction.visibility = View.VISIBLE
        }
        return str.toString()
    }


    /**
     * Builds a cross-platform Google maps direction URL with multiple waypoints starting from current location.
     * @param addresses A newline separated list of address or coordinates
     * @return
     */
    private fun buildGoogleMapUrl(addresses: MutableList<String>): String {
        val firstAddrs = addresses[0]
        addresses.removeAt(0)
        val builder = Uri.Builder()
        builder.scheme("https")
            .authority("maps.google.ca")
            .appendPath("maps")
            .appendQueryParameter("f", "d")
            .appendQueryParameter("saddr", "My Location")
            // When adding waypoints (ie: '+to:'), 'daddr' acts as second waypoint instead of last waypoint.
            .appendQueryParameter(
                "daddr",
                firstAddrs + "+to:" + addresses.joinToString(separator = "+to:") { it })

        val encodedUrl =
            builder.build().toString().replace("%2Bto%3A", "+to:").replace("%20", "+")
        Log.d(TAG, "Encoded and formatted url: $encodedUrl")
        return encodedUrl
    }


    /**
     * Given a start and end location in address or coordinates format, it returns the actual route time and distance between those two points.
     * @param startAddr The starting address
     * @param destAddr The destination address
     * @return A [Pair]<[Double],[Double]> (route_duration, route_distance) in minutes and kilometers
     */
    private fun getDistance(startAddr: String, destAddr: String): Pair<Double, Double>? {
        var result: Pair<Double, Double>? = null
        val region = "NA"
        try {
            val res =
                wazeRouteCalculator.callAttr("WazeRouteCalculator",
                    startAddr,
                    destAddr,
                    region)
            //Log.d(TAG, "res: ${res}")
            val dis = res.callAttr("calc_route_info")
            //Log.d(TAG, "dis: ${dis}")
            //Log.d(TAG, "asList: ${dis.asList()[0].toString()}")
            result = Pair(dis.asList()[0].toDouble(), dis.asList()[1].toDouble())

        } catch (e: Exception) {
            Log.d(TAG, "Failed to get distance: $e")
        }
        return result
    }


    /**
     * Creates distance matrix
     * @param addresses Newline separated addresses or coordinates
     * @return [Array]<[IntArray]> An adjacency symmetric matrix containing distance between all nodes
     */
    private fun getDistanceMatrix(addresses: MutableList<String>): Array<IntArray> {
        val matrixSize = addresses.size

        // To avoid rounding issues, we scale our non-integer values
        val scaleMatrix = 100

        // Initialize the matrix with 0s
        val disMatrix = Array(matrixSize) { IntArray(matrixSize) }

        /**
         * To reduce the complexity, we will assume the distance is same between same points ( distance(A,B) <==> distance(B,A) ),
         * that is, our adjacency matrix will be symmetric.
         */
        for (start in addresses.indices) {
            for (end in start + 1 until matrixSize) {
                Log.d(
                    TAG, "Getting distance: (${addresses[start]} -> ${addresses[end]})"
                )
                val route = getDistance(addresses[start], addresses[end])

                if (route == null) {
                    Log.d(TAG, "No distance found: (${addresses[start]} -> ${addresses[end]})")
                } else {
                    val (time, dist) = route
                    Log.d(TAG,
                        "$time mins, $dist km: (${addresses[start]} -> ${addresses[end]})")
                    disMatrix[start][end] = (dist * scaleMatrix).toInt()
                    disMatrix[end][start] = disMatrix[start][end]
                }
            }
        }
        return disMatrix
    }
}