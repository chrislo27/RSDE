package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.validation.L10NValidationSupport
import io.github.chrislo27.rhre3.sfxdb.validation.GameObject
import io.github.chrislo27.rhre3.sfxdb.validation.Struct
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox


abstract class StructPane<T : Struct>(val editor: Editor, val struct: T) : BorderPane() {

    val gameObject: GameObject get() = editor.gameObject

    val titleLabel: Label = Label().apply {
        id = "title"
    }
    val centreVbox: VBox = VBox()
    val gridPane: GridPane = GridPane().apply {
        styleClass += "grid-pane"
    }

    private var gridPaneRowIndex: Int = 0
    protected val validation = L10NValidationSupport()

    init {
        stylesheets += "style/datamodelPane.css"

        top = titleLabel
        center = ScrollPane(centreVbox).apply {
            this.hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            this.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }

        gridPane.maxWidth = Double.MAX_VALUE
        VBox.setVgrow(gridPane, Priority.ALWAYS)
        centreVbox.children += gridPane
    }

    protected fun addProperty(label: Node, control: Node): Int {
        gridPane.add(label, 0, gridPaneRowIndex)
        gridPane.add(control, 1, gridPaneRowIndex)
        return ++gridPaneRowIndex
    }

}