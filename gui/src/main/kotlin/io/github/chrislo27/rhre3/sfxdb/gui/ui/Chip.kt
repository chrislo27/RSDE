package io.github.chrislo27.rhre3.sfxdb.gui.ui

import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.layout.HBox
import org.controlsfx.tools.ValueExtractor


open class Chip(val content: String, val graphic: Node? = null, closeable: Boolean = true, val label: Label = Label(content))
    : Control() {

    companion object {
        init {
            ValueExtractor.addObservableValueExtractor({ it is Chip }, { ReadOnlyStringWrapper((it as Chip).content) })
        }
    }

    private val hBox = HBox()
    val closeButton: Button = Button("⛒").apply {
        isFocusTraversable = false
        HBox.setMargin(this, Insets(0.0))
        style = """-fx-shape: "M 100 100 a 50 50 0 1 0 0.00001 0"; -fx-padding: 0;"""
    }
    val chipPaneProperty: SimpleObjectProperty<ChipPane?> = SimpleObjectProperty(null)
    val closeableProperty = SimpleBooleanProperty(closeable)
    var isCloseable: Boolean
        get() = closeableProperty.value
        set(value) {
            closeableProperty.value = value
        }

    init {
        hBox.alignment = Pos.CENTER_LEFT
        style = """-fx-background-color: lightgray; -fx-background-radius: 1em; -fx-padding: 0.25em;"""

        closeButton.visibleProperty().bind(closeableProperty)

        if (graphic != null) {
            hBox.children += graphic
        }
        hBox.children += label
        hBox.children += closeButton

        closeButton.setOnAction {
            val chipPane: ChipPane = chipPaneProperty.value ?: return@setOnAction
            chipPane.list -= this@Chip
        }
    }

    override fun createDefaultSkin(): Skin<Chip> {
        return ChipSkin()
    }

    inner class ChipSkin : Skin<Chip> {
        override fun getSkinnable(): Chip = this@Chip

        override fun getNode(): Node = this@Chip.hBox

        override fun dispose() {
        }
    }

}
