package io.github.chrislo27.rhre3.sfxdb

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

enum class Series(@get:JsonValue val jsonName: String, @JsonIgnore val properName: String) {

    OTHER("other", ""),
    TENGOKU("tengoku", "Tengoku"), DS("ds", "DS"), FEVER("fever", "Fever"), MEGAMIX("megamix", "Megamix"),
    SWITCH("switch", ""),
    SIDE("side", "");

    companion object {
        val VALUES = Series.values().toList()

        @JvmStatic
        @JsonCreator
        fun getEnumFromJsonName(n: String): Series = VALUES.first { n.toLowerCase() == it.jsonName.toLowerCase() }
    }
}
