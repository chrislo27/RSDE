package io.github.chrislo27.rhre3.sfxdb.gui.editor

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.scene.EditorPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.ExceptionAlert
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.validation.*
import javafx.scene.control.Label
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback


class StructurePane(val editorPane: EditorPane) : VBox(), EditorUpdateable {

    private val app: RSDE get() = editorPane.app

    val treeView: TreeView<DataNode> = TreeView<DataNode>().apply {
        this.cellFactory = Callback { DataNodeCell() }
    }

    init {
        stylesheets += "style/structure.css"
        VBox.setVgrow(treeView, Priority.ALWAYS)

        children += Label().bindLocalized("editor.structure").apply {
            id = "label"
        }
        children += treeView

        treeView.setOnMouseClicked {evt ->
            val node = treeView.selectionModel.selectedItem
            val item = node?.value
            if (item != null && evt.button == MouseButton.PRIMARY && evt.clickCount >= (if (node == treeView.root) 1 else 2)) {
                val pane = try {
                    item.editor.getPane(item.struct)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    ExceptionAlert(e).showAndWait()
                    return@setOnMouseClicked
                }
                item.editor.switchToPane(pane)
            }
        }
    }

    override fun update(currentEditor: Editor?) {
        if (currentEditor == null) {
            treeView.root = null
            return
        }
        val gameObj = currentEditor.gameObject

        // Build tree
        val root = TreeItem(DataNode(this, currentEditor, gameObj, gameObj.id.orException(), Transformers.anyNonSuccess(gameObj)))
        gameObj.objects.orNull()?.forEach { obj ->
            if (obj is Result.Unset) return@forEach
            val datamodel = if (obj is Result.Failure) obj.passedIn as DatamodelObject else (obj as Result.Success).value
            val invalid = obj !is Result.Success

            root.children += TreeItem(DataNode(this, currentEditor, datamodel, "${datamodel.id.orElse("? ID ?")} (${datamodel.name.orElse("???")})", invalid))
        }

        root.isExpanded = true

        treeView.root = root
    }

    class DataNode(
        val structure: StructurePane,
        val editor: Editor,
        val struct: Struct, val text: String, val invalid: Boolean
    )

    class DataNodeCell : TreeCell<DataNode>() {
        override fun updateItem(item: DataNode?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                text = ""
            } else {
                text = item.text
                if (item.invalid) {
                    if ("bad-data-node" !in styleClass) styleClass += "bad-data-node"
                } else {
                    styleClass -= "bad-data-node"
                }
            }
        }
    }

}
