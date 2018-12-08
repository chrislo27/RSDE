package io.github.chrislo27.rhre3.sfxdb.gui.editor

import io.github.chrislo27.rhre3.sfxdb.adt.Datamodel
import io.github.chrislo27.rhre3.sfxdb.adt.Game
import io.github.chrislo27.rhre3.sfxdb.adt.JsonStruct
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.editor.panes.GameObjPane
import io.github.chrislo27.rhre3.sfxdb.gui.scene.EditorPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.*
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback


class StructurePane(val editorPane: EditorPane) : VBox() {

    private val app: RSDE get() = editorPane.app

    val treeView: TreeView<DataNode> = TreeView<DataNode>().apply {
        this.cellFactory = Callback { DataNodeCell(app) }
    }

    init {
        stylesheets += "style/structure.css"
        VBox.setVgrow(treeView, Priority.ALWAYS)

        children += BorderPane().apply {
            alignment = Pos.CENTER_LEFT
            this.left = HBox().apply {
                children += Label().bindLocalized("editor.structure").apply {
                    id = "label"
                    alignment = Pos.CENTER_LEFT
                }
                this.children += Button("", ImageView(Image("/image/ui/add.png", 16.0, 16.0, true, true, true))).apply {
                    id = "add-button"
                    setOnAction { _ ->
                        val editor = editorPane.currentEditor ?: return@setOnAction
                        val pane: GameObjPane = (editor.getPane(editor.gameObject) as? GameObjPane) ?: return@setOnAction
                        editor.switchToPane(pane)
                        Platform.runLater {
                            pane.addButton.fire()
                        }
                    }
                    visibleProperty().bind(Bindings.isNotNull(editorPane.currentEditorProperty))
                    tooltip = Tooltip().bindLocalized("editor.add.datamodel")
                }
            }
        }
        children += treeView

        treeView.setOnMouseClicked { evt ->
            val node = treeView.selectionModel.selectedItem
            val item = node?.value
            if (item != null && evt.button == MouseButton.PRIMARY && evt.clickCount >= 1) {
                val pane = try {
                    item.editor.getPane(item.struct)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    ExceptionAlert(app, e).showAndWait()
                    return@setOnMouseClicked
                }
                item.editor.switchToPane(pane)
            }
        }
    }

    fun update(currentEditor: Editor?) {
        if (currentEditor == null) {
            treeView.root = null
            return
        }
        val gameObj = currentEditor.gameObject

        // Build tree
        val root = TreeItem(DataNode(this, currentEditor, gameObj, gameObj.id))
        gameObj.objects.forEach { obj ->
            val datamodel = obj

            root.children += TreeItem(DataNode(this, currentEditor, datamodel, if (datamodel.id.isBlank()) "${datamodel.type} ${Localization["editor.missingID"]}" else "${datamodel.id} (${datamodel.name})"))
        }

        root.isExpanded = true

        treeView.root = root
        treeView.refresh()
    }

    class DataNode(
        val structure: StructurePane,
        val editor: Editor,
        val struct: JsonStruct, val text: String
    )

    class DataNodeCell(private val app: RSDE) : TreeCell<DataNode>() {
        private val datamodelContextMenu: ContextMenu = ContextMenu().apply {
            items += MenuItem("", ImageView(Image("/image/ui/remove.png", 16.0, 16.0, true, true, true))).apply {
                bindLocalized("editor.remove")
                setOnAction { _ ->
                    val item = item ?: return@setOnAction
                    val datamodel = (item.struct as? Datamodel) ?: return@setOnAction
                    val dialog = Alert(Alert.AlertType.CONFIRMATION).apply {
                        val text = UiLocalization["editor.removeObjectConfirm"]
                        this.titleProperty().bind(text)
                        this.contentTextProperty().bind(text)
                        this.addWindowIcons()
                        this.dialogPane?.let {
                            app.addBaseStyleToDialog(it)
                        }
                    }
                    val result = dialog.showAndWait()
                    if (result.orElse(null) == ButtonType.OK) {
                        val editor = item.editor
                        if (editor.mainPane.children.first() == editor.getPane(datamodel)) {
                            editor.switchToPane(null)
                        }
                        editor.gameObject.objects.remove(datamodel)
                        editor.editorPane.fireUpdate()
                    }
                }
            }
        }
        private val gameContextMenu: ContextMenu = ContextMenu().apply {
            items += MenuItem().apply {
                bindLocalized("editor.openFolderLocation")
                setOnAction { _ ->
                    val item = item ?: return@setOnAction
                    item.structure.app.hostServices.showDocument("file://${item.editor.folder.absolutePath}")
                }
            }
        }

        override fun updateItem(item: DataNode?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                text = ""
                contextMenu = null
                graphic = null
            } else {
                text = item.text
                contextMenu = when (item.struct) {
                    is Datamodel -> datamodelContextMenu
                    is Game -> gameContextMenu
                    else -> null
                }
                graphic = if (item.struct is Game) ImageView(item.editor.iconGraphic).apply {
                    this.isPreserveRatio = true
                    this.fitWidth = 1.5.em
                    this.fitHeight = 1.5.em
                } else null
                val pane = item.editor.getPane(item.struct)
                if (pane != null && pane is HasValidator) {
                    val style = "bad-list-cell"
                    if (pane.hasErrors()) {
                        if (style !in styleClass) styleClass += style
                    } else {
                        styleClass -= style
                        val warningStyle = "warning-list-cell"
                        if (pane.getValidationResult().warnings.isNotEmpty()) {
                            if (warningStyle !in styleClass) styleClass += warningStyle
                        } else {
                            styleClass -= warningStyle
                        }
                    }
                }
            }
        }
    }

}
