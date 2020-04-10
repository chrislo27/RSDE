package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.adt.CuePointer
import io.github.chrislo27.rhre3.sfxdb.adt.KeepTheBeat
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.doubleSpinnerFactory
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.scene.control.Label
import javafx.scene.control.Tooltip


class KeepTheBeatObjPane(editor: Editor, struct: KeepTheBeat) : MultipartStructPane<KeepTheBeat>(editor, struct) {

    val defaultDurationField = doubleSpinnerFactory(0.0, Float.MAX_VALUE.toDouble(), struct.defaultDuration.toDouble(), 0.5)
    override val cuesPane: CuesPane<KeepTheBeat> = CuesPane(this) { pointer, pane -> KeepTheBeatCuePointerPane(pointer, pane) }

    init {
        addProperty(Label().bindLocalized("datamodel.type"), Label("keepTheBeat").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs").apply {
            tooltip = Tooltip().bindLocalized("datamodel.deprecatedIDs.tooltip")
        }, deprecatedIDsField)
        addProperty(Label().bindLocalized("datamodel.subtext").apply {
            tooltip = Tooltip().bindLocalized("datamodel.subtext.tooltip")
        }, subtextField)

        addProperty(Label().bindLocalized("keepTheBeatObj.defaultDuration").apply {
            tooltip = Tooltip().bindLocalized("keepTheBeatObj.defaultDuration.tooltip")
        }, defaultDurationField)

        centreVbox.children += cuesPane
    }

    init {
        // Bind to struct
        defaultDurationField.valueProperty().addListener { _, _, newValue ->
            struct.defaultDuration = newValue.toFloat()
            editor.markDirty()
        }

        defaultDurationField.valueProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.OBJ_ID_STAR_SUB, Validators.identicalObjID(editor.gameObject, this.struct))
        validation.registerValidators(nameField, Validators.NAME_BLANK)
        validation.registerValidators(defaultDurationField, Validators.ZERO_DURATION)
    }

    class KeepTheBeatCuePointerPane(cuePointer: CuePointer, parent: CuesPane<KeepTheBeat>) : CuePointerPane<KeepTheBeat>(parent, cuePointer) {
        init {
            addProperty(Label().bindLocalized("cuePointer.id"), idField)
            addProperty(Label().bindLocalized("cuePointer.beat"), beatField)
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