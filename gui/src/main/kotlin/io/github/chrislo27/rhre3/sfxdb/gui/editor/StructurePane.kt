package io.github.chrislo27.rhre3.sfxdb.gui.editor

import io.github.chrislo27.rhre3.sfxdb.adt.JsonStruct
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.scene.EditorPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.ExceptionAlert
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.validation.Transformers
import javafx.scene.control.Label
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback


class StructurePane(val editorPane: EditorPane) : VBox() {

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

        treeView.setOnMouseClicked { evt ->
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

    fun update(currentEditor: Editor?) {
        if (currentEditor == null) {
            treeView.root = null
            return
        }
        val gameObj = currentEditor.gameObject

        // Build tree
        val root = TreeItem(DataNode(this, currentEditor, gameObj, gameObj.id, Transformers.anyNonSuccess(gameObj)))
        gameObj.objects.forEach { obj ->
            val datamodel = obj

            root.children += TreeItem(DataNode(this, currentEditor, datamodel, if (datamodel.id.isBlank()) "${datamodel.type} ${Localization["editor.missingID"]}" else "${datamodel.id} (${datamodel.name})"))
        }

        root.isExpanded = true

        treeView.root = root
    }

    class DataNode(
        val structure: StructurePane,
        val editor: Editor,
        val struct: JsonStruct, val text: String, val invalid: Boolean = false // FIXME
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
