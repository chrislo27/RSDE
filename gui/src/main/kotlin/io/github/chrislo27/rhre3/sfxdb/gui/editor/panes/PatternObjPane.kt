package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.adt.CuePointer
import io.github.chrislo27.rhre3.sfxdb.adt.Pattern
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.Tooltip


class PatternObjPane(editor: Editor, struct: Pattern) : MultipartStructPane<Pattern>(editor, struct) {

    val stretchableField = CheckBox().apply { this.isSelected = struct.stretchable }
    override val cuesPane: CuesPane<Pattern> = CuesPane(this) { pointer, pane -> PatternCuePointerPane(pointer, pane) }

    init {
        addProperty(Label().bindLocalized("datamodel.type"), Label("pattern").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs").apply {
            tooltip = Tooltip().bindLocalized("datamodel.deprecatedIDs.tooltip")
        }, deprecatedIDsField)

        addProperty(Label().bindLocalized("datamodel.stretchable").apply {
            tooltip = Tooltip().bindLocalized("datamodel.stretchable.tooltip")
        }, stretchableField)

        centreVbox.children += cuesPane
    }

    init {
        // Bind to struct
        stretchableField.selectedProperty().addListener { _, _, newValue ->
            struct.stretchable = newValue
            editor.markDirty()
        }
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.OBJ_ID_STAR_SUB, Validators.identicalObjID(editor.gameObject, this.struct))
        validation.registerValidator(nameField, Validators.NAME_BLANK)
    }

    class PatternCuePointerPane(cuePointer: CuePointer, parent: CuesPane<Pattern>) : CuePointerPane<Pattern>(parent, cuePointer) {
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