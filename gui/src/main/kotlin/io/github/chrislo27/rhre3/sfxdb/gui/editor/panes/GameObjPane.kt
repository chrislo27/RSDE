package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.adt.*
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.editor.HasValidator
import io.github.chrislo27.rhre3.sfxdb.gui.ui.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.ui.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.*
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.GridPane
import javafx.util.Callback
import org.controlsfx.validation.ValidationResult


class GameObjPane(editor: Editor) : StructPane<Game>(editor, editor.gameObject), HasValidator {

    val idField = TextField(struct.id).apply {
        isEditable = false
        isDisable = true
    }
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
    val copyButton: Button = Button("", ImageView(Image("/image/ui/copy.png", 16.0, 16.0, true, true, true)))
    val moveUpButton: Button = Button("", ImageView(Image("/image/ui/up.png", 16.0, 16.0, true, true, true)))
    val moveDownButton: Button = Button("", ImageView(Image("/image/ui/down.png", 16.0, 16.0, true, true, true)))
    val objectsListView: ListView<JsonStruct> = ListView<JsonStruct>(FXCollections.observableArrayList()).apply {
        this.cellFactory = Callback { _ ->
            object : ListCell<JsonStruct>() {
                override fun updateItem(item: JsonStruct?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (item == null || empty) {
                        null
                    } else {
                        val pane = editor.getPane(item) as? HasValidator
                        if (pane != null) {
                            val anyErrors = pane.hasErrors()
                            val sClass = "bad-list-cell"
                            if (anyErrors) {
                                if (sClass !in styleClass) this.styleClass += sClass
                            } else {
                                if (sClass in styleClass) this.styleClass -= sClass
                                val warningStyle = "warning-list-cell"
                                if (pane.getValidationResult().warnings.isNotEmpty()) {
                                    if (warningStyle !in styleClass) styleClass += warningStyle
                                } else {
                                    styleClass -= warningStyle
                                }
                            }
                        }
                        (item as? Datamodel)?.id.takeUnless { it.isNullOrEmpty() } ?: Localization["editor.missingID"]
                    }
                }
            }
        }
    }

    init {
        titleLabel.textProperty().bind(idField.textProperty())

        addProperty(Label().bindLocalized("datamodel.id").apply {
            tooltip = Tooltip().bindLocalized("editor.cannotEditGameIDs")
        }, idField)
        addProperty(Label().bindLocalized("datamodel.name"), nameField)
        addProperty(Label().bindLocalized("gameObject.series").apply {
            tooltip = Tooltip().bindLocalized("gameObject.series.tooltip")
        }, seriesComboBox)
        addProperty(Label().bindLocalized("gameObject.group").apply {
            tooltip = Tooltip().bindLocalized("gameObject.group.tooltip")
        }, groupField)
        addProperty(Label().bindLocalized("gameObject.groupDefault").apply {
            tooltip = Tooltip().bindLocalized("gameObject.groupDefault.tooltip")
        }, groupDefaultCheckbox)
        addProperty(Label().bindLocalized("gameObject.priority").apply {
            tooltip = Tooltip().bindLocalized("gameObject.priority.tooltip")
        }, prioritySpinner)
        addProperty(Label().bindLocalized("gameObject.searchHints").apply {
            tooltip = Tooltip().bindLocalized("gameObject.searchHints.tooltip")
        }, searchHintsField)
        addProperty(Label().bindLocalized("gameObject.noDisplay").apply {
            tooltip = Tooltip().bindLocalized("gameObject.noDisplay.tooltip")
        }, noDisplayCheckbox)

        centreVbox.children += Separator().apply {
            maxWidth = Double.MAX_VALUE
        }

        centreVbox.children += objectsGrid
        objectsGrid.apply {
            add(Label().bindLocalized("gameObject.objects"), 0, 0)
            add(addButton, 0, 1)
            add(removeButton, 1, 1)
            add(copyButton, 2, 1)
            add(moveUpButton, 3, 1)
            add(moveDownButton, 4, 1)
            add(objectsListView, 0, 2, 5, 1)
        }

        addButton.tooltip = Tooltip().bindLocalized("editor.add.datamodel")
        removeButton.tooltip = Tooltip().bindLocalized("editor.remove")
        copyButton.tooltip = Tooltip().bindLocalized("editor.copy")
        moveUpButton.tooltip = Tooltip().bindLocalized("editor.moveUp")
        moveDownButton.tooltip = Tooltip().bindLocalized("editor.moveDown")

        addButton.items.addAll(
            AddMenuItem("CueObject") { Cue("", "", mutableListOf(), 0f) },
            SeparatorMenuItem(),
            AddMenuItem("PatternObject") { Pattern("", "", mutableListOf(), mutableListOf()) },
            AddMenuItem("EquidistantObject") { Equidistant("", "", mutableListOf(), mutableListOf()) },
            AddMenuItem("KeepTheBeatObject") { KeepTheBeat("", "", mutableListOf(), mutableListOf()) },
            AddMenuItem("RandomCueObject") { RandomCue("", "", mutableListOf(), mutableListOf()) }
        )
        val selectionModel = objectsListView.selectionModel
        removeButton.setOnAction {
            val current = selectionModel.selectedItems
            if (current != null && current.isNotEmpty()) {
                val dialog = Alert(Alert.AlertType.CONFIRMATION).apply {
                    val text = UiLocalization[if (current.size == 1) "editor.removeObjectConfirm" else "editor.removeObjectConfirm.multiple"]
                    this.titleProperty().bind(text)
                    this.contentTextProperty().bind(text)
                    this.addWindowIcons()
                    this.dialogPane?.let {
                        editor.editorPane.app.addBaseStyleToDialog(it)
                    }
                }
                val result = dialog.showAndWait()
                if (result.orElse(null) == ButtonType.OK) {
                    gameObject.objects.removeAll(current)
                    editor.editorPane.fireUpdate()
                    editor.markDirty()
                }
            }
        }
        copyButton.setOnAction {
            val current = selectionModel.selectedItem as? Datamodel
            if (current != null) {
                val datamodel = current.copy()
                addDatamodel(datamodel)
                editor.markDirty()
            }
        }
        moveUpButton.setOnAction { _ ->
            val current = selectionModel.selectedItems?.toList()?.filterIsInstance<Datamodel>()
            if (current != null && current.isNotEmpty() && selectionModel.isSelectionContiguous()) {
                val list = gameObject.objects
                val indices = selectionModel.selectedIndices.toList()
                val first = indices.min() ?: -1
                if (indices.isNotEmpty() && first - 1 >= 0) {
                    Platform.runLater {
                        list.removeAll(current)
                        current.forEachIndexed { i, it ->
                            list.add(first - 1 + i, it)
                        }
                        editor.editorPane.fireUpdate()
                        selectionModel.clearSelection()
                        if (indices.size == 2) {
                            // Workaround to a IndexOutOfBoundsException
                            selectionModel.select(indices.first() - 1)
                            selectionModel.select(indices.last() - 1)
                        } else {
                            selectionModel.selectRange(indices.first() - 1, indices.last())
                        }
                        editor.markDirty()
                    }
                }
            }
        }
        moveDownButton.setOnAction { _ ->
            val current = selectionModel.selectedItems?.toList()?.filterIsInstance<Datamodel>()
            if (current != null && current.isNotEmpty() && selectionModel.isSelectionContiguous()) {
                val list = gameObject.objects
                val indices = selectionModel.selectedIndices.toList()
                val first = indices.min() ?: -1
                val last = indices.max() ?: Int.MAX_VALUE
                if (indices.isNotEmpty() && last + 1 < list.size) {
                    Platform.runLater {
                        list.removeAll(current)
                        current.forEachIndexed { i, it ->
                            list.add(first + 1 + i, it)
                        }
                        editor.editorPane.fireUpdate()
                        selectionModel.clearSelection()
                        if (indices.size == 2) {
                            // Workaround to a IndexOutOfBoundsException
                            selectionModel.select(indices.first() + 1)
                            selectionModel.select(indices.last() + 1)
                        } else {
                            selectionModel.selectRange(indices.first() + 1, indices.last() + 2)
                        }
                        editor.markDirty()
                    }
                }
            }
        }

        removeButton.isDisable = true
        copyButton.isDisable = true
        moveUpButton.isDisable = true
        moveDownButton.isDisable = true

        selectionModel.selectionMode = SelectionMode.MULTIPLE
        selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            removeButton.isDisable = newValue == null
            copyButton.isDisable = newValue == null || selectionModel.selectedIndices.size != 1 || selectionModel.selectedItem !is Datamodel
            moveUpButton.isDisable = newValue == null || selectionModel.selectedIndices.min() ?: -1 <= 0 || !selectionModel.isSelectionContiguous()
            moveDownButton.isDisable = newValue == null || selectionModel.selectedIndices.max() ?: Int.MAX_VALUE >= objectsListView.items.size - 1 || !selectionModel.isSelectionContiguous()
        }
        objectsListView.setOnMouseClicked { evt ->
            val item = selectionModel.selectedItem
            if (item != null && evt.button == MouseButton.PRIMARY && evt.clickCount >= 2) {
                val pane = try {
                    editor.getPane(item)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    ExceptionAlert(editor.editorPane.app, e).showAndWait()
                    return@setOnMouseClicked
                }
                editor.switchToPane(pane)
            }
        }

        updateObjectsList()
        refreshLists()
    }

    init {
        // Bind to struct
        idField.textProperty().addListener { _, _, newValue ->
            struct.id = newValue
            editor.markDirty()
        }
        nameField.textProperty().addListener { _, _, newValue ->
            struct.name = newValue
            editor.markDirty()
        }
        seriesComboBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            struct.series = newValue
            editor.markDirty()
        }
        groupField.textProperty().addListener { _, _, newValue ->
            struct.group = newValue?.takeUnless { it.isBlank() }
            editor.markDirty()
        }
        groupDefaultCheckbox.selectedProperty().addListener { _, _, newValue ->
            struct.groupDefault = newValue
            editor.markDirty()
        }
        prioritySpinner.valueProperty().addListener { _, _, newValue ->
            struct.priority = newValue
            editor.markDirty()
        }
        searchHintsField.list.addListener(ListChangeListener { evt ->
            val list = mutableListOf<String>()
            while (evt.next()) {
                list.addAll(evt.list.map { chip -> chip.label.text })
            }
            struct.searchHints = list.distinct().toMutableList().takeUnless { it.isEmpty() }
            editor.markDirty()
        })
        noDisplayCheckbox.selectedProperty().addListener { _, _, newValue ->
            struct.noDisplay = newValue
            editor.markDirty()
        }

        idField.textProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
        nameField.textProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
        noDisplayCheckbox.textProperty().addListener { _, _, _ ->
            editor.refreshLists()
        }
    }

    init {
        // Validators
        validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.GAME_ID)
        validation.registerValidators(nameField, Validators.NAME_BLANK, Validators.appropriateNameSuffixFromSeries(this), Validators.deprecatedFeverName(this))
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

    override fun hasErrors(): Boolean {
        return validation.isInvalid ||
                gameObject.objects.mapNotNull { editor.getPane(it) }.filterIsInstance<HasValidator>().any { it.hasErrors() }
    }

    override fun forceUpdate() {
        validation.initInitialDecoration()
        gameObject.objects.mapNotNull { editor.paneMap[it] }.filterIsInstance<HasValidator>().forEach { it.forceUpdate() }
    }

    override fun getValidationResult(): ValidationResult {
        return gameObject.objects.mapNotNull { editor.paneMap[it] }.filterIsInstance<HasValidator>().fold(validation.validationResult ?: ValidationResult()) { acc, it ->
            acc.combine(it.getValidationResult())
        }
    }

    override fun refreshLists() {
        objectsListView.refresh()
    }

    private fun addDatamodel(datamodel: Datamodel) {
        gameObject.objects.add(datamodel)
        val pane = try {
            editor.getPane(datamodel)
        } catch (e: Throwable) {
            e.printStackTrace()
            ExceptionAlert(editor.editorPane.app, e).showAndWait()
            return
        }
//        editor.switchToPane(pane)
//        (pane as? DatamodelPane<*>)?.let {
//            if (it.idField.text.isEmpty()) {
//                it.idField.text = if (pane.struct is Cue) "*/" else "*_"
//            }
//            it.idField.requestFocus()
//            it.idField.end()
//        }
        editor.editorPane.fireUpdate()
        editor.refreshLists()
    }

    inner class AddMenuItem(text: String, factory: () -> Datamodel) : MenuItem(text) {
        init {
            setOnAction { _ ->
                val datamodel: Datamodel = factory()
                addDatamodel(datamodel)
                editor.markDirty()
            }
        }
    }

}