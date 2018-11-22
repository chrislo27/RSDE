package io.github.chrislo27.rhre3.sfxdb.gui.registry

import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.validation.DatamodelObject
import io.github.chrislo27.rhre3.sfxdb.validation.GameObject
import io.github.chrislo27.rhre3.sfxdb.validation.Result
import io.github.chrislo27.rhre3.sfxdb.validation.orNull
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import java.io.File


class GameRegistry(val version: Int) {

    @Volatile var isLoaded: Boolean = false
        private set

    lateinit var gameMap: Map<String, GameObject>
        private set
    lateinit var datamodelMap: Map<String, DatamodelObject>
        private set

    fun loadSFXFolder(progressCallback: (gameObject: kotlin.Result<GameObject>, loaded: Int, total: Int) -> Unit) {
        isLoaded = false
        try {
            val rootFolder = RSDE.rhreSfxRoot.resolve("games/")
//        val customFolder = RSDE.rhreRoot.resolve("customSounds/")
            val predicate = { file: File -> file.isDirectory && file.resolve("data.json").exists() }
            val folders = rootFolder.listFiles(predicate) /*+ customFolder.listFiles(predicate)*/
            val size = folders.size
            val map = mutableMapOf<String, GameObject>()
            folders.forEachIndexed { index, folder ->
                val jsonRoot = JsonHandler.OBJECT_MAPPER.readTree(folder.resolve("data.json"))
                val gameObject: GameObject = Parser.parseGameDefinition(jsonRoot)
                val gameID = (gameObject.id as? Result.Success)?.value
                if (gameID != null) {
                    map[gameID] = gameObject
                }
                progressCallback(kotlin.Result.success(gameObject), index + 1, size)
            }
            gameMap = map
            val allDatamodels = map.values.asSequence().mapNotNull { it.objects.orNull() }.flatMap { it.asSequence() }.mapNotNull { it.orNull() }.filter { it.id is Result.Success }
            val dmMap = mutableMapOf<String, DatamodelObject>()
            allDatamodels.forEach { datamodel ->
                dmMap[(datamodel.id as Result.Success).value] = datamodel
                val deprecatedIDs = datamodel.deprecatedIDs.orNull()
                deprecatedIDs?.forEach { dmMap[it] = datamodel }
            }
            datamodelMap = dmMap

            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
            progressCallback(kotlin.Result.failure(e), -1, -1)
        }
    }

}