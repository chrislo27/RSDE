package io.github.chrislo27.rhre3.sfxdb

import com.fasterxml.jackson.annotation.JsonValue


enum class Language(@JsonValue val code: String, val langName: String?) {

    NONE("null", null), ENGLISH("en", "English"), JAPANESE("ja", "Japanese"), KOREAN("ko", "Korean"),
    SPANISH("es", "Spanish"), FRENCH("fr", "French"), ITALIAN("it", "Italian"), GERMAN("de", "German");

    companion object {
        val ALL_VALUES: List<Language> = values().toList()
        val VALID_VALUES: List<Language> = values().toList().filter { it != NONE }
        val CODE_MAP: Map<String, Language> = VALID_VALUES.associateBy { it.code }
    }

}