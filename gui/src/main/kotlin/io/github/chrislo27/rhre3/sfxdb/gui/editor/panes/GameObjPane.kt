package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import io.github.chrislo27.rhre3.sfxdb.validation.GameObject
import io.github.chrislo27.rhre3.sfxdb.validation.orElse
import io.github.chrislo27.rhre3.sfxdb.validation.orNull
import javafx.collections.FXCollections
import javafx.scene.control.*


class GameObjPane(editor: Editor) : StructPane<GameObject>(editor, editor.gameObject) {

    val idField = TextField(gameObject.id.orElse("??? MISSING ID ???"))
    val nameField = TextField(gameObject.name.orElse("MISSING NAME"))
    val seriesComboBox = ComboBox<Series>(FXCollections.observableArrayList(Series.VALUES - listOf(Series.SWITCH))).apply {
        this.selectionModel.select(gameObject.series.orNull())
    }
    val groupField = TextField(gameObject.group.orElse(""))
    val groupDefaultCheckbox = CheckBox().apply {
        this.isSelected = gameObject.groupDefault.orElse(false)
    }
    val prioritySpinner = Spinner<Int>(-128, 127, gameObject.priority.orElse(0))
    val searchHintsField = ChipPane(FXCollections.observableArrayList(gameObject.searchHints.orElse(listOf()).map { Chip(it) }))
    val noDisplayCheckbox = CheckBox().apply {
        this.isSelected = gameObject.noDisplay.orElse(false)
    }

    init {
        titleLabel.text = gameObject.id.orElse("??? GAME ID MISSING ???")

        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("gameObject.series"), seriesComboBox)
        addProperty(Label().bindLocalized("gameObject.group"), groupField)
        addProperty(Label().bindLocalized("gameObject.groupDefault"), groupDefaultCheckbox)
        addProperty(Label().bindLocalized("gameObject.priority"), prioritySpinner)
        addProperty(Label().bindLocalized("gameObject.searchHints"), searchHintsField)
        addProperty(Label().bindLocalized("gameObject.noDisplay"), noDisplayCheckbox)

        centreVbox.children += Separator().apply {
            maxWidth = Double.MAX_VALUE
        }
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.GAME_ID)
        validation.registerValidator(nameField, Validators.NAME_BLANK)
        validation.registerValidator(noDisplayCheckbox, Validators.NO_DISPLAY)
    }

}