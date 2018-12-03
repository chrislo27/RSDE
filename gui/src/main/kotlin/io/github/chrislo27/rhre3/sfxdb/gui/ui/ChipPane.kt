package io.github.chrislo27.rhre3.sfxdb.gui.ui

import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.FlowPane


open class ChipPane(initialList: ObservableList<Chip>, canAdd: Boolean = true) : FlowPane(Orientation.HORIZONTAL, 0.5.em, 0.5.em) {

    constructor(canAdd: Boolean = true) : this(FXCollections.observableArrayList(), canAdd)

    val list: ObservableList<Chip> = FXCollections.observableArrayList()
    private val textField: TextField = TextField().apply {
        style = """-fx-background-color: none;"""
        setOnAction { evt ->
            val text = this.text
            if (text.isNotBlank() && canAdd) {
                list += chipFactory(text)
                this.text = ""
            }
        }
        setOnKeyPressed { evt ->
            if (this.text.isEmpty() && evt.code == KeyCode.BACK_SPACE) {
                val chip: Chip = list.lastOrNull() ?: return@setOnKeyPressed
                if (chip.isCloseable) {
                    list -= chip
                }
            }
        }
        prefWidth = 10.0.em
    }
    val canAddProperty = SimpleBooleanProperty(canAdd)
    var canAdd: Boolean
        get() = canAddProperty.value
        set(value) {
            canAddProperty.value = value
        }
    var chipFactory: (text: String) -> Chip = { Chip(it) }

    init {
        textField.editableProperty().bind(canAddProperty)
        textField.visibleProperty().bind(canAddProperty)
        style = "-fx-background-color: #eeeeee;"

        children += textField

        list.addListener(ListChangeListener { ch ->
            while (ch.next()) {
                ch.removed.forEach { chip ->
                    children -= chip
                    chip.chipPaneProperty.value = null
                }
                ch.addedSubList.forEachIndexed { index, chip ->
                    children.add(index + ch.from, chip)
                    chip.chipPaneProperty.value = this
                }
            }
        })
        list.forEach { chip ->
            chip.chipPaneProperty.value = this
        }

        list.addAll(initialList)
    }

}