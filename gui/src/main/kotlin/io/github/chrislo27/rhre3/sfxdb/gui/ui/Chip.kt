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

    private val hBox = HBox().apply {
        styleClass += "chip"
        styleClass += "hbox"
    }
    val closeButton: Button = Button().apply {
        isFocusTraversable = false
        HBox.setMargin(this, Insets(0.0))
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
        styleClass += "chip"

        closeButton.visibleProperty().bind(closeableProperty)

        if (graphic != null) {
            hBox.children += graphic
        }
        hBox.children += label.apply {
            styleClass += "chip"
        }
        hBox.children += closeButton.apply {
            styleClass += "chip"
            styleClass += "close-button"
        }

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
