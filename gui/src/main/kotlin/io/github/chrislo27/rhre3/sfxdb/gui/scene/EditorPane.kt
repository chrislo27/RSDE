package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import javafx.scene.control.ToolBar
import javafx.scene.layout.BorderPane


class EditorPane(val app: RSDE) : BorderPane() {

    val toolbar: ToolBar

    init {
        stylesheets += "style/editor.css"

        toolbar = ToolBar()
        top = toolbar

    }

}