package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.adt.CuePointer
import io.github.chrislo27.rhre3.sfxdb.adt.RandomCue
import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.Tooltip


class RandomCueObjPane(editor: Editor, struct: RandomCue) : MultipartStructPane<RandomCue>(editor, struct) {

    val responseIDsField = ChipPane(FXCollections.observableArrayList((struct.responseIDs ?: listOf()).map { Chip(it) }))
    override val cuesPane: CuesPane<RandomCue> = CuesPane(this) { pointer, pane -> RandomCueCuePointerPane(pointer, pane) }

    init {
        titleLabel.text = struct.id

        addProperty(Label().bindLocalized("datamodel.type"), Label("randomCue").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs"), deprecatedIDsField)

        addProperty(Label().bindLocalized("datamodel.responseIDs"), responseIDsField)

        centreVbox.children += cuesPane
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.OBJ_ID_STAR_SUB)
        validation.registerValidator(nameField, Validators.NAME_BLANK)
    }

    class RandomCueCuePointerPane(cuePointer: CuePointer, parent: CuesPane<RandomCue>) : CuePointerPane<RandomCue>(parent, cuePointer) {
        init {
            addProperty(Label().bindLocalized("cuePointer.id"), idField)
            addProperty(Label().bindLocalized("cuePointer.duration").apply {
                tooltip = Tooltip().bindLocalized("cuePointer.duration.tooltip")
            }, durationField)
            addProperty(Label().bindLocalized("cuePointer.semitone"), semitoneField)
            addProperty(Label().bindLocalized("cuePointer.track"), trackField)
            addProperty(Label().bindLocalized("cuePointer.volume"), volumeField)
        }
    }

}