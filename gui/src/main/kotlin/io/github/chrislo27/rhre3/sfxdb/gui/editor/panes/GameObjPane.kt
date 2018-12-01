package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.gui.util.UiLocalization
import io.github.chrislo27.rhre3.sfxdb.gui.util.addWindowIcons
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import io.github.chrislo27.rhre3.sfxdb.validation.*
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.util.Callback


class GameObjPane(editor: Editor) : StructPane<GameObject>(editor, editor.gameObject) {

    val idField = TextField(gameObject.id.orElse("??? MISSING ID ???"))
    val nameField = TextField(gameObject.name.orElse("MISSING NAME"))
    val seriesComboBox =
        ComboBox<Series>(FXCollections.observableArrayList(Series.VALUES - listOf(Series.SWITCH))).apply {
            this.selectionModel.select(gameObject.series.orNull())
        }
    val groupField = TextField(gameObject.group.orElse(""))
    val groupDefaultCheckbox = CheckBox().apply {
        this.isSelected = gameObject.groupDefault.orElse(false)
    }
    val prioritySpinner = Spinner<Int>(-128, 127, gameObject.priority.orElse(0))
    val searchHintsField =
        ChipPane(FXCollections.observableArrayList(gameObject.searchHints.orElse(listOf()).map { Chip(it) }))
    val noDisplayCheckbox = CheckBox().apply {
        this.isSelected = gameObject.noDisplay.orElse(false)
    }

    val objectsGrid: GridPane = GridPane().apply {
        styleClass += "multipart-objects-pane"
    }
    val addButton: MenuButton = MenuButton("", ImageView(Image("/image/ui/add.png", 16.0, 16.0, true, true, true)))
    val removeButton: Button = Button("", ImageView(Image("/image/ui/remove.png", 16.0, 16.0, true, true, true)))
    val moveUpButton: Button = Button("", ImageView(Image("/image/ui/up.png", 16.0, 16.0, true, true, true)))
    val moveDownButton: Button = Button("", ImageView(Image("/image/ui/down.png", 16.0, 16.0, true, true, true)))
    val objectsListView: ListView<Struct> = ListView<Struct>(FXCollections.observableArrayList()).apply {
        this.cellFactory = Callback {
            object : ListCell<Struct>() {
                override fun updateItem(item: Struct?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (item == null || empty) {
                        null
                    } else {
                        (item as? DatamodelObject)?.id?.orElse(Localization["editor.missingID"])
                    }
                }
            }
        }
    }

    init {
        titleLabel.textProperty().bind(idField.textProperty())

        addProperty(Label().bindLocalized("datamodel.id"), idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("gameObject.series"), seriesComboBox)
        addProperty(Label().bindLocalized("gameObject.group"), groupField)
        addProperty(Label().bindLocalized("gameObject.groupDefault"), groupDefaultCheckbox)
        addProperty(Label().bindLocalized("gameObject.priority"), prioritySpinner)
        addProperty(Label().bindLocalized("gameObject.searchHints"), searchHintsField)
        addProperty(Label().bindLocalized("gameObject.noDisplay"), noDisplayCheckbox)

        centreVbox.children += Separator().apply {
            maxWidth = Double.MAX_VALUE
        }

        centreVbox.children += objectsGrid
        objectsGrid.apply {
            add(Label().bindLocalized("gameObject.objects"), 0, 0)
            add(addButton, 0, 1)
            add(removeButton, 1, 1)
            add(moveUpButton, 2, 1)
            add(moveDownButton, 3, 1)
            add(objectsListView, 0, 2, 5, 1)
        }

        addButton.items.addAll(
            MenuItem("CueObject"),
            SeparatorMenuItem(),
            MenuItem("PatternObject"),
            MenuItem("EquidistantObject"),
            MenuItem("KeepTheBeatObject"),
            MenuItem("RandomCueObject")
        )
        removeButton.setOnAction {
            val current = objectsListView.selectionModel.selectedItem
            if (current != null) {
                val dialog = Alert(Alert.AlertType.CONFIRMATION).apply {
                    this.titleProperty().bind(UiLocalization["editor.removeObjectConfirm"])
                    this.contentTextProperty().bind(UiLocalization["editor.removeObjectConfirm"])
                    this.addWindowIcons()
                }
                val result = dialog.showAndWait()
                if (result.orElse(null) == ButtonType.OK) {
                    gameObject.objects.orNull()?.removeIf { obj -> obj is Result.Success && obj.value == current }
                    editor.editorPane.fireUpdate()
                }
            }
        }
        moveUpButton.setOnAction { _ ->
            val current = objectsListView.selectionModel.selectedItem
            if (current != null) {
                val list = gameObject.objects.orException()
                val index = list.indexOfFirst { it is Result.Success && it.value == current }
                if (index != -1 && index - 1 >= 0) {
                    val removed = list.removeAt(index)
                    list.add(index - 1, removed)
                    editor.editorPane.fireUpdate()
                    objectsListView.selectionModel.select(index - 1)
                }
            }
        }
        moveDownButton.setOnAction { _ ->
            val current = objectsListView.selectionModel.selectedItem
            if (current != null) {
                val list = gameObject.objects.orException()
                val index = list.indexOfFirst { it is Result.Success && it.value == current }
                if (index != -1 && index + 1 < list.size - 1) {
                    val removed = list.removeAt(index)
                    list.add(index + 1, removed)
                    editor.editorPane.fireUpdate()
                    objectsListView.selectionModel.select(index + 1)
                }
            }
        }

        objectsListView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            removeButton.isDisable = newValue == null
            moveUpButton.isDisable = newValue == null && objectsListView.selectionModel.selectedIndex > 0
            moveDownButton.isDisable = newValue == null && objectsListView.selectionModel.selectedIndex < objectsListView.items.size - 1
        }

        updateObjectsList()
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.GAME_ID)
        validation.registerValidator(nameField, Validators.NAME_BLANK)
        validation.registerValidator(noDisplayCheckbox, Validators.NO_DISPLAY)
    }

    override fun update() {
        super.update()
        updateObjectsList()
    }

    private fun updateObjectsList() {
        objectsListView.items.apply {
            clear()
            gameObject.objects.orNull()?.let { dObjRes ->
                dObjRes.filterIsInstance<Result.Success<DatamodelObject>>().map { it.value }.forEach {
                    this.add(it)
                }
            }
        }
    }

    inner class AddMenuItem(text: String) : MenuItem(text) {

    }

}