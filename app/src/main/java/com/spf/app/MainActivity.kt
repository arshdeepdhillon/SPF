package com.spf.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.lang.Exception
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val _requestCode = 100
    private lateinit var _capture: Button
    private lateinit var _direction: Button
    private lateinit var _capturedAddresses: TextView
    private lateinit var _bitMap: Bitmap
    private lateinit var _py: Python
    private lateinit var _wazeRouteCalculator: PyObject
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        _py = Python.getInstance()
        _wazeRouteCalculator = _py.getModule("WazeRouteCalculator")

        _capture = findViewById(R.id.bttn_capture)
        _direction = findViewById(R.id.bttn_direction)
        _capturedAddresses = findViewById(R.id.txt_data)
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                _requestCode
            )
        }
        _capture.setOnClickListener {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this)
        }

        _direction.setOnClickListener {
//            val adrs = """
//            1102 Aviation Blvd NE
//            7 Aspen Summit Ct SW
//            142 Bridleridge Cir SW
//            109 11 Ave SE""".trimIndent()
            val adrs = """
            5877 Grousewoods Dr, North Vancouver, BC
            83 Broadway St W, Nakusp, BC
            2911 Weather Hill, West Kelowna, BC
            403 Eveline St, Selkirk, MB
            """.trimIndent()
            val str = buildGoogleMapUrl(adrs)

            //TODO use a DP? (also try 2/3-opt ???) for TSP algo, then pass the distance matrix from getDistanceMatrix() to that algo
            //getDistanceMatrix(adrs)

            //TODO uncomment me when trying to parse from _capturedAddresses.
            //val str = buildGoogleMapUrl(_capturedAddresses.text.toString())
            val gmmIntentUri = Uri.parse(str)
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
        // TODO: left to testing but remove it later
        _direction.visibility = View.VISIBLE
    }

    /**
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                CropImage.getActivityResult(data).let {
                    _bitMap = MediaStore.Images.Media.getBitmap(contentResolver, it.uri)
                    _capturedAddresses.text = getTextFromBitmap(_bitMap)
                }
            }

        }
    }

    /**
     *
     */
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
            _direction.visibility = View.VISIBLE
        }
        return str.toString()
    }


    /**
     * Builds a cross-platform Google maps direction URL with multiple waypoints starting from current location.
     * @param addrs A newline separated list of address or coordinates
     * @return
     */
    private fun buildGoogleMapUrl(addrs: String): String {
        val addresses = addrs.split("\n").toMutableList()
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

        val encodedUrl = builder.build().toString().replace("%2Bto%3A", "+to:").replace("%20", "+")
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
                _wazeRouteCalculator.callAttr("WazeRouteCalculator", startAddr, destAddr, region)
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
    private fun getDistanceMatrix(addresses: String): Array<IntArray> {
        val addrs = addresses.split("\n").toMutableList()
        val matrixSize = addrs.size

        // To avoid rounding issues, we scale our non-integer values
        val scaleMatrix = 100

        // Initialize the matrix with 0s
        val disMatrix = Array(matrixSize) { IntArray(matrixSize) }

        /**
         * To reduce the complexity, we will assume the distance is same between same points ( distance(A,B) <==> distance(B,A) ),
         * that is, our adjacency matrix will be symmetric.
         */
        for (start in addrs.indices) {
            /**
             * Fills the top triangle of matrix.
             * ie:
             *  0, 2240, 374765,   1090,
             *  0,    0, 375480,   1194,
             *  0,    0,      0, 375416,
             *  0,    0,      0,      0
             */
            for (end in start + 1 until matrixSize) {
                Log.d(
                    TAG, "Getting distance: (${addrs[start]} -> ${addrs[end]})"
                )
                val route = getDistance(addrs[start], addrs[end])

                if (route == null) {
                    Log.d(TAG, "No distance found: (${addrs[start]} -> ${addrs[end]})")
                } else {
                    val (time, dist) = route
                    Log.d(TAG, "$time mins, $dist km: (${addrs[start]} -> ${addrs[end]})")
                    disMatrix[start][end] = (dist * scaleMatrix).toInt()
                }
            }
        }

//        for (r in disMatrix) {
//            for (i in r) {
//                print(i)
//                print("\t")
//            }
//            println()
//        }
        return disMatrix
    }
}