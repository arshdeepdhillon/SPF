package com.spf.app

import android.util.Log
import org.jetbrains.annotations.TestOnly
import kotlin.math.pow
import kotlin.math.sqrt


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
    var size: Int = -1
        private set

    /** Name of TSP file */
    lateinit var dataName: String
        private set

    companion object {
        /** Factor to scale the matrix by */
        const val scaleMatrix: Int =
            100 // To avoid rounding issues, we scale our non-integer values
    }

    @TestOnly
    fun testProcessDesc(): Sequence<String> {
        return processDesc()
    }

    @TestOnly
    fun testProcessData(): List<Point> {
        return processData()
    }

    private fun processDesc(): Sequence<String> {
        return data
            .trimIndent()
            .lineSequence()
            .map { it.trim() }
            .toList()
            .asSequence()
            .dropWhile { line ->
                when {
                    line.contains("DIMENSION") -> {
                        size = line.split(":")[1].trim().toInt()
                    }
                    line.contains("NAME") -> {
                        dataName = line.split(":")[1].trim()
                    }
                }
                line != "NODE_COORD_SECTION"
            }
    }

    private fun processData(): List<Point> {
        val dataToProcess = processDesc()
        val reg: Regex = "\\s+".toRegex()
        val result = dataToProcess
            .drop(1)
            .takeWhile { it != "EOF" }
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


    private fun getWeight(a: Point, b: Point): Double {
        val deltaX = (a.x - b.x).toDouble()
        val deltaY = (a.y - b.y).toDouble()
        return sqrt((deltaX.pow(2.0) + deltaY.pow(2.0)))
    }


    /**
     * Creates the distance matrix
     * @return [Array]<[IntArray]> An adjacency symmetric matrix containing distance between all nodes or null
     */
    fun getMatrix(): Array<IntArray>? {
        var disMatrix: Array<IntArray>? = null
        val data = processData().toList()
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
                disMatrix = Array(size) { IntArray(size) }
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
}
