package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.BaseBpmRules
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
    val stretchableField = CheckBox().apply { this.isSelected = struct.stretchable }
    val repitchableField = CheckBox().apply { this.isSelected = struct.repitchable }
    val loopsField = CheckBox().apply { this.isSelected = struct.loops }
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
    val useTimeStretchingField = CheckBox().apply { this.isSelected = struct.useTimeStretching }
    val baseBpmRulesComboBox = ComboBox<BaseBpmRules>(FXCollections.observableArrayList(BaseBpmRules.VALUES)).apply {
        this.selectionModel.select(struct.baseBpmRules)
        this.converter = object : StringConverter<BaseBpmRules>() {
            override fun toString(rule: BaseBpmRules?): String {
                return rule?.properName ?: "???"
            }

            override fun fromString(string: String?): BaseBpmRules {
                return BaseBpmRules.MAP[string] ?: BaseBpmRules.ALWAYS
            }
        }
    }
    val introSoundField = TextField(struct.introSound)
    val endingSoundField = TextField(struct.endingSound)
    val fileExtField = TextField(struct.fileExtension).apply {
        this.promptText = SoundFileExtensions.DEFAULT.fileExt
    }
    val earlinessField = doubleSpinnerFactory(0.0, Float.MAX_VALUE.toDouble(), 0.0, 0.1)
    val loopStartField = doubleSpinnerFactory(0.0, Float.MAX_VALUE.toDouble(), 0.0, 0.1)
    val loopEndField = doubleSpinnerFactory(-1.0, Float.MAX_VALUE.toDouble(), 0.0, 0.1)
    val responseIDsField = ChipPane(FXCollections.observableArrayList((struct.responseIDs ?: mutableListOf()).map { Chip(it) }))
    val pitchBendingField = CheckBox().apply { this.isSelected = struct.pitchBending }
    val writtenPitchSpinner = Spinner<Int>(-128, 127, struct.writtenPitch)

    init {
        addProperty(Label().bindLocalized("datamodel.type"), Label("cue").apply { styleClass += "monospaced" })
        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("datamodel.deprecatedIDs").apply {
            tooltip = Tooltip().bindLocalized("datamodel.deprecatedIDs.tooltip")
        }, deprecatedIDsField)
        addProperty(Label().bindLocalized("datamodel.subtext").apply {
            tooltip = Tooltip().bindLocalized("datamodel.subtext.tooltip")
        }, subtextField)

        addProperty(Label().bindLocalized("cueObject.duration"), durationField)
        addProperty(Label().bindLocalized("datamodel.stretchable").apply {
            tooltip = Tooltip().bindLocalized("datamodel.stretchable.tooltip")
        }, stretchableField)
        addProperty(Label().bindLocalized("cueObject.repitchable"), repitchableField)
        addProperty(Label().bindLocalized("cueObject.loops"), loopsField)
        addProperty(Label().bindLocalized("cueObject.baseBpm").apply {
            tooltip = Tooltip().bindLocalized("cueObject.baseBpm.tooltip")
        }, baseBpmField)
        addProperty(Label().bindLocalized("cueObject.useTimeStretching").apply {
            tooltip = Tooltip().bindLocalized("cueObject.useTimeStretching.tooltip")
        }, useTimeStretchingField)
        addProperty(Label().bindLocalized("cueObject.baseBpmRules").apply {
            tooltip = Tooltip().bindLocalized("cueObject.baseBpmRules.tooltip")
        }, baseBpmRulesComboBox)
        addProperty(Label().bindLocalized("cueObject.introSound").apply {
            tooltip = Tooltip().bindLocalized("cueObject.introSound.tooltip")
        }, introSoundField)
        addProperty(Label().bindLocalized("cueObject.endingSound").apply {
            tooltip = Tooltip().bindLocalized("cueObject.endingSound.tooltip")
        }, endingSoundField)
        addProperty(Label().bindLocalized("cueObject.fileExtension"), fileExtField)
        addProperty(Label().bindLocalized("cueObject.earliness").apply {
            tooltip = Tooltip().bindLocalized("cueObject.earliness.tooltip")
        }, earlinessField)
        addProperty(Label().bindLocalized("cueObject.loopStart").apply {
            tooltip = Tooltip().bindLocalized("cueObject.loopStart.tooltip")
        }, loopStartField)
        addProperty(Label().bindLocalized("cueObject.loopEnd").apply {
            tooltip = Tooltip().bindLocalized("cueObject.loopEnd.tooltip")
        }, loopEndField)
        addProperty(Label().bindLocalized("cueObject.pitchBending").apply {
            tooltip = Tooltip().bindLocalized("cueObject.pitchBending.tooltip")
        }, pitchBendingField)
        addProperty(Label().bindLocalized("cueObject.writtenPitch").apply {
            tooltip = Tooltip().bindLocalized("cueObject.writtenPitch.tooltip")
        }, writtenPitchSpinner)
        addProperty(Label().bindLocalized("datamodel.responseIDs").apply {
            tooltip = Tooltip().bindLocalized("datamodel.responseIDs.tooltip")
        }, responseIDsField)
    }

    init {
        // Bind to struct
        durationField.valueProperty().addListener { _, _, newValue ->
            struct.duration = newValue.toFloat()
            editor.markDirty()
        }
        stretchableField.selectedProperty().addListener { _, _, newValue ->
            struct.stretchable = newValue
            editor.markDirty()
        }
        repitchableField.selectedProperty().addListener { _, _, newValue ->
            struct.repitchable = newValue
            editor.markDirty()
        }
        loopsField.selectedProperty().addListener { _, _, newValue ->
            struct.loops = newValue
            editor.markDirty()
            forceUpdate()
        }
        baseBpmField.valueProperty().addListener { _, _, newValue ->
            struct.baseBpm = newValue.toFloat()
            editor.markDirty()
        }
        useTimeStretchingField.selectedProperty().addListener { _, _, newValue ->
            struct.useTimeStretching = newValue
            editor.markDirty()
        }
        baseBpmRulesComboBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            struct.baseBpmRules = newValue
            editor.markDirty()
        }
        introSoundField.textProperty().addListener { _, _, newValue ->
            struct.introSound = newValue?.takeUnless { it.isBlank() }
            editor.markDirty()
        }
        endingSoundField.textProperty().addListener { _, _, newValue ->
            struct.endingSound = newValue?.takeUnless { it.isBlank() }
            editor.markDirty()
        }
        fileExtField.textProperty().addListener { _, _, newValue ->
            struct.fileExtension = newValue?.takeUnless { it.isBlank() } ?: SoundFileExtensions.DEFAULT.fileExt
            editor.markDirty()
        }
        responseIDsField.list.addListener(ListChangeListener { evt ->
            val list = mutableListOf<String>()
            while (evt.next()) {
                list.addAll(evt.list.map { chip -> chip.label.text })
            }
            struct.responseIDs = list.takeUnless { it.isEmpty() }
            editor.markDirty()
        })
        earlinessField.valueProperty().addListener { _, _, n ->
            struct.earliness = n.toFloat()
            editor.markDirty()
        }
        loopStartField.valueProperty().addListener { _, _, n ->
            struct.loopStart = n.toFloat()
            editor.markDirty()
        }
        loopEndField.valueProperty().addListener { _, _, n ->
            struct.loopEnd = n.toFloat()
            editor.markDirty()
        }
        pitchBendingField.selectedProperty().addListener { _, _, n ->
            struct.pitchBending = n
            editor.markDirty()
        }
        writtenPitchSpinner.valueProperty().addListener { _, _, n ->
            struct.writtenPitch = n
            editor.markDirty()
        }

        fileExtField.textProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
        introSoundField.textProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
        endingSoundField.textProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
        responseIDsField.list.addListener(ListChangeListener {
            it.next()
            editor.refreshLists()
        })
        durationField.valueProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
        earlinessField.valueProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
        loopStartField.valueProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
        loopEndField.valueProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.OBJ_ID_REGEX, Validators.CUE_ID_STAR_SUB, Validators.identicalObjID(editor.gameObject, this.struct), Validators.soundFileNotFound(editor.folder, this.struct))
        validation.registerValidators(nameField, Validators.NAME_BLANK)
        validation.registerValidators(fileExtField, Validators.FILE_EXT_NOT_OGG, Validators.UNSUPPORTED_FILE_EXT)
        validation.registerValidators(introSoundField, Validators.EXTERNAL_CUE_POINTER, Validators.cuePointerPointsNowhere(editor.gameObject))
        validation.registerValidators(endingSoundField, Validators.EXTERNAL_CUE_POINTER, Validators.cuePointerPointsNowhere(editor.gameObject))
        validation.registerValidators(responseIDsField, Validators.EXTERNAL_RESPONSE_IDS, Validators.responseIDsPointsNowhere(editor.gameObject))
        validation.registerValidators(durationField, Validators.ZERO_DURATION)
        validation.registerValidators(loopStartField, Validators.loopStartAheadOfEnd(this), Validators.loopStartWithoutLooping(this))
        validation.registerValidators(loopEndField, Validators.loopEndWithoutLooping(this))
    }

}