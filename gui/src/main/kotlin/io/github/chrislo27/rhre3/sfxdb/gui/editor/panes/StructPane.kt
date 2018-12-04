package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.Constants
import io.github.chrislo27.rhre3.sfxdb.adt.*
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.editor.HasValidator
import io.github.chrislo27.rhre3.sfxdb.gui.ui.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.ui.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.*
import io.github.chrislo27.rhre3.sfxdb.gui.validation.L10NValidationSupport
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.scene.text.TextAlignment
import javafx.util.Callback
import javafx.util.StringConverter
import org.controlsfx.validation.ValidationResult
import java.util.*


abstract class StructPane<T : JsonStruct>(val editor: Editor, val struct: T) : BorderPane(), HasValidator {

    val gameObject: Game get() = editor.gameObject

    val titleLabel: Label = Label().apply {
        id = "title"
    }
    val centreVbox: VBox = VBox()
    val gridPane: GridPane = GridPane().apply {
        styleClass += "grid-pane"
    }

    private var gridPaneRowIndex: Int = 0
    val validation = L10NValidationSupport()

    init {
        stylesheets += "style/structPane.css"

        top = titleLabel
        center = ScrollPane(centreVbox).apply {
            this.hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            this.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }

        gridPane.maxWidth = Double.MAX_VALUE
        VBox.setVgrow(gridPane, Priority.ALWAYS)
        centreVbox.children += gridPane

        Platform.runLater {
            validation.initInitialDecoration()
        }
    }

    protected fun addProperty(label: Node, control: Node): Int {
        gridPane.add(label, 0, gridPaneRowIndex)
        gridPane.add(control, 1, gridPaneRowIndex)
        return ++gridPaneRowIndex
    }

    open fun update() {
    }

    override fun isInvalid(): Boolean {
        return validation.isInvalid
    }

    override fun forceUpdate() {
        validation.initInitialDecoration()
    }

    override fun getValidationResult(): ValidationResult {
        return validation.validationResult
    }
}

abstract class DatamodelPane<T : Datamodel>(editor: Editor, struct: T) : StructPane<T>(editor, struct) {
    val idField: TextField = TextField(struct.id)
    val nameField: TextField = TextField(struct.name)
    val deprecatedIDsField: ChipPane = ChipPane(FXCollections.observableArrayList(struct.deprecatedIDs.map { Chip(it) }))

    init {
        titleLabel.textProperty().bind(idField.textProperty())

        // Bind to struct fields
        idField.textProperty().addListener { _, _, newValue -> struct.id = newValue }
        nameField.textProperty().addListener { _, _, newValue -> struct.name = newValue }
        deprecatedIDsField.list.addListener(ListChangeListener { evt ->
            val list = mutableListOf<String>()
            while (evt.next()) {
                list.addAll(evt.list.map { chip -> chip.label.text })
            }
            struct.deprecatedIDs = list
        })
        idField.textProperty().addListener { _, _, _ ->
            editor.editorPane.fireUpdate()
        }
        nameField.textProperty().addListener { _, _, _ ->
            editor.editorPane.fireUpdate()
        }
    }
}

abstract class MultipartStructPane<T : MultipartDatamodel>(editor: Editor, struct: T) : DatamodelPane<T>(editor, struct) {

    abstract val cuesPane: CuesPane<T>

    override fun update() {
        super.update()
        cuesPane.update()
    }

    override fun isInvalid(): Boolean {
        return super.isInvalid() || cuesPane.isInvalid()
    }

    override fun forceUpdate() {
        super.forceUpdate()
        cuesPane.forceUpdate()
    }

    override fun getValidationResult(): ValidationResult {
        return super.getValidationResult().combine(cuesPane.getValidationResult())
    }

    @Suppress("LeakingThis")
    open class CuesPane<T : MultipartDatamodel>(
        val parentPane: MultipartStructPane<T>,
        val paneFactory: (CuePointer, CuesPane<T>) -> CuePointerPane<T>?
    ) : GridPane(), HasValidator {
        val paneMap: Map<CuePointer, CuePointerPane<T>> = WeakHashMap()

        val addButton: Button = Button("", ImageView(Image("/image/ui/add.png", 16.0, 16.0, true, true, true)))
        val removeButton: Button = Button("", ImageView(Image("/image/ui/remove.png", 16.0, 16.0, true, true, true)))
        val moveUpButton: Button = Button("", ImageView(Image("/image/ui/up.png", 16.0, 16.0, true, true, true)))
        val moveDownButton: Button = Button("", ImageView(Image("/image/ui/down.png", 16.0, 16.0, true, true, true)))
        val cuesListView: ListView<CuePointer> = ListView<CuePointer>(FXCollections.observableArrayList()).apply {
            this.cellFactory = Callback { _ ->
                object : ListCell<CuePointer>() {
                    override fun updateItem(item: CuePointer?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (item == null || empty) {
                            null
                        } else {
                            val pane = getPaneForCuePointer(item)
                            if (pane != null) {
                                val anyErrors = pane.isInvalid()
                                val sClass = "bad-list-cell"
                                if (anyErrors) {
                                    if (sClass !in styleClass) this.styleClass += sClass
                                } else {
                                    if (sClass in styleClass) this.styleClass -= sClass
                                }
                            }
                            "${this.index + 1}. " + (item.id.takeUnless { it.isEmpty() } ?: Localization["editor.missingID"])
                        }
                    }
                }
            }
            items.addListener(ListChangeListener { evt ->
                val list = mutableListOf<CuePointer>()
                while (evt.next()) {
                    list.addAll(evt.list)
                }
                parentPane.struct.cues = list.distinct().toMutableList()
            })
        }
        private val selectLabel = Label().bindLocalized("multipart.selectAPointer").apply {
            id = "pick-cue-pointer-label"
            alignment = Pos.CENTER
            textAlignment = TextAlignment.LEFT
            isWrapText = true
            maxWidth = Double.MAX_VALUE
        }
        private val displayPane: StackPane = StackPane()

        init {
            styleClass += "multipart-objects-pane"

            add(Label().bindLocalized("multipart.cues"), 0, 0)
            add(addButton, 0, 1)
            add(removeButton, 1, 1)
            add(moveUpButton, 2, 1)
            add(moveDownButton, 3, 1)
            add(cuesListView, 0, 2, 5, 1)
            add(displayPane, 6, 2)
            add(selectLabel, 6, 1)

            fun switchToPointerPane(pointer: CuePointer): CuePointerPane<T>? {
                displayPane.children.clear()
                val pane = getPaneForCuePointer(pointer)
                displayPane.children.add(pane)
                selectLabel.isVisible = false
                parentPane.validation.initInitialDecoration()
                return pane
            }

            addButton.setOnAction { _ ->
                val struct = parentPane.struct
                val pointer = CuePointer("*/")
                struct.cues.add(pointer)
                val pane = switchToPointerPane(pointer)
                pane?.idField?.let {
                    it.requestFocus()
                    it.end()
                }
                update()
            }
            val selectionModel = cuesListView.selectionModel
            removeButton.setOnAction {
                val current = selectionModel.selectedItems?.toList()
                if (current != null && current.isNotEmpty()) {
                    val dialog = Alert(Alert.AlertType.CONFIRMATION).apply {
                        val text = UiLocalization[if (current.size == 1) "editor.removeCuePointerConfirm" else "editor.removeCuePointerConfirm.multiple"]
                        this.titleProperty().bind(text)
                        this.contentTextProperty().bind(text)
                        this.addWindowIcons()
                    }
                    val result = dialog.showAndWait()
                    if (result.orElse(null) == ButtonType.OK) {
                        parentPane.struct.cues.removeAll(current)
                        parentPane.editor.editorPane.fireUpdate()
                    }
                }
            }
            moveUpButton.setOnAction { _ ->
                val current = selectionModel.selectedItems?.toList()
                if (current != null && current.isNotEmpty() && selectionModel.isSelectionContiguous()) {
                    val list = parentPane.struct.cues
                    val indices = selectionModel.selectedIndices.toList()
                    val first = indices.min() ?: -1
                    if (indices.isNotEmpty() && first - 1 >= 0) {
                        list.removeAll(current)
                        current.forEachIndexed { i, it ->
                            list.add(first - 1 + i, it)
                        }
                        parentPane.editor.editorPane.fireUpdate()
                        selectionModel.clearSelection()
                        val newIndices = indices.map { it - 1 }
                        selectionModel.selectIndices(newIndices.first(), *newIndices.drop(1).toIntArray())
                    }
                }
            }
            moveDownButton.setOnAction { _ ->
                val current = selectionModel.selectedItems?.toList()
                if (current != null && current.isNotEmpty() && selectionModel.isSelectionContiguous()) {
                    val list = parentPane.struct.cues
                    val indices = selectionModel.selectedIndices.toList()
                    val first = indices.min() ?: -1
                    val last = indices.max() ?: Int.MAX_VALUE
                    if (indices.isNotEmpty() && last + 1 < list.size) {
                        list.removeAll(current)
                        current.forEachIndexed { i, it ->
                            list.add(first + 1 + i, it)
                        }
                        parentPane.editor.editorPane.fireUpdate()
                        selectionModel.clearSelection()
                        val newIndices = indices.map { it + 1 }
                        selectionModel.selectIndices(newIndices.first(), *newIndices.drop(1).toIntArray())
                    }
                }
            }

            selectionModel.selectionMode = SelectionMode.MULTIPLE
            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                removeButton.isDisable = newValue == null
                moveUpButton.isDisable = newValue == null || selectionModel.selectedIndices.min() ?: -1 <= 0 || !selectionModel.isSelectionContiguous()
                moveDownButton.isDisable = newValue == null || selectionModel.selectedIndices.max() ?: Int.MAX_VALUE > cuesListView.items.size - 1 || !selectionModel.isSelectionContiguous()
            }
            cuesListView.setOnMouseClicked { evt ->
                val item = selectionModel.selectedItem
                if (item != null && evt.button == MouseButton.PRIMARY && evt.clickCount >= 1) {
                    switchToPointerPane(item)
                }
            }

            updateObjectsList()
        }

        fun getPaneForCuePointer(cuePointer: CuePointer): CuePointerPane<T>? {
            paneMap as MutableMap
            val fromMap = paneMap[cuePointer]
            if (fromMap == null) {
                val newPane = paneFactory(cuePointer, this)
                if (newPane != null) {
                    paneMap[cuePointer] = newPane
                }
                return newPane
            }
            return fromMap
        }

        fun update() {
            updateObjectsList()
        }

        override fun isInvalid(): Boolean {
            return parentPane.struct.cues.any { cue -> paneMap[cue]?.isInvalid() == true }
        }

        override fun forceUpdate() {
            parentPane.struct.cues.forEach { cue -> paneMap[cue]?.forceUpdate() }
        }

        override fun getValidationResult(): ValidationResult {
            return parentPane.struct.cues.mapNotNull { cue -> paneMap[cue]?.getValidationResult() }.fold(ValidationResult()) { acc, it ->
                acc.combine(it)
            }
        }

        private fun updateObjectsList() {
            val cuePointers = parentPane.struct.cues
            cuesListView.items.apply {
                clear()
                addAll(cuePointers)
            }
            paneMap as MutableMap
            paneMap.keys.toList().forEach {
                if (it !in cuePointers) {
                    val removed = paneMap.remove(it)
                    if (removed in displayPane.children) {
                        displayPane.children.clear()
                        selectLabel.isVisible = true
                    }
                }
            }
        }
    }

    open class CuePointerPane<T : MultipartDatamodel>(val parentPane: CuesPane<T>, val cuePointer: CuePointer) : GridPane(), HasValidator {
        val validation = L10NValidationSupport()

        val idField = TextField(cuePointer.id)
        val beatField: Spinner<Double> = doubleSpinnerFactory(0.0, Double.MAX_VALUE, cuePointer.beat.takeUnless { it == Float.MIN_VALUE }?.toDouble() ?: 0.0, 0.5)
        val durationField: Spinner<Double> = Spinner<Double>().apply {
            valueFactory = SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, Double.MAX_VALUE, cuePointer.duration.coerceAtLeast(0f).toDouble(), 0.5).apply {
                this.converter = object : StringConverter<Double>() {
                    override fun toString(`object`: Double): String {
                        return if (`object` == 0.0) Localization["cuePointer.duration.inherited"] else `object`.toString()
                    }

                    override fun fromString(string: String): Double {
                        return string.toDoubleOrNull() ?: 0.0
                    }
                }
            }
            isEditable = true
        }
        val semitoneField: Spinner<Int> = intSpinnerFactory(Constants.SEMITONE_RANGE.first, Constants.SEMITONE_RANGE.last, cuePointer.semitone, 1)
        val trackField: Spinner<Int> = intSpinnerFactory(-16, 16, cuePointer.track, 1)
        val volumeField: Spinner<Int> = intSpinnerFactory(Constants.VOLUME_RANGE.first, Constants.VOLUME_RANGE.last, cuePointer.volume, 25)

        val allFields: List<Control> by lazy { listOf(idField, beatField, durationField, semitoneField, trackField, volumeField) }

        init {
            styleClass += "grid-pane"
        }

        init {
            // Bind to struct
            idField.textProperty().addListener { _, _, newValue ->
                cuePointer.id = newValue
                parentPane.update()
            }
            beatField.valueProperty().addListener { _, _, newValue ->
                cuePointer.beat = newValue.toFloat()
            }
            durationField.valueProperty().addListener { _, _, newValue ->
                cuePointer.duration = newValue.toFloat()
            }
            semitoneField.valueProperty().addListener { _, _, newValue ->
                cuePointer.semitone = newValue
            }
            trackField.valueProperty().addListener { _, _, newValue ->
                cuePointer.track = newValue
            }
            volumeField.valueProperty().addListener { _, _, newValue ->
                cuePointer.volume = newValue
            }
        }

        init {
            // Validation
            validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.EXTERNAL_CUE_POINTER, Validators.cuePointerPointsNowhere(parentPane.parentPane.gameObject))
        }

        private var gridPaneRowIndex: Int = 0

        protected fun addProperty(label: Node, control: Node): Int {
            add(label, 0, gridPaneRowIndex)
            add(control, 1, gridPaneRowIndex)
            return ++gridPaneRowIndex
        }

        override fun isInvalid(): Boolean {
            return validation.isInvalid
        }

        override fun forceUpdate() {
            validation.initInitialDecoration()
        }

        override fun getValidationResult(): ValidationResult {
            return validation.validationResult
        }
    }
}
