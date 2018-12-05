package io.github.chrislo27.rhre3.sfxdb.gui.ui

import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.FlowPane
import org.controlsfx.tools.ValueExtractor
import org.fxmisc.easybind.EasyBind


open class ChipPane(val list: ObservableList<Chip> = FXCollections.observableArrayList(), canAdd: Boolean = true) : Control() {

    companion object {
        init {
            ValueExtractor.addObservableValueExtractor({ it is ChipPane }, { (it as ChipPane).readOnlyListStringWrapper })
        }
    }

    private val readOnlyListStringWrapper: ReadOnlyListWrapper<String> = ReadOnlyListWrapper(EasyBind.map(list, Chip::content))
    val flowPane: FlowPane = FlowPane(Orientation.HORIZONTAL, 0.5.em, 0.5.em)
    private val textField: TextField = TextField().apply {
        style = """-fx-background-color: none;"""
        setOnAction { _ ->
            val text = this.text.trim()
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

    override fun createDefaultSkin(): Skin<ChipPane> {
        return ChipPaneSkin()
    }

    init {
        textField.editableProperty().bind(canAddProperty)
        textField.visibleProperty().bind(canAddProperty)
        styleClass += "text-input"
        styleClass += "chip-pane"

        flowPane.children += textField

        list.addListener(ListChangeListener { ch ->
            while (ch.next()) {
                ch.removed.forEach { chip ->
                    flowPane.children -= chip
                    chip.chipPaneProperty.value = null
                }
                ch.addedSubList.forEachIndexed { index, chip ->
                    flowPane.children.add(index + ch.from, chip)
                    chip.chipPaneProperty.value = this
                }
            }
        })
        list.forEachIndexed { index, chip ->
            flowPane.children.add(index, chip)
            chip.chipPaneProperty.value = this
        }
    }

    inner class ChipPaneSkin : Skin<ChipPane> {
        override fun getSkinnable(): ChipPane = this@ChipPane

        override fun getNode(): Node = this@ChipPane.flowPane

        override fun dispose() {
        }
    }

}