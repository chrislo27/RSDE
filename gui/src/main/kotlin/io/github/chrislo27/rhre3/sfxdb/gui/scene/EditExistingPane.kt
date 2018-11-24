package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.adt.Game
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import io.github.chrislo27.rhre3.sfxdb.validation.Transformers
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Callback


class EditExistingPane(val app: RSDE) : BorderPane() {

    val games: ObservableList<Game> = FXCollections.observableArrayList(app.gameRegistry.gameMap.values.sortedBy { it.name })
    val gameListView: ListView<Game>
    val continueButton: Button
    val gameIDField: TextField
    val errorLabel: Label

    init {
        stylesheets += "style/editExisting.css"
        val bottom = BorderPane().apply {
            this@EditExistingPane.bottom = this
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
            this@EditExistingPane.top = this
            id = "top-hbox"
            BorderPane.setMargin(this, Insets(0.1.em))
        }
        val centre = HBox().apply {
            this@EditExistingPane.center = this
            id = "centre-hbox"
            BorderPane.setMargin(this, Insets(0.0, 2.0.em, 0.0, 2.0.em))
            alignment = Pos.CENTER
        }

        top.children += Label().apply {
            id = "title"
            bindLocalized("welcome.editExisting")
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
            onAction = EventHandler {
//                app.primaryStage.scene.root = WelcomePane(app)
            }
            alignment = Pos.CENTER_RIGHT
            isDisable = true
        }
        bottomRight.children += continueButton

        val gameSelBox = VBox().apply {
            id = "game-selector-box"
            BorderPane.setMargin(this, Insets(0.0, 2.0.em, 0.0, 2.0.em))
            alignment = Pos.CENTER
            maxWidth = Double.MAX_VALUE
        }
        gameSelBox.children += Label().apply {
            bindLocalized("editExisting.selectBaseLabel")
            styleClass += "search-related"
            maxWidth = Double.MAX_VALUE
        }
        gameListView = ListView(games).apply {
            styleClass += "search-related"
            id = "search-list"
            maxWidth = Double.MAX_VALUE
            cellFactory = Callback { GameCell() }
        }
        val searchBar = TextField().apply {
            this.promptText = Localization["editExisting.search"]
            styleClass += "search-related"
            maxWidth = Double.MAX_VALUE
            textProperty().addListener { observable, oldValue, newValue ->
                val query = newValue.toLowerCase()
                gameListView.items = games.filtered { game -> query in game.name.toLowerCase() || query in game.id.toLowerCase() || game.searchHints?.any { query in it.toLowerCase() } == true }
            }
        }
        gameSelBox.children += searchBar
        gameSelBox.children += gameListView

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
            gameListView.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
                Platform.runLater {
                    this.text = newValue?.id ?: ""
                }
            }
            isDisable = true
        }
        idSelBox.children += gameIDField
        errorLabel = Label().apply {
            styleClass += "searchRelated"
            maxWidth = Double.MAX_VALUE
            wrapTextProperty().value = true
            id = "error-label"
        }
        idSelBox.children += errorLabel

        centre.children += gameSelBox
        centre.children += Label("➡").apply {
            styleClass += "arrow-label"
        }
        centre.children += idSelBox
    }

    init {
        gameListView.selectionModel.selectedItems.addListener(ListChangeListener {
            it.next()
            if (it.list.isEmpty()) {
                gameIDField.text = ""
            }
            gameIDField.isDisable = it.list.isEmpty()
        })
        gameIDField.textProperty().addListener { observable, oldValue, newValue ->
            var failed = true
            if (newValue.isBlank()) {
                errorLabel.text = ""
            } else if (gameListView.selectionModel.selectedItems.isEmpty()) {
                errorLabel.text = Localization["editExisting.warning.pickBaseFirst"]
            } else if (!Transformers.GAME_ID_REGEX.matches(newValue.trim())) {
                errorLabel.text = Localization["editExisting.warning.illegalID"]
            } else if (RSDE.customSFXFolder.resolve(newValue.trim()).exists()) {
                errorLabel.text = Localization["editExisting.warning.folderExists"]
            } else {
                failed = false
                errorLabel.text = ""
            }

            continueButton.isDisable = failed && gameListView.selectionModel.selectedItems.isNotEmpty()
        }
    }

    inner class GameCell : ListCell<Game>() {
        override fun updateItem(item: Game?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty || item == null) {
                graphic = null
                text = null
            } else {
                text = item.name
                graphic = ImageView(app.gameRegistry.gameIconMap[item] ?: app.gameRegistry.missingIconImage)
            }
        }
    }

}