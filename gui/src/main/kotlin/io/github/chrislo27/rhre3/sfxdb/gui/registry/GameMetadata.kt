package io.github.chrislo27.rhre3.sfxdb.gui.registry

import io.github.chrislo27.rhre3.sfxdb.adt.Game
import javafx.scene.image.Image
import java.io.File


data class GameMetadata(val game: Game, val icon: Image, val folder: File)