package io.github.chrislo27.rhre3.sfxdb

import com.fasterxml.jackson.databind.JsonNode
import io.github.chrislo27.rhre3.sfxdb.adt.GameObject
import io.github.chrislo27.rhre3.sfxdb.adt.Property
import io.github.chrislo27.rhre3.sfxdb.adt.Struct
import io.github.chrislo27.rhre3.sfxdb.util.findDelegatingPropertyInstances


object Parser {

    fun parseGameDefinition(root: JsonNode): GameObject {
        val gameObj = GameObject()
        buildStruct(gameObj, root, true)
        return gameObj
    }

    inline fun <reified T : Struct> buildStruct(struct: T, baseNode: JsonNode, printProperties: Boolean = false) {
        val propertyFields = findDelegatingPropertyInstances(struct, Property::class)

        propertyFields.forEach { property ->
            val name = property.property.name
            val node: JsonNode? = baseNode[name]
            node?.let { n ->
                property.delegatingToInstance.setJson(node)
            }
        }

        if (printProperties) propertyFields.forEach { property ->
            println("Property ${property.property.name} = ${property.property.get(struct)}")
        }
    }

}