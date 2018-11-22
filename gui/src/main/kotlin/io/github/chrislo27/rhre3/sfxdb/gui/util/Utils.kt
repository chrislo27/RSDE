package io.github.chrislo27.rhre3.sfxdb.gui.util

import javafx.scene.Scene
import javafx.scene.input.KeyCombination
import javafx.scene.text.Font
import javafx.stage.Stage


fun Scene.addDebugAccelerators() {
    this.accelerators[KeyCombination.keyCombination("Shortcut+Alt+'i'")] = Runnable(Localization::refreshLangs)
}

fun Stage.setMinimumBoundsToSized() {
    this.sizeToScene()
    this.minWidth = this.width
    this.minHeight = this.height
}

val Double.em: Double get() = Font.getDefault().size * this
