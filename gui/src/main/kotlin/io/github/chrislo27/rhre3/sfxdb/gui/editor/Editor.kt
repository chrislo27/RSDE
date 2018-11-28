package io.github.chrislo27.rhre3.sfxdb.gui.editor

import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.gui.registry.GameRegistry
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import io.github.chrislo27.rhre3.sfxdb.validation.GameObject
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import java.io.File


class Editor(val folder: File) {

    val gameObject: GameObject = GameObject().apply {
        Parser.buildStruct(this, JsonHandler.OBJECT_MAPPER.readTree(folder.resolve("data.json")))
    }

    val mainPane: StackPane = StackPane()
    val tab: Tab = Tab(folder.name, mainPane).apply tab@{
        val iconFile = folder.resolve("icon.png").takeIf { it.exists() }
        graphic = ImageView(if (iconFile != null) Image("file:${iconFile.path}") else GameRegistry.missingIconImage).apply iv@{
            this.isPreserveRatio = true
            this.fitWidth = 1.5.em
            this.fitHeight = 1.5.em
        }

    }
    private val pickFirstLabel = Label().bindLocalized("editor.selectAnItem").apply {
        id = "pick-first-label"
        alignment = Pos.CENTER
        textAlignment = TextAlignment.CENTER
        isWrapText = true
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE
    }

    init {

        mainPane.alignment = Pos.CENTER
        mainPane.children += pickFirstLabel
    }

    fun switchToPane(pane: Pane?) {
        if (mainPane.children.size > 1)
            mainPane.children.remove(1, mainPane.children.size)

        if (pane != null) {
            mainPane.children += pane
            pickFirstLabel.isVisible = false
        } else {
            pickFirstLabel.isVisible = true
        }
    }

}
