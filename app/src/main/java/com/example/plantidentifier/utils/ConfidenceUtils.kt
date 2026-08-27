package com.example.plantidentifier.utils

import java.util.Locale

fun formatConfidence(score: Double): String =
    String.format(Locale.US, "%.1f%%", (score.coerceIn(0.0, 1.0) * 100))
