package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.discord.ChangesPresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DefaultRichPresence
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DiscordHelper
import io.github.chrislo27.rhre3.sfxdb.gui.discord.PresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.editor.HasValidator
import io.github.chrislo27.rhre3.sfxdb.gui.editor.StructurePane
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.UiLocalization
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import org.controlsfx.control.StatusBar
import org.controlsfx.validation.ValidationResult
import java.io.File
import java.nio.file.Files
import java.text.DecimalFormat
import java.util.concurrent.Callable
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.measureNanoTime


class EditorPane(val app: RSDE) : BorderPane(), ChangesPresenceState {

    val toolbar: MenuBar
    val splitPane: SplitPane = SplitPane()
    val centreTabPane: TabPane = TabPane().apply {
        this.side = Side.TOP
        this.tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
    }
    //    val leftTabPane: TabPane = TabPane().apply {
//        this.side = Side.LEFT
//    }
//    val rightTabPane: TabPane = TabPane().apply {
//        this.side = Side.RIGHT
//    }
    val bottomPane: Pane = VBox()

    val editors: ObservableList<Editor> = FXCollections.observableArrayList()
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
        center = splitPane
//        left = leftTabPane
//        right = rightTabPane
        bottom = bottomPane

        structurePane = StructurePane(this)

        splitPane.items.addAll(structurePane, centreTabPane)
        splitPane.setDividerPosition(0, 0.3)

        toolbar.menus += Menu().bindLocalized("editor.toolbar.file").apply {
            items += MenuItem().bindLocalized("editor.toolbar.file.welcomeScreen").apply {
                setOnAction {
                    app.primaryStage.scene.root = WelcomePane(app)
                }
                accelerator = KeyCombination.keyCombination("Shortcut+Shift+W")
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
                        Files.walk(sourceDirPath).filter { !Files.isDirectory(it) && it.toFile().name != "OLD-data.json" }.forEach { path ->
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
        toolbar.menus += Menu().bindLocalized("editor.toolbar.analyze").apply {
            items += MenuItem().bindLocalized("editor.toolbar.analyze.validateCurrent").apply {
                setOnAction { _ ->
                    val currentEditor = currentEditor
                    val validator = (currentEditor?.getPane(currentEditor.gameObject) as? HasValidator)
                    if (currentEditor == null || validator == null) {
                        statusBar.text = Localization["editor.status.validation.none"]
                    } else {
                        val time = (measureNanoTime {
                            validator.forceUpdate()
                        } / 1_000_000.0)
                        val result = validator.getValidationResult()
                        statusBar.text = Localization["editor.status.validation", DecimalFormat("0.000").format(time), result.errors.size, result.warnings.size]
                    }
                }
                accelerator = KeyCombination.keyCombination("Shortcut+F9")
                disableProperty().bind(Bindings.createBooleanBinding(Callable { currentEditor == null }, centreTabPane.selectionModel.selectedItemProperty()))
            }
            items += MenuItem().bindLocalized("editor.toolbar.analyze.validateAll").apply {
                setOnAction { _ ->
                    val validators = editors.mapNotNull { it.getPane(it.gameObject) as? HasValidator }
                    if (validators.isEmpty()) {
                        statusBar.text = Localization["editor.status.validation.none"]
                    } else {
                        val time = (measureNanoTime {
                            validators.forEach(HasValidator::forceUpdate)
                        } / 1_000_000.0)
                        val result = validators.fold(ValidationResult()) { acc, it -> acc.combine(it.getValidationResult()) }
                        statusBar.text = Localization["editor.status.validation.all", DecimalFormat("0.000").format(time), result.errors.size, result.warnings.size]
                    }
                }
                accelerator = KeyCombination.keyCombination("Shortcut+Shift+F9")
                disableProperty().bind(Bindings.isEmpty(editors))
            }
        }
        toolbar.menus += Menu().bindLocalized("editor.toolbar.about").apply {
            items += MenuItem().bindLocalized("editor.toolbar.about.docs").apply {
                setOnAction { _ ->
                    val docsTab: DocsTab? = centreTabPane.tabs.firstOrNull { it is DocsTab } as DocsTab?
                    if (docsTab == null) {
                        val newTab = DocsTab()
                        newTab.textProperty().bind(UiLocalization["editor.toolbar.about.docs"])
                        newTab.loadDocs()
                        centreTabPane.tabs += newTab
                        centreTabPane.selectionModel.select(newTab)
                    } else {
                        centreTabPane.selectionModel.select(docsTab)
                        if (docsTab.engine.location != docsTab.docsUrl) {
                            docsTab.loadDocs()
                        }
                    }
                }
                accelerator = KeyCombination.keyCombination("Shortcut+Shift+D")
            }
            items += MenuItem().bindLocalized("editor.toolbar.about.checkUpdates").apply {
                setOnAction { _ ->
                    app.hostServices.showDocument(RSDE.GITHUB + "/releases")
                }
            }
            items += MenuItem().bindLocalized("editor.toolbar.about.about").apply {
                setOnAction { _ ->
                    val aboutTab: AboutPane.AboutTab? = centreTabPane.tabs.firstOrNull { it is AboutPane.AboutTab } as AboutPane.AboutTab?
                    if (aboutTab == null) {
                        val newTab = AboutPane.AboutTab(app)
                        newTab.textProperty().bind(UiLocalization["editor.toolbar.about.about"])
                        centreTabPane.tabs += newTab
                        centreTabPane.selectionModel.select(newTab)
                    } else {
                        centreTabPane.selectionModel.select(aboutTab)
                    }
                }
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
        if (currentEditor == null) {
            val rpObj = when (centreTabPane.selectionModel.selectedItem) {
                is DocsTab -> PresenceState.ViewingSomething("documentation").toRichPresenceObj()
                is AboutPane.AboutTab -> PresenceState.ViewingSomething("About page").toRichPresenceObj()
                else -> null
            }
            if (rpObj != null) return rpObj
        }
        return PresenceState.InEditor(currentEditor?.folder?.name).toRichPresenceObj()
    }

    fun addEditor(editor: Editor) {
        editors += editor
        centreTabPane.tabs += editor.tab
        fireUpdate()
    }

    fun removeEditor(editor: Editor) {
        editors -= editor
        centreTabPane.tabs -= editor.tab
        fireUpdate()
    }

    fun attemptSave(editor: Editor): Pair<Int, Int> {
        // Check validation for all
        val mainPane = (editor.getPane(editor.gameObject) as? HasValidator) ?: return 1 to 0
        mainPane.forceUpdate()
        val result = mainPane.getValidationResult()
        if (result.errors.isNotEmpty()) {
            return result.errors.size to result.warnings.size
        }

        val datajson = editor.folder.resolve("data.json")
        datajson.copyTo(editor.folder.resolve("OLD-data.json"), true)
        datajson.writeText(JsonHandler.OBJECT_MAPPER.writeValueAsString(editor.gameObject))
        return 0 to result.warnings.size
    }

    inner class DocsTab(docsBranch: String = "dev") : Tab() {
        val docsUrl = RSDE.getDocsUrl(docsBranch)
        val webView = WebView()
        val engine: WebEngine = webView.engine
        val progressBar = ProgressBar().apply {
            this.progressProperty().bind(engine.loadWorker.progressProperty())
            this.visibleProperty().bind(Bindings.lessThan(this.progressProperty(), 1.0))
        }

        init {
            this.content = webView

            setOnClosed {
                statusBar.rightItems -= progressBar
            }
        }

        fun loadDocs() {
            engine.load(docsUrl)
            if (progressBar !in statusBar.rightItems) {
                statusBar.rightItems += progressBar
            }
        }
    }

}