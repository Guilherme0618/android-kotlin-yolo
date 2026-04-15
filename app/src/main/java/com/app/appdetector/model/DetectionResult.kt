package com.app.appdetector.model

data class DetectionResult(
    val classIndex: Int,
    val confidence: Float,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float
)