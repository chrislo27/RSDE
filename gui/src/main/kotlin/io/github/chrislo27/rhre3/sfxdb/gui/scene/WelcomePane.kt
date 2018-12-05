package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.gui.DatabaseStatus
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.discord.ChangesPresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DefaultRichPresence
import io.github.chrislo27.rhre3.sfxdb.gui.discord.PresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.registry.GameRegistry
import io.github.chrislo27.rhre3.sfxdb.gui.util.ExceptionAlert
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.validation.Transformers
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.scene.text.TextAlignment
import javafx.util.Callback
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess

class WelcomePane(val app: RSDE) : BorderPane(), ChangesPresenceState {

    data class CustomSFX(val folder: File, val valid: Boolean) {
        val icon: Image by lazy { folder.resolve("icon.png").takeIf { it.exists() }?.let { Image("file:" + it.path, 32.0, 32.0, false, true) } ?: GameRegistry.missingIconImage }
        var isEmpty: Boolean = false
    }

    val EMPTY_CUSTOM_SFX: CustomSFX by lazy { CustomSFX(File(""), true).apply { isEmpty = true } }

    val centrePane = BorderPane().apply {
        this.styleClass += "spacing"
    }
    val centreBox = VBox().apply {
        this.alignment = Pos.CENTER
        this.id = "centre-box"
        this.styleClass += "spacing"
        centrePane.center = this
    }
    val leftBox: VBox = VBox().apply {
        this.alignment = Pos.TOP_LEFT
        this.id = "left-vbox"
        this.styleClass += "spacing"
    }

    val logo = ImageView("icon/256.png")
    val title = Label(RSDE.TITLE).apply { id = "title" }
    val version = Label(RSDE.VERSION.toString()).apply { id = "version-subtitle" }
    val sfxdbLabel: Label

    val customSfxList: ObservableList<CustomSFX> = FXCollections.observableArrayList()
    val customSfxView: ListView<CustomSFX> = ListView(customSfxList).apply {
        cellFactory = Callback { CustomSFXCell(app) }
    }

    init {
        stylesheets += "style/welcomePane.css"

        center = centrePane
        left = leftBox

        VBox.setVgrow(customSfxView, Priority.ALWAYS)

        centreBox.children += logo
        centreBox.children += title
        centreBox.children += version

        // Spacing between version text and buttons
        centreBox.children += Pane().apply {
            id = "centre-pane"
        }

        sfxdbLabel = Label().bindLocalized("welcome.dbBranch", RSDE.SFX_DB_BRANCH).apply {
            this.textAlignment = TextAlignment.RIGHT
            this.alignment = Pos.CENTER_RIGHT
            this.id = "db-version"
        }
        centrePane.bottom = HBox().apply {
            this.alignment = Pos.BASELINE_RIGHT
            children += sfxdbLabel
        }

        when (app.databasePresent) {
            DatabaseStatus.DOES_NOT_EXIST -> {
                centreBox.children += Label().apply {
                    this.bindLocalized("welcome.noDatabase")
                    this.textAlignment = TextAlignment.CENTER
                }
                centreBox.children += Hyperlink(RSDE.RHRE_GITHUB).apply {
                    setOnAction {
                        app.hostServices.showDocument(RSDE.RHRE_GITHUB)
                    }
                }
            }
            DatabaseStatus.INCOMPATIBLE -> {
                centreBox.children += Label().apply {
                    this.bindLocalized("welcome.databaseTooNew")
                    this.textAlignment = TextAlignment.CENTER
                }
                centreBox.children += Hyperlink(RSDE.RHRE_GITHUB).apply {
                    setOnAction {
                        app.hostServices.showDocument(RSDE.RHRE_GITHUB)
                    }
                }
            }
            DatabaseStatus.EXISTS -> {
                fun addStartButtons() {
                    centreBox.children += Button().apply {
                        this.bindLocalized("welcome.startNewGame")
                        styleClass += "buttonWidth"
                        tooltip = Tooltip().bindLocalized("welcome.startNewGame.tooltip")

                        onAction = EventHandler {
                            // TODO
                        }
                        isDisable = true
                    }
                    centreBox.children += Button().apply {
                        this.bindLocalized("welcome.editExisting")
                        styleClass += "buttonWidth"
                        tooltip = Tooltip().bindLocalized("welcome.editExisting.tooltip")

                        onAction = EventHandler {
                            app.primaryStage.scene.root = EditExistingPane(app)
                        }
                    }
                    customSfxList += EMPTY_CUSTOM_SFX
                }

                // Recent projects
                leftBox.children += Label().apply {
                    id = "detected-custom-title"
                    this.bindLocalized("welcome.detectedCustom")
                    tooltip = Tooltip().bindLocalized("welcome.detectedCustom.tooltip")
                }
                customSfxView.disableProperty().value = true
                leftBox.children += customSfxView

                val gameIdLabel = Label("").apply {
                    this.textAlignment = TextAlignment.CENTER
                }
                val progressLabel = Label("").apply {
                    this.textAlignment = TextAlignment.CENTER
                }
                val progressBar = ProgressBar().apply {
                    id = "progress-bar"
                }

                centreBox.children += gameIdLabel
                centreBox.children += progressLabel
                centreBox.children += progressBar

                fun removeLoadingElements() {
                    centreBox.children -= gameIdLabel
                    centreBox.children -= progressLabel
                    centreBox.children -= progressBar
                }

                fun finishLoading() {
                    removeLoadingElements()
                    addStartButtons()
                    val registry = app.gameRegistry
                    sfxdbLabel.bindLocalized("welcome.sfxDatabase", registry.version, registry.editorVersion, RSDE.SFX_DB_BRANCH)

                    // Add detected custom SFX
                    GlobalScope.launch {
                        RSDE.customSFXFolder.listFiles { file ->
                            file.isDirectory && file.name.matches(Transformers.GAME_ID_REGEX) && file.resolve("data.json").exists()
                        }.forEach { file ->
                            val parsedSuccessfully = try {
                                Parser.parseGameDefinition(JsonHandler.OBJECT_MAPPER.readTree(file.resolve("data.json"))).producePerfectADT()
                                true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                false
                            }
                            Platform.runLater {
                                customSfxList.add(CustomSFX(file, parsedSuccessfully))
                            }
                        }
                        Platform.runLater {
                            customSfxList.remove(EMPTY_CUSTOM_SFX)
                            customSfxView.disableProperty().value = false
                        }
                    }
                }

                if (!app.gameRegistry.isLoaded) {
                    // Start loading DB
                    GlobalScope.launch {
                        app.gameRegistry.loadSFXFolder { gameResult, loaded, total ->
                            if (gameResult.isFailure) {
                                Platform.runLater {
                                    removeLoadingElements()
                                    centreBox.children += Label().apply {
                                        this.bindLocalized("welcome.error")
                                        this.textAlignment = TextAlignment.CENTER
                                    }
                                    ExceptionAlert(gameResult.exceptionOrNull()!!, Localization["welcome.error"])
                                        .showAndWait()
                                    exitProcess(0)
                                }
                            } else {
                                val gameObject = gameResult.getOrNull()!!
                                if (loaded == total) {
                                    // Done
                                    Platform.runLater {
                                        finishLoading()
                                    }
                                } else {
                                    Platform.runLater {
                                        val gameObjId = gameObject.id
                                        gameIdLabel.text = gameObjId
                                        progressLabel.text = "$loaded / $total"
                                        progressBar.progress = loaded.toDouble() / total.coerceAtLeast(1)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    finishLoading()
                }
            }
            DatabaseStatus.ERROR -> {
                centreBox.children += Label().apply {
                    this.bindLocalized("welcome.error")
                    this.textAlignment = TextAlignment.CENTER
                }
            }
        }

        val secret = "sansundertale"
        val secretStyle = "style/sans.css"
        val entry = mutableListOf<Char>()
        setOnKeyTyped { evt ->
            entry.add(evt.character[0])
            if (entry.joinToString(separator = "") != secret.substring(0, entry.size)) {
                entry.clear()
            }
            if (entry.size >= secret.length) {
                if (secretStyle in scene.stylesheets) {
                    scene.stylesheets -= secretStyle
                } else {
                    scene.stylesheets += secretStyle
                }
                entry.clear()
            }
        }
    }

    override fun getPresenceState(): DefaultRichPresence {
        return PresenceState.WelcomeScreen.toRichPresenceObj()
    }

    class CustomSFXCell(val app: RSDE) : ListCell<CustomSFX>() {
        init {
            setOnMouseClicked { mouseEvent ->
                val item = this@CustomSFXCell.item ?: return@setOnMouseClicked
                if (mouseEvent.button == MouseButton.PRIMARY && mouseEvent.clickCount >= 2) {
                    app.switchToEditorPane {
                        val sameFolder = this.editors.firstOrNull { it.folder == item.folder }
                        if (sameFolder == null) {
                            val newEditor = Editor(item.folder, this)
                            addEditor(newEditor)
                            this.centreTabPane.selectionModel.select(newEditor.tab)
                        } else {
                            this.centreTabPane.selectionModel.select(sameFolder.tab)
                        }
                    }
                }
            }
        }

        override fun updateItem(item: CustomSFX?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                text = ""
                graphic = null
            } else {
                if (item.valid) {
                    styleClass -= "bad-custom-sfx"
                } else {
                    styleClass += "bad-custom-sfx"
                }
                if (item.isEmpty) {
                    graphic = ProgressIndicator()
                    text = Localization["welcome.loadingCustoms"]
                } else {
                    text = item.folder.name
                    graphic = ImageView(item.icon)
                }
            }
        }
    }

}