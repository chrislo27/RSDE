package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.validation.Result
import io.github.chrislo27.rhre3.sfxdb.gui.DatabaseStatus
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.ExceptionAlert
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class WelcomePane(val app: RSDE) : BorderPane() {

    val centreBox: VBox = VBox().apply {
        this.alignment = Pos.CENTER
        this.id = "centre-vbox"
        this.styleClass += "spacing"
    }
    val leftBox: VBox = VBox().apply {
        this.alignment = Pos.TOP_LEFT
        this.id = "left-vbox"
        this.styleClass += "spacing"
    }

    val logo = ImageView("icon/256.png")
    val title = Label(RSDE.TITLE).apply { id = "title" }
    val version = Label(RSDE.VERSION.toString()).apply { id = "version-subtitle" }

    val recentProjectsView: ListView<String> = ListView()

    init {
        stylesheets += "style/welcomePane.css"

        center = centreBox
        left = leftBox

        VBox.setVgrow(recentProjectsView, Priority.ALWAYS)

        centreBox.children += logo
        centreBox.children += title
        centreBox.children += version

        // Spacing between version text and buttons
        centreBox.children += Pane().apply {
            id = "centre-pane"
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
                    }
                    centreBox.children += Button().apply {
                        this.bindLocalized("welcome.editExisting")
                        styleClass += "buttonWidth"
                        tooltip = Tooltip().bindLocalized("welcome.editExisting.tooltip")

                        onAction = EventHandler {
                            app.primaryStage.scene.root = EditExistingPane(app)
                        }
                    }

                    recentProjectsView.items.addAll("Detecting custom databased", "SFX is an incubating feature.")
                }

                // Recent projects
                leftBox.children += Label().apply {
                    id = "detected-custom-title"
                    this.bindLocalized("welcome.detectedCustom")
                    tooltip = Tooltip().bindLocalized("welcome.detectedCustom.tooltip")
                }
                recentProjectsView.disableProperty().value = true
                leftBox.children += recentProjectsView

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
                    recentProjectsView.disableProperty().value = false
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
                                        .ifPresent { exitProcess(0) }
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
                                        val id = if (gameObjId is Result.Success) gameObjId.value else "???"
                                        gameIdLabel.text = id
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
    }

}