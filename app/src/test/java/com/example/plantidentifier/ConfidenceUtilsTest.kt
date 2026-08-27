package com.example.plantidentifier

import com.example.plantidentifier.utils.formatConfidence
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfidenceUtilsTest {
    @Test
    fun formatsScoreAsPercentage() {
        assertEquals("26.5%", formatConfidence(0.265))
    }

    @Test
    fun clampsInvalidScores() {
        assertEquals("0.0%", formatConfidence(-1.0))
        assertEquals("100.0%", formatConfidence(2.0))
    }
}
