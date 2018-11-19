package io.github.chrislo27.rhre3.sfxdb.gui.registry

import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.adt.GameObject
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import java.io.File


class GameRegistry(val version: Int) {

    @Volatile var isLoaded: Boolean = false
        private set

    fun loadSFXFolder(progressCallback: (gameObject: GameObject?, loaded: Int, total: Int) -> Unit) {
        isLoaded = false
        try {
            val rootFolder = RSDE.rhreSfxRoot.resolve("games/")
//        val customFolder = RSDE.rhreRoot.resolve("customSounds/")
            val predicate = { file: File -> file.isDirectory && file.resolve("data.json").exists() }
            val folders = rootFolder.listFiles(predicate) /*+ customFolder.listFiles(predicate)*/
            val size = folders.size
            folders.forEachIndexed { index, folder ->
                val jsonRoot = JsonHandler.OBJECT_MAPPER.readTree(folder.resolve("data.json"))
                val gameObject: GameObject = Parser.parseGameDefinition(jsonRoot)
                progressCallback(gameObject, index + 1, size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            progressCallback(null, -1, -1)
        }
        isLoaded = true
    }

}