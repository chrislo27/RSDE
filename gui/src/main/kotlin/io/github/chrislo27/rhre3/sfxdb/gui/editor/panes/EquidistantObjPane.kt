package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.adt.CuePointer
import io.github.chrislo27.rhre3.sfxdb.adt.Equidistant
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.doubleSpinnerFactory
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.Tooltip


class EquidistantObjPane(editor: Editor, struct: Equidistant) : MultipartStructPane<Equidistant>(editor, struct) {

    val distanceField = doubleSpinnerFactory(0.0, Float.MAX_VALUE.toDouble(), struct.distance.toDouble(), 0.5)
    val stretchableField = CheckBox().apply { this.isSelected = struct.stretchable }
    override val cuesPane: CuesPane<Equidistant> = CuesPane(this) { pointer, pane -> EquidistantCuePointerPane(pointer, pane) }

    init {
        addProperty(Label().bindLocalized("datamodel.type"), Label("equidistant").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs").apply {
            tooltip = Tooltip().bindLocalized("datamodel.deprecatedIDs.tooltip")
        }, deprecatedIDsField)
        addProperty(Label().bindLocalized("datamodel.subtext").apply {
            tooltip = Tooltip().bindLocalized("datamodel.subtext.tooltip")
        }, subtextField)

        addProperty(Label().bindLocalized("equidistantObj.distance").apply {
            tooltip = Tooltip().bindLocalized("equidistantObj.distance.tooltip")
        }, distanceField)
        addProperty(Label().bindLocalized("datamodel.stretchable").apply {
            tooltip = Tooltip().bindLocalized("datamodel.stretchable.tooltip")
        }, stretchableField)

        centreVbox.children += cuesPane
    }

    init {
        // Bind to struct
        distanceField.valueProperty().addListener { _, _, newValue ->
            struct.distance = newValue.toFloat()
            editor.markDirty()
        }
        stretchableField.selectedProperty().addListener { _, _, newValue ->
            struct.stretchable = newValue
            editor.markDirty()
        }

        distanceField.valueProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.OBJ_ID_STAR_SUB, Validators.identicalObjID(editor.gameObject, this.struct))
        validation.registerValidators(nameField, Validators.NAME_BLANK)
        validation.registerValidators(distanceField, Validators.ZERO_DISTANCE)
    }

    class EquidistantCuePointerPane(cuePointer: CuePointer, parent: CuesPane<Equidistant>) : CuePointerPane<Equidistant>(parent, cuePointer) {
        init {
            addProperty(Label().bindLocalized("cuePointer.id"), idField)
            addProperty(Label().bindLocalized("cuePointer.semitone"), semitoneField)
            addProperty(Label().bindLocalized("cuePointer.track").apply {
                tooltip = Tooltip().bindLocalized("cuePointer.track.tooltip")
            }, trackField)
            addProperty(Label().bindLocalized("cuePointer.volume"), volumeField)
        }
    }

}