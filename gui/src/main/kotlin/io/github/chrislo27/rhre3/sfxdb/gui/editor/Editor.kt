package io.github.chrislo27.rhre3.sfxdb.gui.editor

import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.adt.*
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.editor.panes.*
import io.github.chrislo27.rhre3.sfxdb.gui.registry.GameRegistry
import io.github.chrislo27.rhre3.sfxdb.gui.scene.EditorPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import io.github.chrislo27.rhre3.sfxdb.validation.GameObject
import javafx.beans.property.SimpleBooleanProperty
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


class Editor(val folder: File, val editorPane: EditorPane) {

    val paneMap: Map<JsonStruct, Pane> = WeakHashMap()
    val gameObject: Game = GameObject().run {
        Parser.buildStruct(this, JsonHandler.OBJECT_MAPPER.readTree(folder.resolve("data.json")))
        this.produceImperfectADT()
    }
    val mainPane: StackPane = StackPane()
    val iconGraphic: Image
    val tab: Tab
    val dirtyProperty = SimpleBooleanProperty(false)
    var isDirty: Boolean
        get() = dirtyProperty.get()
        set(value) = dirtyProperty.set(value)

    init {
        val iconFile = folder.resolve("icon.png").takeIf { it.exists() }
        iconGraphic = if (iconFile != null) Image("file:${iconFile.path}") else GameRegistry.missingIconImage
        tab = Tab(folder.name, mainPane).apply tab@{
            graphic = ImageView(iconGraphic).apply {
                this.isPreserveRatio = true
                this.fitWidth = 1.5.em
                this.fitHeight = 1.5.em
            }
            dirtyProperty.addListener { _, _, v ->
                this.text = "${folder.name}${if (isDirty) "*" else ""}"
            }
            setOnClosed {
                editorPane.removeEditor(this@Editor)
            }
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

        val gamePane = (getPane(gameObject) as? GameObjPane)

        if (gamePane == null) {
            mainPane.children += pickFirstLabel
        } else {
            mainPane.children += gamePane
        }
    }

    fun getPane(struct: JsonStruct): Pane? {
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

    private fun createPaneFromStruct(struct: JsonStruct): Pane? {
        return when (struct) {
            is Game -> GameObjPane(this)
            is Cue -> CueObjPane(this, struct)
            is Pattern -> PatternObjPane(this, struct)
            is KeepTheBeat -> KeepTheBeatObjPane(this, struct)
            is Equidistant -> EquidistantObjPane(this, struct)
            is RandomCue -> RandomCueObjPane(this, struct)
            is CuePointer -> null
            is SubtitleEntity, is ShakeEntity, is EndRemixEntity, is TextureEntity, is PlayalongEntity, is TapeMeasure, is MusicDistortEntity -> null
            else -> {
                RSDE.LOGGER.warn("JsonStruct ${struct::class.java.name} is not supported for editing, implicitly returning null")
                null
            }
        }
    }

    fun markDirty() {
        isDirty = true
    }

    fun update() {
        paneMap as MutableMap
        paneMap.values.forEach { if (it is StructPane<*>) it.update() }
        paneMap.keys.filter { it !in gameObject.objects }.filterIsInstance<Datamodel>().forEach { paneMap.remove(it) }
    }

    fun refreshLists() {
        paneMap.values.forEach { (it as? StructPane<*>)?.refreshLists() }
        editorPane.structurePane.treeView.refresh()
    }

    fun switchToPane(pane: Pane?) {
        mainPane.children.clear()
        mainPane.children += pane ?: pickFirstLabel
    }

}
