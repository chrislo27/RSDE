package io.github.chrislo27.rhre3.sfxdb.gui.editor

import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.gui.editor.panes.*
import io.github.chrislo27.rhre3.sfxdb.gui.registry.GameRegistry
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import io.github.chrislo27.rhre3.sfxdb.validation.*
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import java.io.File
import java.util.*


class Editor(val folder: File) {

    val gameObject: GameObject = GameObject().apply {
        Parser.buildStruct(this, JsonHandler.OBJECT_MAPPER.readTree(folder.resolve("data.json")))
    }
    val paneMap: Map<Struct, Pane> = WeakHashMap()
    val mainPane: StackPane = StackPane()
    val tab: Tab = Tab(folder.name, mainPane).apply tab@{
        val iconFile = folder.resolve("icon.png").takeIf { it.exists() }
        graphic =
                ImageView(if (iconFile != null) Image("file:${iconFile.path}") else GameRegistry.missingIconImage).apply iv@{
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

    fun getPane(struct: Struct): Pane? {
        paneMap as MutableMap
        val fromMap = paneMap[struct]
        if (fromMap == null) {
            val newPane = createPaneFromStruct(struct)
            if (newPane != null) {
                paneMap[struct] = newPane
            }
            return newPane
        }
        return fromMap
    }

    private fun createPaneFromStruct(struct: Struct): Pane? {
        return when (struct) {
            is GameObject -> GameObjPane(this)
            is CueObject -> CueObjPane(this, struct)
            is PatternObject -> PatternObjPane(this, struct)
            is KeepTheBeatObject -> KeepTheBeatObjPane(this, struct)
            is EquidistantObject -> EquidistantObjPane(this, struct)
            is RandomCueObject -> RandomCueObjPane(this, struct)
            is CuePointerObject -> {
                TODO()
            }
            is SubtitleEntityObject, is ShakeEntityObject, is EndRemixEntityObject, is TextureEntityObject -> null
            else -> throw IllegalStateException("Struct ${struct::class.java.name} is not supported for editing. Please tell the developer!")
        }
    }

    fun update() {
        paneMap.values.forEach { if (it is StructPane<*>) it.update() }
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
