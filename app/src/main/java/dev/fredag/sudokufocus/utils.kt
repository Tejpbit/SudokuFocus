package dev.fredag.sudokufocus

import kotlin.math.sqrt

fun hypotenuse(side1: Float, side2: Float): Float {
    return sqrt(side1 * side1 + side2 * side2.toDouble()).toFloat()
}