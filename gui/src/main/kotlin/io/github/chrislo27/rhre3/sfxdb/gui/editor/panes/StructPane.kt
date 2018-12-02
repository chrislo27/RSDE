package io.github.chrislo27.rhre3.sfxdb.gui.editor.panes

import io.github.chrislo27.rhre3.sfxdb.Constants
import io.github.chrislo27.rhre3.sfxdb.adt.*
import io.github.chrislo27.rhre3.sfxdb.gui.control.Chip
import io.github.chrislo27.rhre3.sfxdb.gui.control.ChipPane
import io.github.chrislo27.rhre3.sfxdb.gui.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.gui.util.*
import io.github.chrislo27.rhre3.sfxdb.gui.validation.L10NValidationSupport
import io.github.chrislo27.rhre3.sfxdb.gui.validation.Validators
import javafx.application.Platform
import javafx.collections.FXCollections
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
import java.util.*


abstract class StructPane<T : JsonStruct>(val editor: Editor, val struct: T) : BorderPane() {

    val gameObject: Game get() = editor.gameObject

    val titleLabel: Label = Label().apply {
        id = "title"
    }
    val centreVbox: VBox = VBox()
    val gridPane: GridPane = GridPane().apply {
        styleClass += "grid-pane"
    }

    private var gridPaneRowIndex: Int = 0
    protected val validation = L10NValidationSupport()

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

}

abstract class DatamodelPane<T : Datamodel>(editor: Editor, struct: T) : StructPane<T>(editor, struct) {
    open val idField: TextField = TextField(struct.id)
    open val nameField: TextField = TextField(struct.name)
    open val deprecatedIDsField: ChipPane = ChipPane(FXCollections.observableArrayList(struct.deprecatedIDs.map { Chip(it) }))
}

abstract class MultipartStructPane<T : MultipartDatamodel>(editor: Editor, struct: T) : DatamodelPane<T>(editor, struct) {

    abstract val cuesPane: CuesPane<T>

    override fun update() {
        super.update()
        cuesPane.update()
    }

    @Suppress("LeakingThis")
    open class CuesPane<T : MultipartDatamodel>(val parentPane: MultipartStructPane<T>,
                                                val paneFactory: (CuePointer, CuesPane<T>) -> CuePointerPane<T>?)
        : GridPane() {
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
                            "${this.index + 1}. " + (item.id.takeUnless { it.isEmpty() } ?: Localization["editor.missingID"])
                        }
                    }
                }
            }
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

            addButton.setOnAction {

            }
            removeButton.setOnAction {
                val current = cuesListView.selectionModel.selectedItem
                if (current != null) {
                    val dialog = Alert(Alert.AlertType.CONFIRMATION).apply {
                        this.titleProperty().bind(UiLocalization["editor.removeObjectConfirm"])
                        this.contentTextProperty().bind(UiLocalization["editor.removeObjectConfirm"])
                        this.addWindowIcons()
                    }
                    val result = dialog.showAndWait()
                    if (result.orElse(null) == ButtonType.OK) {
                        parentPane.struct.cues.remove(current)
                        parentPane.editor.editorPane.fireUpdate()
                    }
                }
            }
            moveUpButton.setOnAction { _ ->
                val current = cuesListView.selectionModel.selectedItem
                if (current != null) {
                    val list = parentPane.struct.cues
                    val index = list.indexOf(current)
                    if (index != -1 && index - 1 >= 0) {
                        val removed = list.removeAt(index)
                        list.add(index - 1, removed)
                        parentPane.editor.editorPane.fireUpdate()
                        cuesListView.selectionModel.select(index - 1)
                    }
                }
            }
            moveDownButton.setOnAction { _ ->
                val current = cuesListView.selectionModel.selectedItem
                if (current != null) {
                    val list = parentPane.struct.cues
                    val index = list.indexOf(current)
                    if (index != -1 && index + 1 < list.size - 1) {
                        val removed = list.removeAt(index)
                        list.add(index + 1, removed)
                        parentPane.editor.editorPane.fireUpdate()
                        cuesListView.selectionModel.select(index + 1)
                    }
                }
            }

            cuesListView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                removeButton.isDisable = newValue == null
                moveUpButton.isDisable = newValue == null && cuesListView.selectionModel.selectedIndex > 0
                moveDownButton.isDisable = newValue == null && cuesListView.selectionModel.selectedIndex < cuesListView.items.size - 1
            }
            cuesListView.setOnMouseClicked { evt ->
                val item = cuesListView.selectionModel.selectedItem
                if (item != null && evt.button == MouseButton.PRIMARY && evt.clickCount >= 1) {
                    displayPane.children.clear()
                    displayPane.children.add(getPaneForCuePointer(item))
                    selectLabel.isVisible = false
                }
            }

            updateObjectsList()
        }

        fun getPaneForCuePointer(cuePointer: CuePointer): Pane? {
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

    open class CuePointerPane<T : MultipartDatamodel>(val parentPane: CuesPane<T>, val cuePointer: CuePointer) : GridPane() {
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

        init {
            styleClass += "grid-pane"
        }

        init {
            // Validation
            val validation = parentPane.parentPane.validation
            validation.registerValidators(idField, Validators.OBJ_ID_BLANK, Validators.EXTERNAL_CUE_POINTER, Validators.cuePointerPointsNowhere(parentPane.parentPane.gameObject))
        }

        private var gridPaneRowIndex: Int = 0

        protected fun addProperty(label: Node, control: Node): Int {
            add(label, 0, gridPaneRowIndex)
            add(control, 1, gridPaneRowIndex)
            return ++gridPaneRowIndex
        }
    }
}
