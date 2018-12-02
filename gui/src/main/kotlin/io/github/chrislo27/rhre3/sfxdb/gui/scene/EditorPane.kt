package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.discord.ChangesPresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DefaultRichPresence
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DiscordHelper
import io.github.chrislo27.rhre3.sfxdb.gui.discord.PresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.editor.StructurePane
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import javafx.application.Platform
import javafx.geometry.Side
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox


class EditorPane(val app: RSDE) : BorderPane(), ChangesPresenceState {

    val toolbar: MenuBar
    val centreTabPane: TabPane = TabPane().apply {
        this.side = Side.TOP
    }
    //    val leftTabPane: TabPane = TabPane().apply {
//        this.side = Side.LEFT
//    }
//    val rightTabPane: TabPane = TabPane().apply {
//        this.side = Side.RIGHT
//    }
    val bottomPane: Pane = VBox()

    val editors: List<Editor> = mutableListOf()
    val currentEditor: Editor?
        get() {
            val currentTab = centreTabPane.selectionModel.selectedItem ?: return null
            return editors.find { it.tab == currentTab }
        }

    val structurePane: StructurePane

    init {
        stylesheets += "style/editorPane.css"

        toolbar = MenuBar()
        top = toolbar
        center = centreTabPane
//        left = leftTabPane
//        right = rightTabPane
        bottom = bottomPane

        structurePane = StructurePane(this)
        left = structurePane

        toolbar.menus += Menu().bindLocalized("editor.toolbar.file").apply {
            items += MenuItem().bindLocalized("editor.toolbar.file.welcomeScreen").apply {
                setOnAction {
                    app.primaryStage.scene.root = WelcomePane(app)
                }
            }
            items += MenuItem().bindLocalized("editor.toolbar.file.save")
            items += MenuItem().bindLocalized("editor.toolbar.file.export")
        }

        centreTabPane.selectionModel.selectedItemProperty().addListener { _, oldValue, newValue ->
            if (oldValue != newValue) {
                DiscordHelper.updatePresence(getPresenceState())

                fireUpdate()
            }
        }

        Platform.runLater {
            fireUpdate()
        }
    }

    fun fireUpdate() {
        structurePane.update(currentEditor)
        editors.forEach { it.update() }
    }

    override fun getPresenceState(): DefaultRichPresence {
        return PresenceState.InEditor(currentEditor?.folder?.name).toRichPresenceObj()
    }

    fun addEditor(editor: Editor) {
        editors as MutableList
        editors += editor
        centreTabPane.tabs += editor.tab
        fireUpdate()
    }

    fun removeEditor(editor: Editor) {
        editors as MutableList
        editors -= editor
        centreTabPane.tabs -= editor.tab
        fireUpdate()
    }

}