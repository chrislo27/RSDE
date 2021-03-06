package io.github.chrislo27.rhre3.sfxdb.gui

import io.github.chrislo27.rhre3.sfxdb.gui.discord.DiscordHelper
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import java.io.File


class Settings(val app: RSDE) {

    val prefsFolder: File = RSDE.rootFolder.resolve("prefs/").apply {
        mkdirs()
    }
    val prefsFile: File = prefsFolder.resolve("prefs.json")

    val nightModeProperty = SimpleBooleanProperty(false)
    var nightMode: Boolean
        get() = nightModeProperty.value
        set(value) = nightModeProperty.set(value)
    val dividerPositionProperty = SimpleDoubleProperty(0.3)
    var dividerPosition: Double
        get() = dividerPositionProperty.value
        set(value) = dividerPositionProperty.set(value)
    val richPresenceProperty = SimpleBooleanProperty(true)
    var richPresence: Boolean
        get() = richPresenceProperty.value
        set(value) = richPresenceProperty.set(value)

    init {
        richPresenceProperty.addListener { _, _, newValue ->
            DiscordHelper.enabled = newValue
        }
    }

    fun loadFromStorage() {
        if (!prefsFile.exists()) return
        try {
            val obj = JsonHandler.OBJECT_MAPPER.readTree(prefsFile)

            nightMode = obj["nightMode"]?.asBoolean(false) ?: false
            dividerPosition = obj["dividerPosition"]?.asDouble(0.3)?.coerceIn(0.0, 1.0) ?: 0.3
            richPresence = obj["discordRichPresence"]?.asBoolean(true) ?: true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun persistToStorage() {
        prefsFile.createNewFile()
        val json = JsonHandler.OBJECT_MAPPER.createObjectNode()

        json.put("nightMode", nightMode)
        json.put("dividerPosition", dividerPosition)
        json.put("discordRichPresence", richPresence)

        prefsFile.writeText(JsonHandler.OBJECT_MAPPER.writeValueAsString(json))
    }

}