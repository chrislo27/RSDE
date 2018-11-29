package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.validation.orElse
import io.github.chrislo27.rhre3.sfxdb.validation.orNull
import javafx.collections.FXCollections
import javafx.scene.control.*


class GamePane(editor: Editor) : DatamodelPane(editor) {

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
    val searchHintsField = ChipPane(FXCollections.observableArrayList()).apply {
        val list = gameObject.searchHints.orNull()
        list?.forEach { hint ->
            this.list += Chip(hint)
        }
    }
    val noDisplayCheckbox = CheckBox().apply {
        this.isSelected = gameObject.noDisplay.orElse(false)
    }

    init {
        titleLabel.text = gameObject.id.orElse("??? GAME ID MISSING ???")

        gridPane.add(Label().bindLocalized("gameObject.id"), 0, 0)
        gridPane.add(idField, 1, 0)
        gridPane.add(Label().bindLocalized("gameObject.name"), 0, 1)
        gridPane.add(nameField, 1, 1)
        gridPane.add(Label().bindLocalized("gameObject.series"), 0, 2)
        gridPane.add(seriesComboBox, 1, 2)
        gridPane.add(Label().bindLocalized("gameObject.group"), 0, 3)
        gridPane.add(groupField, 1, 3)
        gridPane.add(Label().bindLocalized("gameObject.groupDefault"), 0, 4)
        gridPane.add(groupDefaultCheckbox, 1, 4)
        gridPane.add(Label().bindLocalized("gameObject.priority"), 0, 5)
        gridPane.add(prioritySpinner, 1, 5)
        gridPane.add(Label().bindLocalized("gameObject.searchHints"), 0, 6)
        gridPane.add(searchHintsField, 1, 6)
        gridPane.add(Label().bindLocalized("gameObject.noDisplay"), 0, 7)
        gridPane.add(noDisplayCheckbox, 1, 7)

        centreVbox.children += Separator().apply {
            maxWidth = Double.MAX_VALUE
        }
    }

}