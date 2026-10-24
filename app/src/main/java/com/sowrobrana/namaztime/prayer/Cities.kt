package com.sowrobrana.namaztime.prayer

data class City(val name: String, val lat: Double, val lng: Double)

val BD_CITIES = listOf(
    City("ঢাকা", 23.81, 90.41),
    City("চট্টগ্রাম", 22.36, 91.78),
    City("খুলনা", 22.85, 89.54),
    City("রাজশাহী", 24.37, 88.60),
    City("সিলেট", 24.89, 91.87),
    City("বরিশাল", 22.70, 90.37),
    City("রংপুর", 25.74, 89.28),
    City("ময়মনসিংহ", 24.75, 90.40),
    City("কুমিল্লা", 23.46, 91.18),
    City("কক্সবাজার", 21.43, 92.00),
)
