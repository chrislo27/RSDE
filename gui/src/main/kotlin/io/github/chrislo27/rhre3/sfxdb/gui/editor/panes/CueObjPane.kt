package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.SoundFileExtensions
import io.github.chrislo27.rhre3.sfxdb.adt.Cue
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.ui.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.ui.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.doubleSpinnerFactory
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.control.*
import javafx.util.StringConverter


class CueObjPane(editor: Editor, struct: Cue) : DatamodelPane<Cue>(editor, struct) {
    
    val durationField = doubleSpinnerFactory(0.0, Float.MAX_VALUE.toDouble(), struct.duration.toDouble(), 0.5)
    val stretchableField = CheckBox().apply { this.isSelected = struct.stretchable}
    val repitchableField = CheckBox().apply { this.isSelected = struct.repitchable }
    val loopsField = CheckBox().apply { this.isSelected = struct.loops}
    val baseBpmField = Spinner<Double>().apply {
        valueFactory = SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, Float.MAX_VALUE.toDouble(), struct.baseBpm.toDouble(), 1.0).apply {
            this.converter = object : StringConverter<Double>() {
                override fun toString(`object`: Double): String {
                    return if (`object` == 0.0) Localization["cueObject.baseBpm.none"] else `object`.toString()
                }

                override fun fromString(string: String): Double {
                    return string.toDoubleOrNull() ?: 0.0
                }
            }
        }
        isEditable = true
    }
    val introSoundField = TextField(struct.introSound)
    val endingSoundField = TextField(struct.endingSound)
    val fileExtField = TextField(struct.endingSound).apply {
        this.promptText = SoundFileExtensions.DEFAULT.fileExt
    }
    val responseIDsField = ChipPane(FXCollections.observableArrayList((struct.responseIDs ?: mutableListOf()).map { Chip(it) }))

    init {
        addProperty(Label().bindLocalized("datamodel.type"), Label("cue").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs"), deprecatedIDsField)
        
        addProperty(Label().bindLocalized("cueObject.duration"), durationField)
        addProperty(Label().bindLocalized("datamodel.stretchable"), stretchableField)
        addProperty(Label().bindLocalized("cueObject.repitchable"), repitchableField)
        addProperty(Label().bindLocalized("cueObject.loops"), loopsField)
        addProperty(Label().bindLocalized("cueObject.baseBpm").apply {
            tooltip = Tooltip().bindLocalized("cueObject.baseBpm.tooltip")
        }, baseBpmField)
        addProperty(Label().bindLocalized("cueObject.introSound"), introSoundField)
        addProperty(Label().bindLocalized("cueObject.endingSound"), endingSoundField)
        addProperty(Label().bindLocalized("cueObject.fileExtension"), fileExtField)
        addProperty(Label().bindLocalized("datamodel.responseIDs"), responseIDsField)
    }

    init {
        // Bind to struct
        durationField.valueProperty().addListener { _, _, newValue -> struct.duration = newValue.toFloat() }
        stretchableField.selectedProperty().addListener { _, _, newValue -> struct.stretchable = newValue }
        repitchableField.selectedProperty().addListener { _, _, newValue -> struct.repitchable = newValue }
        loopsField.selectedProperty().addListener { _, _, newValue -> struct.loops = newValue }
        baseBpmField.valueProperty().addListener { _, _, newValue -> struct.baseBpm = newValue.toFloat() }
        introSoundField.textProperty().addListener { _, _, newValue -> struct.introSound = newValue?.takeUnless { it.isBlank() } }
        endingSoundField.textProperty().addListener { _, _, newValue -> struct.endingSound = newValue?.takeUnless { it.isBlank() } }
        fileExtField.textProperty().addListener { _, _, newValue -> struct.fileExtension = newValue?.takeUnless { it.isBlank() } ?: SoundFileExtensions.DEFAULT.fileExt }
        responseIDsField.list.addListener(ListChangeListener { evt ->
            val list = mutableListOf<String>()
            while (evt.next()) {
                list.addAll(evt.list.map { chip -> chip.label.text })
            }
            struct.responseIDs = list.takeUnless { it.isEmpty() }
        })
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.CUE_ID_STAR_SUB, Validators.identicalObjID(editor.gameObject, this.struct), Validators.soundFileNotFound(editor.folder, this.struct))
        validation.registerValidator(nameField, Validators.NAME_BLANK)
        validation.registerValidators(fileExtField, Validators.FILE_EXT_NOT_OGG)
        validation.registerValidators(introSoundField, Validators.EXTERNAL_CUE_POINTER, Validators.cuePointerPointsNowhere(editor.gameObject))
        validation.registerValidators(endingSoundField, Validators.EXTERNAL_CUE_POINTER, Validators.cuePointerPointsNowhere(editor.gameObject))
        validation.registerValidators(responseIDsField, Validators.EXTERNAL_RESPONSE_IDS, Validators.responseIDsPointsNowhere(editor.gameObject))
        validation.registerValidators(durationField, Validators.ZERO_DURATION)
    }

}