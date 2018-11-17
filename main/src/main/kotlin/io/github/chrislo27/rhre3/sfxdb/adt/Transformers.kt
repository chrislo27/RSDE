package io.github.chrislo27.rhre3.sfxdb.adt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import io.github.chrislo27.rhre3.sfxdb.Constants
import io.github.chrislo27.rhre3.sfxdb.Parser
import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.SoundFileExtensions
import io.github.chrislo27.rhre3.sfxdb.util.findDelegatingPropertyInstances


object Transformers {

    private fun JsonNode.checkNodeType(type: JsonNodeType) {
        if (this.nodeType != type)
            throw JsonNodeTypeException(this, listOf(type), this.nodeType)
    }

    private fun JsonNode.checkNodeTypes(vararg types: JsonNodeType) {
        if (this.nodeType !in types)
            throw JsonNodeTypeException(this, types.toList(), this.nodeType)
    }

    val GAME_ID_REGEX: Regex = "(?:[A-Za-z0-9_/\\-])+".toRegex()
    val ID_REGEX: Regex = "(?:[A-Za-z0-9_/\\-*])+".toRegex()

    val stringTransformer: JsonTransformer<String> = {
        it.checkNodeType(JsonNodeType.STRING)
        Result.Success(it.textValue())
    }
    val nonEmptyStringTransformer: JsonTransformer<String> = { node ->
        node.checkNodeType(JsonNodeType.STRING)
        val text = node.textValue()
        if (text.isEmpty())
            Result.Failure(node, text, "String cannot be empty")
        else
            Result.Success(node.textValue())
    }
    val floatTransformer: JsonTransformer<Float> = {
        it.checkNodeType(JsonNodeType.NUMBER)
        if (!it.isFloatingPointNumber)
            Result.Failure(it, it.toString(), "Expected float")
        else
            Result.Success(it.floatValue())
    }
    val booleanTransformer: JsonTransformer<Boolean> = {
        it.checkNodeType(JsonNodeType.BOOLEAN)
        Result.Success(it.booleanValue())
    }
    val intTransformer: JsonTransformer<Int> = {
        it.checkNodeType(JsonNodeType.NUMBER)
        if (!it.isInt)
            Result.Failure(it, it.toString(), "Expected integer")
        else
            Result.Success(it.intValue())
    }
    val stringArrayTransformer: JsonTransformer<List<String>> = transformer@{ node ->
        node.checkNodeType(JsonNodeType.ARRAY)
        val list = node.map {
            if (!it.isTextual)
                return@transformer Result.Failure(it, it.toString(), "Expected array of only strings")
            it.asText()
        }
        Result.Success(list)
    }

    val gameIdTransformer: JsonTransformer<String> = { node ->
        node.checkNodeType(JsonNodeType.STRING)
        val st = node.textValue() ?: error("Escaped node type check!")
        if (!GAME_ID_REGEX.matches(st))
            Result.Failure(node, st, "String does not confirm to game ID rules")
        else
            Result.Success(st)
    }
    val idTransformer: JsonTransformer<String> = { node ->
        node.checkNodeType(JsonNodeType.STRING)
        val st = node.textValue() ?: error("Escaped node type check!")
        if (!ID_REGEX.matches(st))
            Result.Failure(node, st, "String does not confirm to general ID rules")
        else
            Result.Success(st)
    }
    val soundFileExtensionTransformer: JsonTransformer<String> = { node ->
        node.checkNodeType(JsonNodeType.STRING)
        val text = node.textValue()
        if (SoundFileExtensions.VALUES.any { text.equals(it.fileExt, ignoreCase = true) })
            Result.Success(node.textValue())
        else
            Result.Failure(node, text, "String must be a supported file extension. Supported: ${SoundFileExtensions.VALUES.map(SoundFileExtensions::fileExt)}")
    }
    val seriesTransformer: JsonTransformer<Series> = { node ->
        node.checkNodeType(JsonNodeType.STRING)
        val st = node.textValue() ?: error("Escaped node type check!")
        val series: Series? = Series.VALUES.find { it.jsonName == st }
        if (series == null)
            Result.Failure(node, st, "No series found with that name. Supported: ${Series.VALUES.map(Series::jsonName)}")
        else
            Result.Success(series)
    }
    val responseIDsTransformer: JsonTransformer<List<String>> = transformer@{ node ->
        val initial = stringArrayTransformer(node)
        if (initial is Result.Success) {
            val value = initial.value
            value.forEach {
                if (!ID_REGEX.matches(it)) {
                    return@transformer Result.Failure(node, it, "Response ID does not match ID rules")
                }
            }
            initial
        } else initial
    }
    val volumeTransformer: JsonTransformer<Int> = transformer@{ node ->
        val initial = intTransformer(node)
        if (initial is Result.Success) {
            val value = initial.value
            if (value in Constants.VOLUME_RANGE)
                initial
            else
                Result.Failure(node, value, "Volume is not in range: ${Constants.VOLUME_RANGE}")
        } else initial
    }

    val datamodelTransformer: JsonTransformer<DatamodelObject> = transformer@{ node ->
        val typeNode: JsonNode = node["type"] ?: return@transformer Result.Failure(node, null, "Missing type string field")
        typeNode.checkNodeType(JsonNodeType.STRING)
        val type = typeNode.textValue() ?: error("Escaped node type check!")

        // The repetition of the run statements is required for reified types
        val datamodel: Pair<DatamodelObject, Boolean> = when(type) {
            "cue" -> CueObject().run {
                Parser.buildStruct(this, node, true)
                this to anyNonSuccess(this)
            }
            "pattern" -> PatternObject().run {
                Parser.buildStruct(this, node, true)
                this to anyNonSuccess(this)
            }
            "equidistant" -> EquidistantObject().run {
                Parser.buildStruct(this, node, true)
                this to anyNonSuccess(this)
            }
            "keepTheBeat" -> KeepTheBeatObject().run {
                Parser.buildStruct(this, node, true)
                this to anyNonSuccess(this)
            }
            "randomCue" -> RandomCueObject().run {
                Parser.buildStruct(this, node, true)
                this to anyNonSuccess(this)
            }
            else -> return@transformer Result.Failure(node, type, "Type of datamodel is not valid or not implemented")
        }
        if (datamodel.second)
            Result.Failure(node, datamodel.first, "Error in datamodel")
        else Result.Success(datamodel.first)
    }

    fun positiveFloatTransformer(errorMessage: String, inclusive: Boolean = true): JsonTransformer<Float> = { node ->
        val initial = floatTransformer(node)
        if (initial is Result.Success) {
            val value = initial.value
            if ((inclusive && value >= 0f) || (!inclusive && value > 0f))
                initial
            else
                Result.Failure(node, value, errorMessage)
        } else initial
    }

    fun cuePointerTransformer(beatNotUsed: Boolean = false, durationNotUsed: Boolean = false): JsonTransformer<CuePointerObject> = { node ->
        Result.NotImplemented() // TODO
    }

    fun <T> transformerToList(pointerTransformer: JsonTransformer<T>): JsonTransformer<List<Result<T>>> = { node ->
        node.checkNodeType(JsonNodeType.ARRAY)
        val list = node.map(pointerTransformer)
        if (list.any { it !is Result.Success })
            Result.Failure(node, list, "Non-Successes in list")
        else
            Result.Success(list)
    }

    inline fun <reified T : Any> anyNonSuccess(obj: T): Boolean {
        return findDelegatingPropertyInstances(obj, Result::class).any { it.delegatingToInstance !is Result.Success }
    }

}
