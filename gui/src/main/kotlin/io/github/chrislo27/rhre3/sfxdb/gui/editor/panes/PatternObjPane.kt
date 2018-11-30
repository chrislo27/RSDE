package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.validation.PatternObject
import io.github.chrislo27.rhre3.sfxdb.validation.orElse
import javafx.collections.FXCollections
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField


class PatternObjPane(editor: Editor, struct: PatternObject) : StructPane<PatternObject>(editor, struct) {

    val idField = TextField(struct.id.orElse("??? MISSING ID ???"))
    val nameField = TextField(struct.name.orElse("MISSING NAME"))
    val deprecatedIDsField = ChipPane(FXCollections.observableArrayList(struct.deprecatedIDs.orElse(listOf()).map { Chip(it) }))

    val stretchableField = CheckBox().apply { this.isSelected = struct.stretchable.orElse(false) }

    init {
        titleLabel.text = struct.id.orElse("??? ID MISSING ???")

        addProperty(Label().bindLocalized("datamodel.type"), Label("pattern").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs"), deprecatedIDsField)

        addProperty(Label().bindLocalized("datamodel.stretchable"), stretchableField)
    }

}