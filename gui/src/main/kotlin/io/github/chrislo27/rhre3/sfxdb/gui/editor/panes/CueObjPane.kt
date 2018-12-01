package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.SoundFileExtensions
import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.doubleSpinnerFactory
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import io.github.chrislo27.rhre3.sfxdb.validation.CueObject
import io.github.chrislo27.rhre3.sfxdb.validation.orElse
import io.github.chrislo27.rhre3.sfxdb.validation.orException
import javafx.collections.FXCollections
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField


class CueObjPane(editor: Editor, cueObject: CueObject) : StructPane<CueObject>(editor, cueObject) {

    val idField = TextField(struct.id.orElse("??? MISSING ID ???"))
    val nameField = TextField(struct.name.orElse("MISSING NAME"))
    val deprecatedIDsField = ChipPane(FXCollections.observableArrayList(struct.deprecatedIDs.orElse(listOf()).map { Chip(it) }))
    
    val durationField = doubleSpinnerFactory(0.0, Float.MAX_VALUE.toDouble(), struct.duration.orException().toDouble(), 0.5)
    val stretchableField = CheckBox().apply { this.isSelected = struct.stretchable.orElse(false) }
    val repitchableField = CheckBox().apply { this.isSelected = struct.repitchable.orElse(false) }
    val loopsField = CheckBox().apply { this.isSelected = struct.loops.orElse(false) }
    val baseBpmField = doubleSpinnerFactory(0.0, Float.MAX_VALUE.toDouble(), struct.baseBpm.orElse(0f).toDouble(), 1.0)
    val introSoundField = TextField(struct.introSound.orElse(""))
    val endingSoundField = TextField(struct.endingSound.orElse(""))
    val fileExtField = TextField(struct.endingSound.orElse(SoundFileExtensions.DEFAULT.fileExt)).apply {
        this.promptText = SoundFileExtensions.DEFAULT.fileExt
    }
    val responseIDsField = ChipPane(FXCollections.observableArrayList(struct.responseIDs.orElse(listOf()).map { Chip(it) }))

    init {
        titleLabel.text = struct.id.orElse("??? ID MISSING ???")

        addProperty(Label().bindLocalized("datamodel.type"), Label("cue").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs"), deprecatedIDsField)
        
        addProperty(Label().bindLocalized("cueObject.duration"), durationField)
        addProperty(Label().bindLocalized("datamodel.stretchable"), stretchableField)
        addProperty(Label().bindLocalized("cueObject.repitchable"), repitchableField)
        addProperty(Label().bindLocalized("cueObject.loops"), loopsField)
        addProperty(Label().bindLocalized("cueObject.baseBpm"), baseBpmField)
        addProperty(Label().bindLocalized("cueObject.introSound"), introSoundField)
        addProperty(Label().bindLocalized("cueObject.endingSound"), endingSoundField)
        addProperty(Label().bindLocalized("cueObject.fileExtension"), fileExtField)
        addProperty(Label().bindLocalized("datamodel.responseIDs"), responseIDsField)
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.CUE_ID_STAR_SUB)
        validation.registerValidator(nameField, Validators.NAME_BLANK)
        validation.registerValidator(fileExtField, Validators.FILE_EXT_NOT_OGG)
    }

}