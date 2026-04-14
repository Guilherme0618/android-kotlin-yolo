package com.app.appdetector.model

data class DetectionResult(
    val label: String,
    val confidence: Float,
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float
)