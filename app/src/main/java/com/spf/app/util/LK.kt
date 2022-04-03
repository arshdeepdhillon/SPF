package com.spf.app.util

import android.util.Log
import com.spf.app.TspLibParser
import org.jetbrains.annotations.TestOnly

data class Result(val error: String?, val tourCost: Int, val optTour: MutableList<Int>)
class LK(private var costMatrix: Array<IntArray>? = null) {
    private val _tag: String = "LK"
    private var _nCities: Int = 0
    private lateinit var _edges: Array<IntArray>
    private lateinit var _tspParser: TspLibParser
    private var _isTspLib: Boolean = false
    private lateinit var _tour: MutableList<Int>
    private var _tourCost: Int = 0


    /**
     * @return [String] containing <[_tourCost]\n[_tour]>
     */
    fun solve(): Result {

        if (!_isTspLib) {
            _edges = costMatrix!!
            _nCities = costMatrix!!.size
        }
        if (_nCities <= 2) {
            Log.d(_tag, "Must have at least 3 destinations")
            return Result("Must have at least 3 destinations", _tourCost, _tour)
        }
        initTour()
        improve()
        return Result("", _tourCost, _tour)
    }

    /**
     * Uses Greedy algorithm to construct the initial tour & cost.
     */
    private fun initTour(initNode: Int = 0) {
        val (tourCost, tour) = greedyTSP(initNode)
        _tourCost = tourCost
        _tour = tour
    }

    private fun cost(): Int {
        val start = _tour[0]
        var totalCost = 0
        for (end in 0 until _tour.size) {
            totalCost += weight(start, _tour[end])
        }
        return totalCost
    }

    private fun weight(a: Int, b: Int): Int {
        return _edges[a][b]
    }

    private fun improve() {

    }

    /**
     * Creates the tour using the following Greedy algorithm.
     *
     * - Step 1a Choose an initial node
     * - Step 1b Set initial node to current node
     * - Step 1c Set tour cost to 0
     * - Step 1d Create a list of same size total number of nodes and mark all them unvisited except the initial node
     * - Step 1e Add initial node to tour
     * - Step 2 From current node, choose the next node that isn't visited and has the least edge cost.
     * - Step 3 Mark current node visited
     * - Step 4 Add the edge cost to tour cost
     * - Step 5 Set next node as current node
     * - Step 6 If pending nodes to visit goto step 2 otherwise goto step 7
     * - Step 7 Add initial node to tour
     * - Step 8 Add edge code between initial and initial note
     *
     * @param initNode the starting node position (eg: 0,1,..,etc)
     * @return [Pair]<[Int], [MutableList]<[Int]>> first value contains the tour cost and second the Greedy tour
     */
    private fun greedyTSP(initNode: Int = 0): Pair<Int, MutableList<Int>> {
        var visited = 1
        var currNode = initNode
        var tourCost = 0
        val tour = mutableListOf<Int>()
        val visitedNodes: HashMap<Int, Boolean> = hashMapOf()

        for (node in 0 until _nCities) visitedNodes[node] = false
        visitedNodes[initNode] = true
        tour.add(initNode)

        while (visited < _nCities) {
            _edges[currNode]
                .withIndex()
                .filter { !(visitedNodes[it.index] as Boolean) }
                .minByOrNull { it.value }
                ?.let { nextMinNode ->
                    tour.add(nextMinNode.index)
                    visitedNodes[nextMinNode.index] = true
                    tourCost += nextMinNode.value
                    visited++
                    currNode = nextMinNode.index
                }
        }
        if (_isTspLib) {
            tour.add(initNode)
            tourCost += _edges[currNode][initNode]
            tour.forEachIndexed { index, tourIndex -> tour[index] = tourIndex + 1 }
        }
        Log.d(_tag, "init cost $tourCost, tour: $tour")
        return Pair(tourCost, tour)
    }

    @TestOnly
    fun setTspData(dataFile: String) {
        _isTspLib = true
        _tspParser = TspLibParser(dataFile)
        _edges = _tspParser.getMatrix()
        _nCities = _tspParser.size
    }
}