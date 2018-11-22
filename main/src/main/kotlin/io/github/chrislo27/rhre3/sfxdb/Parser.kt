package io.github.chrislo27.rhre3.sfxdb

import com.fasterxml.jackson.databind.JsonNode
import io.github.chrislo27.rhre3.sfxdb.validation.GameObject
import io.github.chrislo27.rhre3.sfxdb.validation.Property
import io.github.chrislo27.rhre3.sfxdb.validation.Struct
import io.github.chrislo27.rhre3.sfxdb.util.findDelegatingPropertyInstances


object Parser {

    fun parseGameDefinition(root: JsonNode, printProperties: Boolean = false): GameObject {
        val gameObj = GameObject()
        buildStruct(gameObj, root, printProperties)
        return gameObj
    }

    inline fun <reified T : Struct> buildStruct(struct: T, baseNode: JsonNode, printProperties: Boolean = false) {
        val propertyFields = findDelegatingPropertyInstances(struct, Property::class)

        propertyFields.forEach { property ->
            val name = property.property.name
            val node: JsonNode? = baseNode[name]
            node?.let {
                property.delegatingToInstance.setJson(node)
            }
        }

        if (printProperties) propertyFields.forEach { property ->
            println("Property ${property.property.name} = ${property.property.get(struct)}")
        }
    }

}