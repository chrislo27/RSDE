package io.github.chrislo27.rhre3.sfxdb.adt

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.chrislo27.rhre3.sfxdb.Series


class Game(
    val id: String, val name: String, val series: Series,
    val objects: List<Datamodel>,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val group: String? = null,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val groupDefault: Boolean = false,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val priority: Int = 0,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val searchHints: List<String>? = null,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val noDisplay: Boolean = false
)

class CuePointer(
    val id: String,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val beat: Float = Float.MIN_VALUE,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val duration: Float = 0f,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val semitone: Int = 0,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val volume: Int = 100,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val track: Int = 0,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val metadata: Map<String, Any?>? = null
)

abstract class Datamodel(val type: String, val id: String, val name: String, val deprecatedIDs: List<String>)

class Cue(
    id: String, name: String, deprecatedIDs: List<String>,
    val duration: Float,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val stretchable: Boolean = false,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val repitchable: Boolean = false,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val fileExtension: String = "ogg",
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val introSound: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val endingSound: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val responseIDs: List<String>? = null,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val baseBpm: Float = 0f,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val loops: Boolean = false
) : Datamodel("cue", id, name, deprecatedIDs)

class Pattern(
    id: String, name: String, deprecatedIDs: List<String>,
    val cues: List<CuePointer>,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) val stretchable: Boolean = false
) : Datamodel("pattern", id, name, deprecatedIDs)

class Equidistant(
    id: String, name: String, deprecatedIDs: List<String>,
    val cues: List<CuePointer>,
    val distance: Float = 0f,
    val stretchable: Boolean = false
) : Datamodel("equidistant", id, name, deprecatedIDs)

class KeepTheBeat(
    id: String, name: String, deprecatedIDs: List<String>,
    val cues: List<CuePointer>,
    val defaultDuration: Float = 0f
) : Datamodel("keepTheBeat", id, name, deprecatedIDs)

class RandomCue(
    id: String, name: String, deprecatedIDs: List<String>,
    val cues: List<CuePointer>,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val responseIDs: List<String>? = null
) : Datamodel("randomCue", id, name, deprecatedIDs)

class SubtitleEntity(
    id: String, name: String, deprecatedIDs: List<String>,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val subtitleType: String? = null
) : Datamodel("subtitle", id, name, deprecatedIDs)

class EndRemixEntity(id: String, name: String, deprecatedIDs: List<String>) : Datamodel("endEntity", id, name, deprecatedIDs)

class ShakeEntity(id: String, name: String, deprecatedIDs: List<String>) : Datamodel("shakeEntity", id, name, deprecatedIDs)

class TextureEntity(id: String, name: String, deprecatedIDs: List<String>) : Datamodel("textureEntity", id, name, deprecatedIDs)
