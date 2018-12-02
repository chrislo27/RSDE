package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.adt.RandomCue
import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.TextField


class RandomCueObjPane(editor: Editor, struct: RandomCue) : StructPane<RandomCue>(editor, struct) {

    override val idField = TextField(struct.id)
    override val nameField = TextField(struct.name)
    val deprecatedIDsField = ChipPane(FXCollections.observableArrayList(struct.deprecatedIDs.map { Chip(it) }))

    val responseIDsField = ChipPane(FXCollections.observableArrayList((struct.responseIDs ?: listOf()).map { Chip(it) }))

    init {
        titleLabel.text = struct.id

        addProperty(Label().bindLocalized("datamodel.type"), Label("randomCue").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs"), deprecatedIDsField)

        addProperty(Label().bindLocalized("datamodel.responseIDs"), responseIDsField)
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.OBJ_ID_STAR_SUB)
        validation.registerValidator(nameField, Validators.NAME_BLANK)
    }

}