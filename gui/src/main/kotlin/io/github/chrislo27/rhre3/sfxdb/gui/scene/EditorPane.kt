package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.discord.ChangesPresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DefaultRichPresence
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DiscordHelper
import io.github.chrislo27.rhre3.sfxdb.gui.discord.PresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.editor.StructurePane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.panes.StructPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import javafx.application.Platform
import javafx.geometry.Side
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.controlsfx.control.StatusBar
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class EditorPane(val app: RSDE) : BorderPane(), ChangesPresenceState {

    val toolbar: MenuBar
    val centreTabPane: TabPane = TabPane().apply {
        this.side = Side.TOP
    }
    //    val leftTabPane: TabPane = TabPane().apply {
//        this.side = Side.LEFT
//    }
//    val rightTabPane: TabPane = TabPane().apply {
//        this.side = Side.RIGHT
//    }
    val bottomPane: Pane = VBox()

    val editors: List<Editor> = mutableListOf()
    val currentEditor: Editor?
        get() {
            val currentTab = centreTabPane.selectionModel.selectedItem ?: return null
            return editors.find { it.tab == currentTab }
        }

    val structurePane: StructurePane
    val statusBar: StatusBar = StatusBar().apply {
        this.text = ""
    }

    init {
        stylesheets += "style/editorPane.css"

        toolbar = MenuBar()
        top = toolbar
        center = centreTabPane
//        left = leftTabPane
//        right = rightTabPane
        bottom = bottomPane

        structurePane = StructurePane(this)
        left = structurePane

        toolbar.menus += Menu().bindLocalized("editor.toolbar.file").apply {
            items += MenuItem().bindLocalized("editor.toolbar.file.welcomeScreen").apply {
                setOnAction {
                    app.primaryStage.scene.root = WelcomePane(app)
                }
                accelerator = KeyCombination.keyCombination("Shortcut+W")
            }
            items += MenuItem().bindLocalized("editor.toolbar.file.save").apply {
                setOnAction {
                    val editor = currentEditor ?: return@setOnAction
                    val attemptSave = attemptSave(editor)
                    statusBar.text = if (attemptSave.first == 0) {
                        Localization["editor.status.save.successful", attemptSave.second]
                    } else Localization["editor.status.save.cannot", attemptSave.first, attemptSave.second]
                }
                accelerator = KeyCombination.keyCombination("Shortcut+S")
            }
            items += MenuItem().bindLocalized("editor.toolbar.file.export").apply {
                setOnAction { _ ->
                    val editor = currentEditor ?: return@setOnAction
                    val fileChooser = FileChooser().apply {
                        this.initialFileName = editor.folder.name + ".zip"
                        this.title = Localization["editor.export.title", editor.folder.name]
                        this.selectedExtensionFilter = FileChooser.ExtensionFilter(Localization["editor.export.filetype"], ".zip")
                        this.extensionFilters += this.selectedExtensionFilter
                    }
                    val file: File = fileChooser.showSaveDialog(app.primaryStage) ?: return@setOnAction
                    val zipFilePath = file.toPath()
                    val zos = ZipOutputStream(Files.newOutputStream(zipFilePath))
                    zos.use {
                        val sourceDirPath = editor.folder.toPath()
                        Files.walk(sourceDirPath).filter { !Files.isDirectory(it) }.forEach { path ->
                            val zipEntry = ZipEntry(sourceDirPath.relativize(path).toString())
                            zos.putNextEntry(zipEntry)
                            zos.write(Files.readAllBytes(path))
                            zos.closeEntry()

                            statusBar.text = Localization["editor.status.export.successful"]
                        }
                    }

                }
                accelerator = KeyCombination.keyCombination("Shortcut+E")
            }
        }

        centreTabPane.selectionModel.selectedItemProperty().addListener { _, oldValue, newValue ->
            if (oldValue != newValue) {
                DiscordHelper.updatePresence(getPresenceState())

                fireUpdate()
            }
        }

        bottomPane.children += statusBar

        Platform.runLater {
            fireUpdate()
        }
    }

    fun fireUpdate() {
        structurePane.update(currentEditor)
        editors.forEach { it.update() }
    }

    override fun getPresenceState(): DefaultRichPresence {
        return PresenceState.InEditor(currentEditor?.folder?.name).toRichPresenceObj()
    }

    fun addEditor(editor: Editor) {
        editors as MutableList
        editors += editor
        centreTabPane.tabs += editor.tab
        fireUpdate()
    }

    fun removeEditor(editor: Editor) {
        editors as MutableList
        editors -= editor
        centreTabPane.tabs -= editor.tab
        fireUpdate()
    }

    fun attemptSave(editor: Editor): Pair<Int, Int> {
        // Check validation for all
        val allPanes = (listOf(editor.gameObject) + editor.gameObject.objects).mapNotNull { editor.paneMap[it] }.filterIsInstance<StructPane<*>>()
        allPanes.forEach {
            it.validation.initInitialDecoration()
        }
        val warnings = allPanes.sumBy { it.validation.validationResult.warnings.size }
        if (allPanes.any { it.validation.isInvalid }) {
            return allPanes.sumBy { it.validation.validationResult.errors.size } to warnings
        }

        val datajson = editor.folder.resolve("data.json")
        datajson.copyTo(editor.folder.resolve("OLD-data.json"), true)
        datajson.writeText(JsonHandler.OBJECT_MAPPER.writeValueAsString(editor.gameObject))
        return 0 to warnings
    }

}