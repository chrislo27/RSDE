package io.github.chrislo27.rhre3.sfxdb.gui.registry

import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.adt.GameObject
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import java.io.File


class GameRegistry(val version: Int) {

    @Volatile var isLoaded: Boolean = false
        private set

    fun loadSFXFolder(rootFolder: File, progressCallback: (gameObject: GameObject, loaded: Int, total: Int) -> Unit) {
        isLoaded = false
        val folders = rootFolder.listFiles { file: File -> file.isDirectory && file.resolve("data.json").exists() }
        val size = folders.size
        folders.forEachIndexed { index, folder ->
            val jsonRoot = JsonHandler.OBJECT_MAPPER.readTree(folder.resolve("data.json"))
            val gameObject: GameObject = Parser.parseGameDefinition(jsonRoot)
            progressCallback(gameObject, index + 1, size)
        }
        isLoaded = true
    }

}