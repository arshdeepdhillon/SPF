package com.spf.app

import com.spf.app.TestData.TspLib.att1
import com.spf.app.TestData.TspLib.att3
import com.spf.app.TestData.TspLib.att48
import com.spf.app.TestData.TspLib.optTourAtt48
import com.spf.app.TestData.TspLib.expectedAtt48
import org.junit.Test

import org.junit.Assert.*

class TspLibParserTest {

    @Test
    fun null_on_empty_data() {
        val parser = TspLibParser("")
        val matrix = parser.getMatrix()
        assertTrue(matrix.isEmpty())
        assertNotNull(matrix)
    }

    @Test
    fun parse_desc() {
        val parser = TspLibParser(att1)
        assert(parser.testProcessDesc().toList().isNotEmpty())
        assert(parser.dataName == "att1")
        assert(parser.size == 1)
    }

    @Test
    fun parse_nodes() {
        val parser = TspLibParser(att3)
        val actual = parser.testProcessData()
        val expected = arrayOf(
            Point(0, 0),
            Point(2, 2),
            Point(4, 4))
        assert(actual.size == expected.size)
        assertTrue(actual.zip(expected).all { (a, b) -> a.x == b.x && a.y == b.y })
    }

    @Test
    fun parse_matrix() {
        val parser = TspLibParser(att3)
        val actual = parser.getMatrix()
        val expected = arrayOf(intArrayOf(Int.MAX_VALUE, 2, 5),
            intArrayOf(2, Int.MAX_VALUE, 2),
            intArrayOf(5, 2, Int.MAX_VALUE))
        assertNotNull(actual)
        for (row in actual.indices) {
            for (col in 0 until parser.size) {
                assert(expected[row][col] == actual[row][col])
            }
        }
    }

    @Test
    fun parse_scaled_matrix() {
        val parser = TspLibParser(att3, true)
        val actual = parser.getMatrix()
        val expected =
            arrayOf(intArrayOf(Int.MAX_VALUE, 282, 565),
                intArrayOf(282, Int.MAX_VALUE, 282),
                intArrayOf(565, 282, Int.MAX_VALUE))
        assertNotNull(actual)
        for (row in actual.indices) {
            for (col in 0 until parser.size) {
                assert(expected[row][col] == actual[row][col])
            }
        }
    }

    @Test
    fun parse_large_matrix() {
        val parser = TspLibParser(att48)
        val actual = parser.getMatrix()
        val expected = expectedAtt48
        assertNotNull(actual)
        for (row in actual.indices) {
            for (col in 0 until parser.size) {
                assert(expected[row][col] == actual[row][col])
            }
        }
    }

    @Test
    fun parse_large_opt_tour() {
        val optTour = optTourAtt48
        val parser = TspLibParser(att48)
        parser.initMatrix()
        parser.testProcessOptTour(optTour)
    }
}