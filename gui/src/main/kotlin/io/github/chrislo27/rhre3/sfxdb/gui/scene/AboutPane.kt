package io.github.chrislo27.rhre3.sfxdb.gui.scene

import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.LibrariesUsed
import io.github.chrislo27.rhre3.sfxdb.gui.util.bindLocalized
import io.github.chrislo27.rhre3.sfxdb.gui.util.em
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.text.TextFlow


@Suppress("UNUSED_CHANGED_VALUE")
class AboutPane(val app: RSDE) : BorderPane() {

    class AboutTab(pane: AboutPane) : Tab() {
        constructor(app: RSDE) : this(AboutPane(app))

        init {
            content = pane
            graphic = ImageView(Image("icon/32.png", true)).apply {
                this.isPreserveRatio = true
                this.fitWidth = 1.5.em
                this.fitHeight = 1.5.em
            }
        }
    }

    init {
        stylesheets += "style/about.css"

        val gridPane = GridPane()
        gridPane.alignment = Pos.CENTER
        gridPane.hgap = 1.0.em
        gridPane.vgap = 0.2.em

        gridPane.add(ImageView(Image("icon/128.png", 128.0, 128.0, true, true, true)), 0, 0)
        gridPane.add(Label("RHRE SFX\nDatabase Editor").apply {
            styleClass += "title"
        }, 1, 0)
        var row = 1
        gridPane.add(Label(RSDE.VERSION.toString()).apply { styleClass += "version" }, 1, row++)
        gridPane.add(Hyperlink(RSDE.GITHUB).apply {
            setOnAction {
                app.hostServices.showDocument(RSDE.GITHUB)
            }
        }, 1, row++)
        gridPane.add(TextFlow().apply {
            children += Label().bindLocalized("about.license")
            children += Hyperlink(RSDE.LICENSE_NAME).apply {
                padding = Insets(0.0)
                setOnAction { _ ->
                    app.hostServices.showDocument("https://github.com/chrislo27/RSDE/blob/master/LICENSE.txt")
                }
            }
        }, 1 ,row++)
        row++
        gridPane.add(Label().bindLocalized("about.oss").apply { styleClass += "oss" }, 1, row++)
        val librariesGridPane = GridPane().apply {
            id = "libraries-gp"
        }
        LibrariesUsed.libraries.forEachIndexed { i, lib ->
            librariesGridPane.add(Hyperlink(lib.name).apply {
                this.setOnAction {
                    app.hostServices.showDocument(lib.website)
                }
            }, i % 2, i / 2)
        }
        gridPane.add(librariesGridPane, 1, row++, 2, 1)

        center = gridPane
    }

}