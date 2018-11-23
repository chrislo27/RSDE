package io.github.chrislo27.rhre3.sfxdb.test

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.validation.GameObject
import io.github.chrislo27.rhre3.sfxdb.validation.Transformers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.io.File


object Tests {

    val objectMapper: ObjectMapper = ObjectMapper()
        .enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(MapperFeature.USE_ANNOTATIONS)
        .enable(JsonParser.Feature.ALLOW_COMMENTS)
        .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .registerModule(AfterburnerModule())
        .registerModule(KotlinModule())
    val sfxFolder1 = File(System.getProperty("user.home")).resolve("Desktop/libGDX-projects/RHRE-database/games/")
    val sfxFolder2 = File(System.getProperty("user.home")).resolve("Desktop/RHRE/RHRE-database/games/")
    lateinit var sfxFolder: File
        private set
    val printProperties = false

    @BeforeAll
    @JvmStatic
    fun checkExists() {
        sfxFolder = when {
            sfxFolder1.exists() -> sfxFolder1
            sfxFolder2.exists() -> sfxFolder2
            else -> File(System.getProperty("user.home")).resolve(".rhre3/sfx/master/games/")
        }
        assertEquals(true, sfxFolder.exists())
    }

    @Test
    fun parseOne() {
        val animalAcrobatFolder = sfxFolder.resolve("microRowMegamix/")
        assertEquals(true, animalAcrobatFolder.exists())
        val dataFile = animalAcrobatFolder.resolve("data.json")
        assertEquals(true, dataFile.exists())

        val rootNode = objectMapper.readTree(dataFile)
        val gameObject: GameObject = Parser.parseGameDefinition(rootNode, printProperties)
        assertEquals(false, Transformers.anyNonSuccess(gameObject))
        gameObject.produceImmutableADT()
    }

    @RepeatedTest(10)
    fun parseAll() {
        sfxFolder.listFiles().filter { it.isDirectory && it.resolve("data.json").exists() }.forEach { folder ->
            val dataFile = folder.resolve("data.json")
            val rootNode = objectMapper.readTree(dataFile)
            val gameObject: GameObject = Parser.parseGameDefinition(rootNode, printProperties)
            assertEquals(false, Transformers.anyNonSuccess(gameObject))
            gameObject.produceImmutableADT()
        }
    }

}