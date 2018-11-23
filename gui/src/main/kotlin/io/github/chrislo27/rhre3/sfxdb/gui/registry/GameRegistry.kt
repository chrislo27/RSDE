package io.github.chrislo27.rhre3.sfxdb.gui.registry

import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.adt.Datamodel
import io.github.chrislo27.rhre3.sfxdb.adt.Game
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import java.io.File


class GameRegistry(val version: Int) {

    @Volatile var isLoaded: Boolean = false
        private set

    lateinit var gameMap: Map<String, Game>
        private set
    lateinit var datamodelMap: Map<String, Datamodel>
        private set
    lateinit var depsDatamodelMap: Map<String, Datamodel>
        private set

    fun loadSFXFolder(progressCallback: (gameObject: kotlin.Result<Game>, loaded: Int, total: Int) -> Unit) {
        isLoaded = false
        try {
            val rootFolder = RSDE.rhreSfxRoot.resolve("games/")
//        val customFolder = RSDE.rhreRoot.resolve("customSounds/")
            val predicate = { file: File -> file.isDirectory && file.resolve("data.json").exists() }
            val folders = rootFolder.listFiles(predicate) /*+ customFolder.listFiles(predicate)*/
            val size = folders.size
            val map = mutableMapOf<String, Game>()
            folders.forEachIndexed { index, folder ->
                val jsonRoot = JsonHandler.OBJECT_MAPPER.readTree(folder.resolve("data.json"))
                RSDE.LOGGER.info("Loading game ${folder.name}")
                val game: Game = Parser.parseGameDefinition(jsonRoot).produceImmutableADT()
                val gameID = game.id
                map[gameID] = game
                progressCallback(kotlin.Result.success(game), index + 1, size)
            }
            gameMap = map
            val allDatamodels = map.values.flatMap { it.objects }
            val dmMap = mutableMapOf<String, Datamodel>()
            val depsDmMap = mutableMapOf<String, Datamodel>()
            allDatamodels.forEach { datamodel ->
                dmMap[datamodel.id] = datamodel
                depsDmMap[datamodel.id] = datamodel
                val deprecatedIDs = datamodel.deprecatedIDs
                deprecatedIDs.forEach { depsDmMap[it] = datamodel }
            }
            datamodelMap = dmMap
            depsDatamodelMap = depsDmMap

            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
            progressCallback(kotlin.Result.failure(e), -1, -1)
        }
    }

}