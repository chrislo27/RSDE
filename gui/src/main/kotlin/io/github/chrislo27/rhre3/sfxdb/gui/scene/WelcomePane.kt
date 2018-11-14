package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.Separator
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

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

        val prefButtonWidth = 350.0
        centreBox.children += Button().apply {
            this.bindLocalized("welcome.createNewEngine")
            this.prefWidth = prefButtonWidth
            this.disableProperty().value = true
        }
        centreBox.children += Button().apply {
            this.bindLocalized("welcome.editEngine")
            this.prefWidth = prefButtonWidth
            this.disableProperty().value = true
        }
        centreBox.children += Separator(Orientation.HORIZONTAL).apply {
            this.maxWidth = prefButtonWidth
            requestFocus()
        }
        centreBox.children += Button().apply {
            this.bindLocalized("welcome.createNewRemix")
            this.prefWidth = prefButtonWidth
            this.disableProperty().value = true
        }
        centreBox.children += Button().apply {
            this.bindLocalized("welcome.editRemix")
            this.prefWidth = prefButtonWidth
            this.disableProperty().value = true
        }

        // Recent projects
        leftBox.children += Label().apply {
            id = "recently-opened-title"
            this.bindLocalized("welcome.recentlyOpened")
        }
        leftBox.children += recentProjectsView

        // FIXME temp
        recentProjectsView.items.addAll(*(1..32).map { "Project $it" }.toTypedArray())
    }

}