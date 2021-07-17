package io.github.chrislo27.rhre3.sfxdb.gui.util


object Credits {

    fun generateList(): List<Credit> =
        listOf(
            "programming" crediting listOf("chrislo27"),
            "earlyAccess" crediting listOf("Draster", "Lvl100Feraligatr"),
            "specialThanks" crediting listOf("Kievit")
        )

    private infix fun String.crediting(persons: List<String>): Credit =
        Credit(this, persons)

    data class Credit(val type: String, val persons: List<String>) {
        val localization: String by lazy {
            "credits.$type"
        }
    }

}