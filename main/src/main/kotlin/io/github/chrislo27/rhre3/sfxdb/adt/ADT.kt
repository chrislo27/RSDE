package io.github.chrislo27.rhre3.sfxdb.adt

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.github.chrislo27.rhre3.sfxdb.BaseBpmRules
import io.github.chrislo27.rhre3.sfxdb.Language
import io.github.chrislo27.rhre3.sfxdb.Series


interface JsonStruct

class Game(
    var id: String,
    var name: String,
    var series: Series,

    @JsonInclude(JsonInclude.Include.CUSTOM, valueFilter = LanguageFilter::class) var language: Language = Language.NONE,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var group: String? = null,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var groupDefault: Boolean = false,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var priority: Int = 0,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var searchHints: MutableList<String>? = null,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var noDisplay: Boolean = false,

    var objects: MutableList<Datamodel>
) : JsonStruct

class CuePointer(
        var id: String,
        @JsonInclude(JsonInclude.Include.ALWAYS) var beat: Float = Float.MIN_VALUE,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var duration: Float = 0f,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var semitone: Int = 0,
        @JsonInclude(JsonInclude.Include.CUSTOM, valueFilter = VolumeFilter::class) var volume: Int = 100,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var track: Int = 0,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) var metadata: MutableMap<String, Any?>? = null
) : JsonStruct {
    fun copy(id: String = this.id, beat: Float = this.beat, duration: Float = this.duration, semitone: Int = this.semitone, volume: Int = this.volume, track: Int = this.track, metadata: MutableMap<String, Any?>? = this.metadata): CuePointer {
        return CuePointer(id, beat, duration, semitone, volume, track, metadata?.toMutableMap())
    }
}

@JsonPropertyOrder("type", "id", "name", "deprecatedIDs", "subtext")
abstract class Datamodel(@JsonInclude(JsonInclude.Include.ALWAYS) var type: String,
                         @JsonInclude(JsonInclude.Include.ALWAYS) var id: String,
                         @JsonInclude(JsonInclude.Include.ALWAYS) var name: String,
                         @JsonInclude(JsonInclude.Include.ALWAYS) var deprecatedIDs: MutableList<String>,
@JsonInclude(JsonInclude.Include.NON_EMPTY) var subtext: String = "") : JsonStruct {
    abstract fun copy(): Datamodel
}

abstract class MultipartDatamodel(type: String, id: String, name: String, deprecatedIDs: MutableList<String>,
                                  var cues: MutableList<CuePointer>) : Datamodel(type, id, name, deprecatedIDs) {
    protected fun getCopyOfCues(): MutableList<CuePointer> = cues.map { it.copy() }.toMutableList()
}

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
    @JsonInclude(JsonInclude.Include.CUSTOM, valueFilter = UseTimeStretchingFilter::class) var useTimeStretching: Boolean = true,
    @JsonInclude(JsonInclude.Include.CUSTOM, valueFilter = BaseBpmRulesFilter::class) var baseBpmRules: BaseBpmRules = BaseBpmRules.ALWAYS,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var loops: Boolean = false,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var earliness: Float = 0f,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var loopStart: Float = 0f,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var loopEnd: Float = 0f,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var pitchBending: Boolean = false,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var writtenPitch: Int = 0
) : Datamodel("cue", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return Cue(id, name, deprecatedIDs, duration, stretchable, repitchable, fileExtension, introSound, endingSound, responseIDs?.toMutableList(), baseBpm, useTimeStretching, baseBpmRules, loops, earliness, loopStart, loopEnd, pitchBending, writtenPitch)
    }
}

@JsonPropertyOrder("type", "id", "name", "deprecatedIDs", "stretchable", "cues")
class Pattern(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        cues: MutableList<CuePointer>,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) var stretchable: Boolean = false
) : MultipartDatamodel("pattern", id, name, deprecatedIDs, cues) {
    override fun copy(): Datamodel {
        return Pattern(id, name, deprecatedIDs, getCopyOfCues(), stretchable)
    }
}

@JsonPropertyOrder("type", "id", "name", "deprecatedIDs", "distance", "stretchable", "cues")
class Equidistant(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        cues: MutableList<CuePointer>,
        var distance: Float = 0f,
        var stretchable: Boolean = false
) : MultipartDatamodel("equidistant", id, name, deprecatedIDs, cues) {
    override fun copy(): Datamodel {
        return Equidistant(id, name, deprecatedIDs, getCopyOfCues(), distance, stretchable)
    }
}

@JsonPropertyOrder("type", "id", "name", "deprecatedIDs", "defaultDuration", "cues")
class KeepTheBeat(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        cues: MutableList<CuePointer>,
        var defaultDuration: Float = 0f
) : MultipartDatamodel("keepTheBeat", id, name, deprecatedIDs, cues) {
    override fun copy(): Datamodel {
        return KeepTheBeat(id, name, deprecatedIDs, getCopyOfCues(), defaultDuration)
    }
}

@JsonPropertyOrder("type", "id", "name", "deprecatedIDs", "responseIDs", "cues")
class RandomCue(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        cues: MutableList<CuePointer>,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) var responseIDs: List<String>? = null
) : MultipartDatamodel("randomCue", id, name, deprecatedIDs, cues) {
    override fun copy(): Datamodel {
        return RandomCue(id, name, deprecatedIDs, getCopyOfCues(), responseIDs?.toList())
    }
}

class SubtitleEntity(
        id: String, name: String, deprecatedIDs: MutableList<String>,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) var subtitleType: String? = null
) : Datamodel("subtitleEntity", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return SubtitleEntity(id, name, deprecatedIDs, subtitleType)
    }
}

class EndRemixEntity(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("endEntity", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return EndRemixEntity(id, name, deprecatedIDs)
    }
}

class ShakeEntity(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("shakeEntity", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return ShakeEntity(id, name, deprecatedIDs)
    }
}

class TextureEntity(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("textureEntity", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return TextureEntity(id, name, deprecatedIDs)
    }
}

class TapeMeasure(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("tapeMeasure", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return TapeMeasure(id, name, deprecatedIDs)
    }
}

class PlayalongEntity(id: String, name: String, deprecatedIDs: MutableList<String>,
                      var stretchable: Boolean, var method: String, var input: String)
    : Datamodel("playalongEntity", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return PlayalongEntity(id, name, deprecatedIDs, stretchable, method, input)
    }
}

class MusicDistortEntity(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("musicDistortEntity", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return MusicDistortEntity(id, name, deprecatedIDs)
    }
}

class PitchBenderEntity(id: String, name: String, deprecatedIDs: MutableList<String>)
    : Datamodel("pitchBenderEntity", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return PitchBenderEntity(id, name, deprecatedIDs)
    }
}

class PitchDependentEntity(id: String, name: String, deprecatedIDs: MutableList<String>,
                           var intervals: MutableMap<String, String>,
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) var responseIDs: List<String>? = null)
    : Datamodel("pitchDependent", id, name, deprecatedIDs) {
    override fun copy(): Datamodel {
        return PitchDependentEntity(id, name, deprecatedIDs, intervals.toMutableMap(), responseIDs?.toList())
    }
}