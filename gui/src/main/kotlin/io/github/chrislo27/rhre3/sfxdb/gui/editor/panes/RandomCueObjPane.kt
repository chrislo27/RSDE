package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.validation.RandomCueObject
import io.github.chrislo27.rhre3.sfxdb.validation.orElse
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.TextField


class RandomCueObjPane(editor: Editor, struct: RandomCueObject) : StructPane<RandomCueObject>(editor, struct) {

    val idField = TextField(struct.id.orElse("??? MISSING ID ???"))
    val nameField = TextField(struct.name.orElse("MISSING NAME"))
    val deprecatedIDsField = ChipPane(FXCollections.observableArrayList(struct.deprecatedIDs.orElse(listOf()).map { Chip(it) }))

    val responseIDsField = ChipPane(FXCollections.observableArrayList(struct.responseIDs.orElse(listOf()).map { Chip(it) }))

    init {
        titleLabel.text = struct.id.orElse("??? ID MISSING ???")

        addProperty(Label().bindLocalized("datamodel.type"), Label("randomCue").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs"), deprecatedIDsField)

        addProperty(Label().bindLocalized("datamodel.responseIDs"), responseIDsField)
    }

}