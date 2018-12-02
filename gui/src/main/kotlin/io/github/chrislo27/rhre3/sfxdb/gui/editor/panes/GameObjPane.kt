package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.adt.*
import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.*
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.GridPane
import javafx.util.Callback


class GameObjPane(editor: Editor) : StructPane<Game>(editor, editor.gameObject) {

    val idField = TextField(struct.id)
    val nameField = TextField(struct.name)
    val seriesComboBox =
        ComboBox<Series>(FXCollections.observableArrayList(Series.VALUES - listOf(Series.SWITCH))).apply {
            this.selectionModel.select(struct.series)
        }
    val groupField = TextField(struct.group)
    val groupDefaultCheckbox = CheckBox().apply {
        this.isSelected = struct.groupDefault
    }
    val prioritySpinner = Spinner<Int>(-128, 127, struct.priority)
    val searchHintsField = ChipPane(FXCollections.observableArrayList((struct.searchHints ?: mutableListOf()).map { Chip(it) }))
    val noDisplayCheckbox = CheckBox().apply {
        this.isSelected = struct.noDisplay
    }

    val objectsGrid: GridPane = GridPane().apply {
        styleClass += "multipart-objects-pane"
    }
    val addButton: MenuButton = MenuButton("", ImageView(Image("/image/ui/add.png", 16.0, 16.0, true, true, true)))
    val removeButton: Button = Button("", ImageView(Image("/image/ui/remove.png", 16.0, 16.0, true, true, true)))
    val moveUpButton: Button = Button("", ImageView(Image("/image/ui/up.png", 16.0, 16.0, true, true, true)))
    val moveDownButton: Button = Button("", ImageView(Image("/image/ui/down.png", 16.0, 16.0, true, true, true)))
    val objectsListView: ListView<JsonStruct> = ListView<JsonStruct>(FXCollections.observableArrayList()).apply {
        this.cellFactory = Callback {
            object : ListCell<JsonStruct>() {
                override fun updateItem(item: JsonStruct?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (item == null || empty) {
                        null
                    } else {
                        (item as? Datamodel)?.id.takeUnless { it.isNullOrEmpty() } ?: Localization["editor.missingID"]
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
            AddMenuItem("CueObject") { Cue("", "", mutableListOf(), 0f) },
            SeparatorMenuItem(),
            AddMenuItem("PatternObject") { Pattern("", "", mutableListOf(), mutableListOf()) },
            AddMenuItem("EquidistantObject") { Equidistant("", "", mutableListOf(), mutableListOf()) },
            AddMenuItem("KeepTheBeatObject") { KeepTheBeat("", "", mutableListOf(), mutableListOf()) },
            AddMenuItem("RandomCueObject") { RandomCue("", "", mutableListOf(), mutableListOf()) }
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
                    gameObject.objects.remove(current)
                    editor.editorPane.fireUpdate()
                }
            }
        }
        moveUpButton.setOnAction { _ ->
            val current = objectsListView.selectionModel.selectedItem
            if (current != null) {
                val list = gameObject.objects
                val index = list.indexOf(current)
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
                val list = gameObject.objects
                val index = list.indexOf(current)
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
        objectsListView.setOnMouseClicked { evt ->
            val item = objectsListView.selectionModel.selectedItem
            if (item != null && evt.button == MouseButton.PRIMARY && evt.clickCount >= 2) {
                val pane = try {
                    editor.getPane(item)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    ExceptionAlert(e).showAndWait()
                    return@setOnMouseClicked
                }
                editor.switchToPane(pane)
            }
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
            this.addAll(gameObject.objects)
        }
    }

    inner class AddMenuItem(text: String, factory: () -> Datamodel) : MenuItem(text) {
        init {
            setOnAction {
                val datamodel: Datamodel = factory()
                gameObject.objects.add(datamodel)
                val pane = try {
                    editor.getPane(datamodel)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    ExceptionAlert(e).showAndWait()
                    return@setOnAction
                }
                editor.switchToPane(pane)
                editor.editorPane.fireUpdate()
            }
        }
    }

}