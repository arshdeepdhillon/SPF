package com.spf.app

import android.util.Log
import org.jetbrains.annotations.TestOnly
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @param x the x coordinate
 * @param y the y coordinate
 */
class Point(var x: Int, var y: Int)

/**
 * Parses [TSPLIB][http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95] formatted data.
 *
 * @param data TSPLIB formatted data
 * @param isScaled Scales the matrix by [scaleMatrix] factor if true
 */
class TspLibParser(private val data: String, private val isScaled: Boolean = false) {
    private val tag = "TspLibParser"

    /** Size of matrix */
    var size: Int = 0
        private set

    /** Name of TSP file */
    lateinit var dataName: String
        private set

    /** Current tour */
    lateinit var tour: MutableList<Int>
        private set

    /** Cost of tour */
    var cost: Int = 0
        private set

    lateinit var edges: Array<IntArray>

    companion object {
        /** Factor to scale the matrix by */
        const val scaleMatrix: Int =
            100 // To avoid rounding issues, we scale our non-integer values
    }

    private enum class TspLib(value: String) {
        DIMENSION("DIMENSION"),
        NAME("NAME"),
        TOUR_SECTION("TOUR_SECTION"),
        NODE_COORD_SECTION("NODE_COORD_SECTION"),
        EOF("EOF")
    }

    private fun processDesc(descData: String = data, procOpt: Boolean = false): Sequence<String> {
        val lineBreak = if (procOpt) TspLib.TOUR_SECTION.name else TspLib.NODE_COORD_SECTION.name
        return descData
            .trimIndent()
            .lineSequence()
            .map { it.trim() }
            .toList()
            .asSequence()
            .dropWhile { line ->
                when {
                    line.contains(TspLib.DIMENSION.name) -> {
                        size = line.split(":")[1].trim().toInt()
                    }
                    line.contains(TspLib.NAME.name) -> {
                        dataName = line.split(":")[1].trim()
                    }
                }
                line != lineBreak
            }
    }

    private fun processPoints(): List<Point> {
        val dataToProcess = processDesc()
        val reg: Regex = "\\s+".toRegex()
        val result = dataToProcess
            .drop(1)
            .takeWhile { it != TspLib.EOF.name }
            .filter {
                if (it.split(reg).size != 3) {
                    Log.w(tag,
                        "Node data not formatted as {<integer> <real_x> <real_y>}, skipping: $it")
                }
                it.split(reg).size == 3
            }
            .map {
                it.split(reg).let { data -> Point(data[1].toInt(), data[2].toInt()) }
            }.toList()
        return result
    }


    /**
     * Calculates the Euclidean distance between two points on 2D Cartesian plane.
     *
     * @param a first [Point]
     * @param b second [Point]
     * @return the Euclidean distance between [a] and [b] points in [Double]
     */
    private fun getWeight(a: Point, b: Point): Double {
        val deltaX = (a.x - b.x).toDouble()
        val deltaY = (a.y - b.y).toDouble()
        return sqrt((deltaX.pow(2.0) + deltaY.pow(2.0)))
    }


    /**
     * Creates the distance matrix
     * @return [Array]<[IntArray]> an adjacency symmetric matrix containing distance between all nodes or empty 2D Array
     */
    fun getMatrix(): Array<IntArray> {
        var disMatrix: Array<IntArray> = Array(0) { IntArray(0) }
        val data = processPoints().toList()
        when {
            data.isEmpty() -> {
                Log.w(tag, "No data found")
            }
            size != data.size -> {
                Log.e(
                    tag,
                    "Improper data set: Expected dimension: ${size}, Actual dimension: ${data.size}"
                )
            }
            else -> {
                disMatrix = Array(size) { IntArray(size) { Int.MAX_VALUE } }
                var cost: Double
                for (start in data.indices) {
                    for (end in start + 1 until size) {
                        cost = getWeight(data[start], data[end])
                        if (isScaled) cost *= scaleMatrix
                        disMatrix[start][end] = cost.toInt()
                        disMatrix[end][start] = disMatrix[start][end]
                    }
                }
            }
        }
        return disMatrix
    }

    @TestOnly
    fun testProcessDesc(): Sequence<String> {
        return processDesc()
    }

    @TestOnly
    fun testProcessData(): List<Point> {
        return processPoints()
    }

    @TestOnly
    fun initMatrix() {
        edges = getMatrix()
    }

    @TestOnly
    fun testProcessOptTour(optTour: String) {
        val tourToProcess = processDesc(optTour, true)
        val reg: Regex = "\\s+".toRegex()
        tour = mutableListOf<Int>()
        cost = 0

        tourToProcess
            .drop(1)
            .takeWhile { it != "EOF" }
            .filter {
                if (it.split(reg).size != 1) {
                    Log.w(tag,
                        "Node data not formatted as {<integer>}, skipping: $it")
                }
                it.split(reg).size == 1
            }
            .map { node ->
                tour.add(node.toInt())
                // Don't handle last (returning to initial point) point here
                if (tour.size > 1 && node.toInt() > 0) {
                    // "tour[...] - 1" is required because Nodes in TspLib data start from 1
                    val currNode = tour[tour.lastIndex] - 1
                    val prvNode = tour[tour.lastIndex - 1] - 1
                    cost += edges[prvNode][currNode]
                }
            }.toList()
        tour.isNotEmpty()
            .and(tour.size > 1)
            .let {
                // Handle last point; Set last Node as positive value
                tour[tour.lastIndex] = abs(tour[tour.lastIndex])
                val lastNode = tour[tour.lastIndex] - 1
                val secLastNode = tour[tour.lastIndex - 1] - 1
                cost += edges[secLastNode][lastNode]
            }
    }
}
