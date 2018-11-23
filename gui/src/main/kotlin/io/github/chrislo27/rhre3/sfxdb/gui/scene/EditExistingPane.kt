package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox


class EditExistingPane(val app: RSDE) : BorderPane() {

    init {
        stylesheets += "style/editExisting.css"
        val bottom = HBox().apply {
            this@EditExistingPane.bottom = this
            id = "bottom-hbox"
            BorderPane.setMargin(this, Insets(1.0.em))
        }

        val backButton = Button().apply {
            bindLocalized("opts.back")
            onAction = EventHandler {
                app.primaryStage.scene.root = WelcomePane(app)
            }
            alignment = Pos.CENTER_LEFT
        }

        bottom.children += backButton
    }

}