package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tab
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import org.controlsfx.control.ToggleSwitch


class SettingsPane(val app: RSDE) : BorderPane() {
    class SettingsTab(pane: SettingsPane) : Tab() {
        constructor(app: RSDE) : this(SettingsPane(app))

        init {
            content = pane
            graphic = ImageView(Image("/image/ui/settings.png", true)).apply {
                this.isPreserveRatio = true
                this.fitWidth = 1.5.em
                this.fitHeight = 1.5.em
            }
        }
    }

    init {
        stylesheets += "style/settings.css"

        top = Label().bindLocalized("settings.title").apply {
            id = "title"
        }

        val gridPane = GridPane().apply {
            styleClass += "grid-pane"
        }
        center = ScrollPane(gridPane).apply {
            hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }

        gridPane.alignment = Pos.CENTER
        gridPane.hgap = 1.0.em
        gridPane.vgap = 0.2.em

        var gridPaneRowIndex = 0
        fun addProperty(label: Node, control: Node): Int {
            gridPane.add(label, 0, gridPaneRowIndex)
            gridPane.add(control, 1, gridPaneRowIndex)
            return ++gridPaneRowIndex
        }

        val settings = app.settings
        addProperty(Label().bindLocalized("settings.nightMode"), ToggleSwitch().apply {
            selectedProperty().bindBidirectional(settings.nightModeProperty)
            selectedProperty().addListener { _, _, _ ->
                settings.persistToStorage()
            }
        })
        gridPane.add(Button().bindLocalized("settings.resetDivider").apply {
            setOnAction { _ ->
                settings.dividerPosition = 0.3
                settings.persistToStorage()
            }
        }, 2, 0)

    }
}