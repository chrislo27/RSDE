package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.SoundFileExtensions
import io.github.chrislo27.rhre3.sfxdb.adt.Cue
import io.github.chrislo27.rhre3.sfxdb.adt.Game
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.discord.ChangesPresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DefaultRichPresence
import io.github.chrislo27.rhre3.sfxdb.gui.discord.PresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.*
import io.github.chrislo27.rhre3.sfxdb.validation.Transformers
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.io.File


class StartNewPane(val app: RSDE) : BorderPane(), ChangesPresenceState {

    val continueButton: Button
    val gameIDField: TextField
    val errorLabel: Label
    val addCues: CheckBox

    init {
        stylesheets += "style/editExisting.css"
        stylesheets += "style/startNew.css"
        val bottom = BorderPane().apply {
            this@StartNewPane.bottom = this
            id = "bottom-box"
//            BorderPane.setMargin(this, Insets(0.1.em))
        }
        val bottomLeft = HBox().apply {
            bottom.left = this
            BorderPane.setMargin(this, Insets(0.1.em))
        }
        val bottomRight = HBox().apply {
            bottom.right = this
            BorderPane.setMargin(this, Insets(0.1.em))
        }
        val top = HBox().apply {
            this@StartNewPane.top = this
            id = "top-hbox"
            BorderPane.setMargin(this, Insets(0.1.em))
        }
        val centre = HBox().apply {
            this@StartNewPane.center = this
            id = "centre-hbox"
            BorderPane.setMargin(this, Insets(0.0, 2.0.em, 0.0, 2.0.em))
            alignment = Pos.CENTER
        }

        top.children += Label().apply {
            id = "title"
            bindLocalized("welcome.startNewGame")
        }

        val backButton = Button().apply {
            bindLocalized("opts.back")
            onAction = EventHandler {
                app.primaryStage.scene.root = WelcomePane(app)
            }
            alignment = Pos.CENTER_LEFT
        }
        bottomLeft.children += backButton
        continueButton = Button().apply {
            bindLocalized("opts.continue")
            alignment = Pos.CENTER_RIGHT
            isDisable = true
        }
        bottomRight.children += continueButton

        val idSelBox = VBox().apply {
            id = "id-selector-box"
            BorderPane.setMargin(this, Insets(0.0, 2.0.em, 0.0, 2.0.em))
            alignment = Pos.CENTER
            maxWidth = Double.MAX_VALUE
        }
        idSelBox.children += Label().apply {
            bindLocalized("editExisting.chooseGameID")
            styleClass += "search-related"
            maxWidth = Double.MAX_VALUE
            wrapTextProperty().value = true
            tooltip = Tooltip().bindLocalized("editExisting.chooseGameID.tooltip")
        }
        gameIDField = TextField().apply {
            styleClass += "search-related"
            maxWidth = Double.MAX_VALUE
        }
        idSelBox.children += gameIDField
        errorLabel = Label().apply {
            styleClass += "searchRelated"
            maxWidth = Double.MAX_VALUE
            wrapTextProperty().value = true
            id = "error-label"
        }
        idSelBox.children += errorLabel

        val optionsBox = VBox().apply {
            id = "options-box"
            BorderPane.setMargin(this, Insets(0.0, 2.0.em, 0.0, 2.0.em))
            alignment = Pos.CENTER
            maxWidth = Double.MAX_VALUE
        }
        addCues = CheckBox().bindLocalized("startNew.addCues").apply {
            isSelected = true
            isWrapText = true
        }
        optionsBox.children += addCues

        centre.children += idSelBox
        centre.children += Label("➡").apply {
            styleClass += "arrow-label"
        }
        centre.children += optionsBox
    }

    override fun getPresenceState(): DefaultRichPresence {
        return PresenceState.PreparingNewDef.toRichPresenceObj()
    }

    init {
        gameIDField.textProperty().addListener { _, _, newValue ->
            var failed = true
            val result = GameIDResult.processGameID(newValue)
            when (result) {
                GameIDResult.SUCCESS -> {
                    failed = false
                    errorLabel.text = ""
                }
                GameIDResult.BLANK -> errorLabel.text = ""
                GameIDResult.ILLEGAL -> errorLabel.text = Localization["editExisting.warning.illegalID"]
                GameIDResult.DATAJSON_EXISTS -> errorLabel.text = Localization["startNew.warning.jsonExists"]
            }

            continueButton.isDisable = failed
        }
        continueButton.setOnAction { _ ->
            continueButton.isDisable = true

            try {
                val id = gameIDField.text
                val folder = RSDE.customSFXFolder.resolve(id.trim())
                folder.mkdirs()
                val jsonFile = folder.resolve("data.json")
                val game = Game(id, id, Series.OTHER, objects = mutableListOf())

                if (addCues.isSelected) {
                    folder.listFiles { f: File -> f.extension in SoundFileExtensions.VALUES.map { it.fileExt } }.forEach { f ->
                        game.objects.add(Cue("*/" + f.nameWithoutExtension, f.nameWithoutExtension, mutableListOf(), 0f, fileExtension = f.extension))
                    }
                }

                JsonHandler.OBJECT_MAPPER.writeValue(jsonFile, game)

                app.switchToEditorPane {
                    addEditor(Editor(folder, this))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ExceptionAlert(e).showAndWait()
            }
        }
    }

    enum class GameIDResult {
        SUCCESS, BLANK, ILLEGAL, DATAJSON_EXISTS;

        companion object {
            fun processGameID(id: String): GameIDResult {
                return when {
                    id.isBlank() -> BLANK
                    !Transformers.GAME_ID_REGEX.matches(id.trim()) -> ILLEGAL
                    RSDE.customSFXFolder.resolve(id.trim()).resolve("data.json").exists() -> DATAJSON_EXISTS
                    else -> SUCCESS
                }
            }
        }
    }

}