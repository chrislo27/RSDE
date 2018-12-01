package io.github.chrislo27.rhre3.sfxdb.adt

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.github.chrislo27.rhre3.sfxdb.Series


interface JsonStruct

class Game(
        var id: String,
        var name: String,
        var series: Series,

        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var group: String? = null,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var groupDefault: Boolean = false,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var priority: Int = 0,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var searchHints: MutableList<String>? = null,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var noDisplay: Boolean = false,

        var objects: MutableList<Datamodel>
) : JsonStruct

class CuePointer(
        var id: String,
        @JsonInclude(JsonInclude.Include.CUSTOM, valueFilter = BeatFilter::class) var beat: Float = Float.MIN_VALUE,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var duration: Float = 0f,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var semitone: Int = 0,
        @JsonInclude(JsonInclude.Include.CUSTOM, valueFilter = VolumeFilter::class) var volume: Int = 100,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var track: Int = 0,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) var metadata: MutableMap<String, Any?>? = null
) : JsonStruct

@JsonPropertyOrder("type", "id", "name", "deprecatedIDs")
abstract class Datamodel(@JsonInclude(JsonInclude.Include.ALWAYS) var type: String,
                         @JsonInclude(JsonInclude.Include.ALWAYS) var id: String,
                         @JsonInclude(JsonInclude.Include.ALWAYS) var name: String,
                         @JsonInclude(JsonInclude.Include.ALWAYS) var deprecatedIDs: MutableList<String>) : JsonStruct

class Cue(
    id: String, name: String, deprecatedIDs: MutableList<String>,
    var duration: Float,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var stretchable: Boolean = false,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var repitchable: Boolean = false,
    @JsonInclude(JsonInclude.Include.CUSTOM, valueFilter = CueFileExtFilter::class) var fileExtension: String = "ogg",
    @JsonInclude(JsonInclude.Include.NON_EMPTY) var introSound: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) var endingSound: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) var responseIDs: MutableList<String>? = null,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var baseBpm: Float = 0f,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var loops: Boolean = false
) : Datamodel("cue", id, name, deprecatedIDs)

class Pattern(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        var cues: MutableList<CuePointer>,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var stretchable: Boolean = false
) : Datamodel("pattern", id, name, deprecatedIDs)

class Equidistant(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        var cues: MutableList<CuePointer>,
        var distance: Float = 0f,
        var stretchable: Boolean = false
) : Datamodel("equidistant", id, name, deprecatedIDs)

class KeepTheBeat(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        var cues: MutableList<CuePointer>,
        var defaultDuration: Float = 0f
) : Datamodel("keepTheBeat", id, name, deprecatedIDs)

class RandomCue(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        var cues: MutableList<CuePointer>,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) var responseIDs: List<String>? = null
) : Datamodel("randomCue", id, name, deprecatedIDs)

class SubtitleEntity(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) var subtitleType: String? = null
) : Datamodel("subtitle", id, name, deprecatedIDs)

class EndRemixEntity(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("endEntity", id, name, deprecatedIDs)

class ShakeEntity(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("shakeEntity", id, name, deprecatedIDs)

class TextureEntity(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("textureEntity", id, name, deprecatedIDs)
