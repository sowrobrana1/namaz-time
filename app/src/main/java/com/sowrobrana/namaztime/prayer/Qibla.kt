package com.sowrobrana.namaztime.prayer

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** Great-circle initial bearing from [lat]/[lng] to the Kaaba, in degrees (0–360). */
object Qibla {
    private const val KAABA_LAT = 21.3891
    private const val KAABA_LNG = 39.8579

    fun bearing(lat: Double, lng: Double): Double {
        val p1 = Math.toRadians(lat)
        val p2 = Math.toRadians(KAABA_LAT)
        val dl = Math.toRadians(KAABA_LNG - lng)
        val theta = atan2(
            sin(dl),
            cos(p1) * tan(p2) - sin(p1) * cos(dl),
        )
        var deg = Math.toDegrees(theta) % 360.0
        if (deg < 0) deg += 360.0
        return deg
    }
}
