package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import javafx.scene.layout.BorderPane


class EditorPane(val app: RSDE) : BorderPane() {

    init {
        stylesheets += "style/editor.css"
    }

}