package io.github.chrislo27.rhre3.sfxdb

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue


enum class BaseBpmRules(@get:JsonValue val jsonName: String, @JsonIgnore val properName: String) {

    ALWAYS("always", "Always"),
    NO_TIME_STRETCH("noTimeStretch", "Only when time stretching isn't available"),
    ONLY_TIME_STRETCH("onlyTimeStretch", "Only when time stretching is available");

    companion object {
        val VALUES: List<BaseBpmRules> = values().toList()
        val MAP: Map<String, BaseBpmRules> = VALUES.associateBy { it.jsonName }

        @JvmStatic
        @JsonCreator
        fun getEnumFromJsonName(id: String): BaseBpmRules = MAP.getValue(id)
    }
}
