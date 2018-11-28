package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.validation.GameObject
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox


abstract class DatamodelPane(val editor: Editor) : BorderPane() {

    val gameObject: GameObject get() = editor.gameObject
    val titleLabel: Label = Label().apply {
        id = "title"
    }

    val centreVbox: VBox = VBox()

    init {
        stylesheets += "style/datamodelPane.css"

        top = titleLabel
        center = ScrollPane(centreVbox).apply {
            this.hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            this.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }
    }

}
