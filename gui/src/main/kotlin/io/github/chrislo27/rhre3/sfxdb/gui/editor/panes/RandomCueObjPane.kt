package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.adt.CuePointer
import io.github.chrislo27.rhre3.sfxdb.adt.RandomCue
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.ui.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.ui.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.control.Label
import javafx.scene.control.Tooltip


class RandomCueObjPane(editor: Editor, struct: RandomCue) : MultipartStructPane<RandomCue>(editor, struct) {

    val responseIDsField = ChipPane(FXCollections.observableArrayList((struct.responseIDs ?: listOf()).map { Chip(it) }))
    override val cuesPane: CuesPane<RandomCue> = CuesPane(this) { pointer, pane -> RandomCueCuePointerPane(pointer, pane) }

    init {
        addProperty(Label().bindLocalized("datamodel.type"), Label("randomCue").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs").apply {
            tooltip = Tooltip().bindLocalized("datamodel.deprecatedIDs.tooltip")
        }, deprecatedIDsField)

        addProperty(Label().bindLocalized("datamodel.responseIDs").apply {
            tooltip = Tooltip().bindLocalized("datamodel.responseIDs.tooltip")
        }, responseIDsField)

        centreVbox.children += cuesPane
    }

    init {
        // Bind to struct
        responseIDsField.list.addListener(ListChangeListener { evt ->
            val list = mutableListOf<String>()
            while (evt.next()) {
                list.addAll(evt.list.map { chip -> chip.label.text })
            }
            struct.responseIDs = list.distinct().takeUnless { it.isEmpty() }
        })
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.OBJ_ID_STAR_SUB, Validators.identicalObjID(editor.gameObject, this.struct))
        validation.registerValidator(nameField, Validators.NAME_BLANK)
        validation.registerValidators(responseIDsField, Validators.EXTERNAL_RESPONSE_IDS, Validators.responseIDsPointsNowhere(editor.gameObject))
    }

    class RandomCueCuePointerPane(cuePointer: CuePointer, parent: CuesPane<RandomCue>) : CuePointerPane<RandomCue>(parent, cuePointer) {
        init {
            addProperty(Label().bindLocalized("cuePointer.id"), idField)
            addProperty(Label().bindLocalized("cuePointer.duration").apply {
                tooltip = Tooltip().bindLocalized("cuePointer.duration.tooltip")
            }, durationField)
            addProperty(Label().bindLocalized("cuePointer.semitone"), semitoneField)
            addProperty(Label().bindLocalized("cuePointer.track").apply {
                tooltip = Tooltip().bindLocalized("cuePointer.track.tooltip")
            }, trackField)
            addProperty(Label().bindLocalized("cuePointer.volume"), volumeField)
        }
    }

}