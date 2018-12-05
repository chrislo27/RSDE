package io.github.chrislo27.rhre3.sfxdb.gui.util


object LibrariesUsed {
    data class Library(val name: String, val website: String)

    val libraries: List<Library> = listOf(
        Library("Kotlin", "https://kotlinlang.org/"),
        Library("Jackson", "https://github.com/FasterXML/jackson"),
        Library("Apache Log4j", "https://logging.apache.org/log4j/2.x/"),
        Library("java-discord-rpc", "https://github.com/MinnDevelopment/java-discord-rpc"),
        Library("ControlsFX", "http://fxexperience.com/controlsfx/"),
        Library("EasyBind", "https://github.com/TomasMikula/EasyBind")
    ).sortedBy { it.name }
}