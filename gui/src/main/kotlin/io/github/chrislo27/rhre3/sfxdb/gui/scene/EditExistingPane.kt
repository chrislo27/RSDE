package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.adt.Game
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import javafx.application.Platform
import javafx.collections.FXCollections
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

    init {
        stylesheets += "style/editExisting.css"
        val bottom = HBox().apply {
            this@EditExistingPane.bottom = this
            id = "bottom-hbox"
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
        bottom.children += backButton

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
        val listView = ListView(games).apply {
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
                listView.items = games.filtered { game -> query in game.name.toLowerCase() || query in game.id.toLowerCase() || game.searchHints?.any { query in it.toLowerCase() } == true }
            }
        }
        gameSelBox.children += searchBar
        gameSelBox.children += listView

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
        val gameIDField = TextField().apply {
            styleClass += "search-related"
            maxWidth = Double.MAX_VALUE
            listView.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
                Platform.runLater {
                    this.text = newValue?.id ?: ""
                }
            }
        }
        idSelBox.children += gameIDField

        centre.children += gameSelBox
        centre.children += Label("➡").apply {
            styleClass += "arrow-label"
        }
        centre.children += idSelBox
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