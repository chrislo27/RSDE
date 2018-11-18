package io.github.chrislo27.rhre3.sfxdb.adt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import kotlin.reflect.KProperty


typealias JsonTransformer<T> = (node: JsonNode) -> Result<T>

open class Property<T>(val transformer: JsonTransformer<T>, initialValue: Result<T> = Result.Unset()) {

    private var storedValue: Result<T> = initialValue

    constructor(transformer: JsonTransformer<T>, initialValue: T) : this(transformer, Result.Success(initialValue))

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Result<T> {
        return storedValue
    }

    /**
     * NOTE: used only for [CuePointerObject] when setting fields to be optional or not
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Result<T>) {
        storedValue = value
    }

    fun setJson(value: JsonNode) {
        storedValue = try {
            transformer(value)
        } catch (jnte: JsonNodeTypeException) {
            Result.Failure(jnte.node, jnte.node.toString(), "Expected ${if (jnte.expectedTypes.size == 1) jnte.expectedTypes.first().toString() else jnte.expectedTypes.toString()}, got ${jnte.gotType} instead")
        } catch (e: Exception) {
            throw e
        }
    }

}

sealed class Result<T> {
    class Unset<T> : Result<T>() {
        override fun toString(): String = "Unset"
    }
    class Success<T>(val value: T) : Result<T>() {
        override fun toString(): String = "Success($value)"
    }
    class Failure<T>(val node: JsonNode, val passedIn: Any?, val errorMessage: String) : Result<T>() {
        override fun toString(): String = "Failure($node, $passedIn, \"$errorMessage\")"
    }
}

class JsonNodeTypeException(val node: JsonNode, val expectedTypes: List<JsonNodeType>, val gotType: JsonNodeType) : RuntimeException()
