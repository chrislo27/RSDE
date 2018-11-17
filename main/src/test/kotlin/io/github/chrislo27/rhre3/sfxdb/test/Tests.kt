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
import io.github.chrislo27.rhre3.sfxdb.adt.GameObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
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
    val sfxFolder = File(System.getProperty("user.home")).resolve(".rhre3/sfx/master/games/")

    @BeforeAll
    @JvmStatic
    fun checkExists() {
        assertEquals(true, sfxFolder.exists())
    }

    @Test
    fun parseOne() {
        val animalAcrobatFolder = sfxFolder.resolve("animalAcrobat/")
        assertEquals(true, animalAcrobatFolder.exists())
        val dataFile = animalAcrobatFolder.resolve("data.json")
        assertEquals(true, dataFile.exists())

        val rootNode = objectMapper.readTree(dataFile)
        val gameObject: GameObject = Parser.parseGameDefinition(rootNode)
//        assertEquals(true, gameObject.)
    }

}