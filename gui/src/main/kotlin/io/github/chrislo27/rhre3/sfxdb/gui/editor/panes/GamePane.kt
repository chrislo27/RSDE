package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.validation.orElse


class GamePane(editor: Editor) : DatamodelPane(editor) {

    init {
        titleLabel.text = gameObject.id.orElse("??? GAME ID MISSING ???")
    }

}