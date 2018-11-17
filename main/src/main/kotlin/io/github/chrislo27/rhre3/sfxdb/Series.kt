package io.github.chrislo27.rhre3.sfxdb

enum class Series(val jsonName: String) {
    OTHER("other"),
    TENGOKU("tengoku"), DS("ds"), FEVER("fever"), MEGAMIX("megamix"),
    SWITCH("switch"),
    SIDE("side");

    companion object {
        val VALUES = Series.values().toList()
    }
}
