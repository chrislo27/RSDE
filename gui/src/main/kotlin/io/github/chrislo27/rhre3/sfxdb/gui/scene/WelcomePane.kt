package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.adt.Result
import io.github.chrislo27.rhre3.sfxdb.gui.DatabaseStatus
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import javafx.application.Platform
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

class WelcomePane(val app: RSDE) : BorderPane() {

    companion object {
        private const val SPACING = 4.0
    }

    val centreBox: VBox = VBox(SPACING).apply {
        this.alignment = Pos.CENTER
        this.id = "centre-vbox"
    }
    val leftBox: VBox = VBox(SPACING).apply {
        this.alignment = Pos.TOP_LEFT
        this.id = "left-vbox"
    }

    val logo = ImageView("icon/256.png")
    val title = Label(RSDE.TITLE).apply { id = "title" }
    val version = Label(RSDE.VERSION.toString()).apply { id = "version-subtitle" }

    val recentProjectsView: ListView<String> = ListView()

    init {
        center = centreBox
        left = leftBox

        VBox.setVgrow(recentProjectsView, Priority.ALWAYS)

        centreBox.children += logo
        centreBox.children += title
        centreBox.children += version

        // Spacing between version text and buttons
        centreBox.children += Pane().apply {
            prefHeight = SPACING * 3
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
                val prefButtonWidth = 350.0

                fun addStartButtons() {
                    centreBox.children += Button().apply {
                        this.bindLocalized("welcome.startNewGame")
                        this.prefWidth = prefButtonWidth
                    }
                    centreBox.children += Button().apply {
                        this.bindLocalized("welcome.makeChanges")
                        this.prefWidth = prefButtonWidth
                    }
//                    centreBox.children += Separator(Orientation.HORIZONTAL).apply {
//                        this.maxWidth = prefButtonWidth
//                        requestFocus()
//                    }

                    recentProjectsView.items.addAll(*"eduardo diego josé francisco de paula juan nepomuceno maría de los remedios cipriano de la santísima trinidad ruiz y picasso".split(" ").toTypedArray())
                }

                // Recent projects
                leftBox.children += Label().apply {
                    id = "detected-custom-title"
                    this.bindLocalized("welcome.detectedCustom")
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
                    prefWidth = prefButtonWidth
                }

                centreBox.children += gameIdLabel
                centreBox.children += progressLabel
                centreBox.children += progressBar

                // Start loading DB
                GlobalScope.launch {
                    app.gameRegistry.loadSFXFolder { gameObject, loaded, total ->
                        if (loaded == total) {
                            // Done
                            Platform.runLater {
                                centreBox.children -= gameIdLabel
                                centreBox.children -= progressLabel
                                centreBox.children -= progressBar
                                addStartButtons()
                                recentProjectsView.disableProperty().value = false
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
            DatabaseStatus.ERROR -> {
                centreBox.children += Label().apply {
                    this.bindLocalized("welcome.error")
                    this.textAlignment = TextAlignment.CENTER
                }
            }
        }
    }

}