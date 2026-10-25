package com.sowrobrana.namaztime.prayer

data class City(val name: String, val lat: Double, val lng: Double)

/**
 * All 64 districts of Bangladesh, by district-town coordinates.
 * The first 10 entries keep the v0.1.x order so a saved city index
 * from an earlier version still points at the same district.
 * Times are computed from these coordinates: they match the local
 * mosque azan within ~1-2 minutes (mosques vary by a minute or two).
 */
val BD_CITIES = listOf(
    // v0.1.x order (preserved).
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
    // Dhaka division (remaining).
    City("ফরিদপুর", 23.50, 89.83),
    City("গাজীপুর", 24.00, 90.43),
    City("গোপালগঞ্জ", 23.00, 89.83),
    City("কিশোরগঞ্জ", 24.44, 90.78),
    City("মাদারীপুর", 23.17, 90.20),
    City("মানিকগঞ্জ", 23.86, 90.01),
    City("মুন্সিগঞ্জ", 23.54, 90.53),
    City("নারায়ণগঞ্জ", 23.62, 90.50),
    City("নরসিংদী", 23.92, 90.72),
    City("রাজবাড়ী", 23.76, 89.65),
    City("শরীয়তপুর", 23.24, 90.44),
    City("টাঙ্গাইল", 24.25, 89.92),
    // Chattogram division (remaining).
    City("বান্দরবান", 22.20, 92.22),
    City("ব্রাহ্মণবাড়িয়া", 23.96, 91.11),
    City("চাঁদপুর", 23.23, 90.65),
    City("ফেনী", 23.02, 91.40),
    City("খাগড়াছড়ি", 23.12, 91.98),
    City("লক্ষ্মীপুর", 22.95, 90.82),
    City("নোয়াখালী", 22.82, 91.10),
    City("রাঙ্গামাটি", 22.65, 92.18),
    // Barishal division (remaining).
    City("বরগুনা", 22.16, 90.13),
    City("ভোলা", 22.69, 90.65),
    City("ঝালকাঠি", 22.64, 90.20),
    City("পটুয়াখালী", 22.36, 90.45),
    City("পিরোজপুর", 22.58, 89.98),
    // Khulna division (remaining).
    City("বাগেরহাট", 22.66, 89.79),
    City("চুয়াডাঙ্গা", 23.64, 88.86),
    City("যশোর", 23.17, 89.21),
    City("ঝিনাইদহ", 23.54, 89.18),
    City("কুষ্টিয়া", 23.91, 89.12),
    City("মাগুরা", 23.49, 89.42),
    City("মেহেরপুর", 23.77, 88.63),
    City("নড়াইল", 23.17, 89.50),
    City("সাতক্ষীরা", 22.72, 89.08),
    // Mymensingh division (remaining).
    City("জামালপুর", 24.92, 89.94),
    City("নেত্রকোনা", 24.88, 90.73),
    City("শেরপুর", 25.02, 90.02),
    // Rajshahi division (remaining).
    City("বগুড়া", 24.85, 89.37),
    City("জয়পুরহাট", 25.10, 89.02),
    City("নওগাঁ", 24.81, 88.95),
    City("নাটোর", 24.41, 88.93),
    City("চাঁপাইনবাবগঞ্জ", 24.60, 88.28),
    City("পাবনা", 24.01, 89.24),
    City("সিরাজগঞ্জ", 24.45, 89.71),
    // Rangpur division (remaining).
    City("দিনাজপুর", 25.63, 88.64),
    City("গাইবান্ধা", 25.33, 89.54),
    City("কুড়িগ্রাম", 25.81, 89.66),
    City("লালমনিরহাট", 25.92, 89.45),
    City("নীলফামারী", 25.93, 88.85),
    City("পঞ্চগড়", 26.33, 88.55),
    City("ঠাকুরগাঁও", 26.03, 88.47),
    // Sylhet division (remaining).
    City("হবিগঞ্জ", 24.38, 91.42),
    City("মৌলভীবাজার", 24.48, 91.78),
    City("সুনামগঞ্জ", 25.07, 91.40),
)
