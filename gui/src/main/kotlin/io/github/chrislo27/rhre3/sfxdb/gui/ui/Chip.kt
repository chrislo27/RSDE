package io.github.chrislo27.rhre3.sfxdb.gui.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox


open class Chip(val label: Label, val graphic: Node? = null, closeable: Boolean = true) : HBox() {

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

    constructor(text: String, graphic: Node? = null, closeable: Boolean = true) : this(Label(text), graphic, closeable)

    init {
        alignment = Pos.CENTER_LEFT
        style = """-fx-background-color: lightgray; -fx-background-radius: 1em; -fx-padding: 0.25em;"""

        closeButton.visibleProperty().bind(closeableProperty)

        if (graphic != null) {
            children += graphic
        }
        children += label
        children += closeButton

        closeButton.setOnAction {
            val chipPane: ChipPane = chipPaneProperty.value ?: return@setOnAction
            chipPane.list -= this@Chip
        }
    }

}
