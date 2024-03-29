package io.github.chrislo27.rhre3.sfxdb.validation

import io.github.chrislo27.rhre3.sfxdb.*
import io.github.chrislo27.rhre3.sfxdb.adt.*


interface Struct {
    fun producePerfectADT(): Any
    fun produceImperfectADT(): Any
}

class GameObject : Struct {
    var id: Result<String> by Property(Transformers.gameIdTransformer)
    val name: Result<String> by Property(Transformers.nonEmptyStringTransformer)
    val series: Result<Series> by Property(Transformers.seriesTransformer, Series.OTHER)
    val objects: Result<MutableList<Result<DatamodelObject>>> by Property(Transformers.transformerToList(Transformers.datamodelTransformer))

    // Optional after this line
    val language: Result<Language> by Property(Transformers.languageTransformer, Language.NONE)
    val group: Result<String> by Property(Transformers.stringTransformer, "")
    val groupDefault: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    val priority: Result<Int> by Property(Transformers.intTransformer, 0)
    val searchHints: Result<MutableList<String>> by Property(Transformers.stringArrayTransformer, mutableListOf())
    val noDisplay: Result<Boolean> by Property(Transformers.booleanTransformer, false)

    override fun producePerfectADT(): Game {
        return Game(
            id.orException(), name.orException(), series.orException(),
            language.orException(),
            group.orException(), groupDefault.orException(), priority.orException(), searchHints.orException(), noDisplay.orException(),
            objects.orException().map { it.orException().producePerfectADT() }.toMutableList()
        )
    }

    override fun produceImperfectADT(): Game {
        return Game(
            id.orElse(""), name.orElse(""), series.orElse(Series.OTHER),
            language.orElse(Language.NONE),
            group.orElse(""), groupDefault.orElse(false), priority.orElse(0), searchHints.orElse(mutableListOf()), noDisplay.orElse(false),
            objects.orElse(mutableListOf()).mapNotNull { it.orNull()?.produceImperfectADT() }.toMutableList()
        )
    }
}

abstract class DatamodelObject : Struct {
    val id: Result<String> by Property(Transformers.idTransformer)
    val name: Result<String> by Property(Transformers.nonEmptyStringTransformer)
    val deprecatedIDs: Result<MutableList<String>> by Property(Transformers.stringArrayTransformer, mutableListOf())
    val subtext: Result<String> by Property(Transformers.stringTransformer, "")

    abstract override fun producePerfectADT(): Datamodel
    abstract override fun produceImperfectADT(): Datamodel
}

class CuePointerObject : Struct {
    val id: Result<String> by Property(Transformers.idTransformer)

    var beat: Result<Float> by Property(Transformers.floatTransformer)
    var duration: Result<Float> by Property(Transformers.positiveFloatTransformer("Duration must be positive", true), 0f)
    var track: Result<Int> by Property(Transformers.intTransformer, 0)
    var semitone: Result<Int> by Property(Transformers.intTransformer, 0)
    var volume: Result<Int> by Property(Transformers.volumeTransformer, 100)
    var metadata: Result<MutableMap<String, Any?>?> by Property(Transformers.cuePointerMetadataTransformer, null as MutableMap<String, Any?>?)

    override fun producePerfectADT(): CuePointer {
        return CuePointer(
            id.orException(), beat.orElse(Float.MIN_VALUE), duration.orException(), semitone.orException(),
            volume.orException(), track.orException(), metadata.orNull()
        )
    }

    override fun produceImperfectADT(): CuePointer {
        return CuePointer(
            id.orElse(""), beat.orElse(Float.MIN_VALUE), duration.orElse(0f), semitone.orElse(0),
            volume.orElse(100), track.orElse(0), metadata.orNull()
        )
    }
}

class CueObject : DatamodelObject() {
    var duration: Result<Float> by Property(Transformers.floatTransformer)

    // Optional after this line
    var stretchable: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    var repitchable: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    var fileExtension: Result<String> by Property(Transformers.soundFileExtensionTransformer, "ogg")
    var loops: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    var baseBpm: Result<Float> by Property(Transformers.positiveFloatTransformer("Base BPM must be positive"), 0f)
    var useTimeStretching: Result<Boolean> by Property(Transformers.booleanTransformer, true)
    var baseBpmRules: Result<BaseBpmRules> by Property(Transformers.baseBpmRulesTransformer, BaseBpmRules.ALWAYS)
    var introSound: Result<String> by Property(Transformers.idTransformer, "")
    var endingSound: Result<String> by Property(Transformers.idTransformer, "")
    var responseIDs: Result<MutableList<String>> by Property(Transformers.responseIDsTransformer, mutableListOf())
    var earliness: Result<Float> by Property(Transformers.positiveFloatTransformer("Earliness must be positive"), 0f)
    var loopStart: Result<Float> by Property(Transformers.positiveFloatTransformer("Loop start must be positive"), 0f)
    var loopEnd: Result<Float> by Property(Transformers.floatTransformer, 0f)
    var pitchBending: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    var writtenPitch: Result<Int> by Property(Transformers.intTransformer, 0)

    override fun producePerfectADT(): Cue {
        return Cue(
            id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException(), duration.orException(),
            stretchable.orException(), repitchable.orException(), fileExtension.orException(), introSound.orException(),
            endingSound.orException(), responseIDs.orException(), baseBpm.orException(), useTimeStretching.orException(),
            baseBpmRules.orException(),
            loops.orException(), earliness.orException(), loopStart.orException(), loopEnd.orException(),
            pitchBending.orException(), writtenPitch.orException()
        )
    }

    override fun produceImperfectADT(): Cue {
        return Cue(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""), duration.orElse(0f),
            stretchable.orElse(false), repitchable.orElse(false), fileExtension.orElse(""), introSound.orNull(),
            endingSound.orNull(), responseIDs.orNull(), baseBpm.orElse(0f), useTimeStretching.orElse(true),
            baseBpmRules.orElse(BaseBpmRules.ALWAYS),
            loops.orElse(false), earliness.orElse(0f), loopStart.orElse(0f), loopEnd.orElse(0f),
            pitchBending.orElse(false), writtenPitch.orElse(0)
        )
    }
}

class PatternObject : DatamodelObject() {
    var cues: Result<MutableList<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer()))

    // Optional after this line
    var stretchable: Result<Boolean> by Property(Transformers.booleanTransformer, false)

    override fun producePerfectADT(): Pattern {
        return Pattern(
            id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException(),
            cues.orException().map { it.orException().producePerfectADT() }.toMutableList(),
            stretchable.orException()
        )
    }

    override fun produceImperfectADT(): Pattern {
        return Pattern(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""),
            cues.orElse(mutableListOf()).mapNotNull { it.orNull()?.producePerfectADT() }.toMutableList(),
            stretchable.orElse(false)
        )
    }
}

class EquidistantObject : DatamodelObject() {
    var cues: Result<MutableList<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer(beatNotUsed = true, durationNotUsed = true)))
    var distance: Result<Float> by Property(Transformers.positiveFloatTransformer("Distance must be positive, non-zero", false))
    var stretchable: Result<Boolean> by Property(Transformers.booleanTransformer)

    override fun producePerfectADT(): Equidistant {
        return Equidistant(
            id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException(),
            cues.orException().map { it.orException().producePerfectADT() }.toMutableList(), distance.orException(), stretchable.orException()
        )
    }

    override fun produceImperfectADT(): Equidistant {
        return Equidistant(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""),
            cues.orElse(mutableListOf()).mapNotNull { it.orNull()?.producePerfectADT() }.toMutableList(),
            distance.orElse(0f), stretchable.orElse(false)
        )
    }
}

class KeepTheBeatObject : DatamodelObject() {
    var cues: Result<MutableList<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer()))
    var defaultDuration: Result<Float> by Property(Transformers.positiveFloatTransformer("Default duration must be positive, non-zero", false))

    override fun producePerfectADT(): KeepTheBeat {
        return KeepTheBeat(
            id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException(),
            cues.orException().map { it.orException().producePerfectADT() }.toMutableList(), defaultDuration.orException()
        )
    }

    override fun produceImperfectADT(): KeepTheBeat {
        return KeepTheBeat(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""),
            cues.orElse(mutableListOf()).mapNotNull { it.orNull()?.producePerfectADT() }.toMutableList(), defaultDuration.orElse(0f)
        )
    }
}

class RandomCueObject : DatamodelObject() {
    var cues: Result<MutableList<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer(beatNotUsed = true)))

    // Optional after this line
    var responseIDs: Result<MutableList<String>> by Property(Transformers.responseIDsTransformer, mutableListOf())

    override fun producePerfectADT(): RandomCue {
        return RandomCue(
            id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException(),
            cues.orException().map { it.orException().producePerfectADT() }.toMutableList(), responseIDs.orException()
        )
    }

    override fun produceImperfectADT(): RandomCue {
        return RandomCue(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""),
            cues.orElse(mutableListOf()).mapNotNull { it.orNull()?.producePerfectADT() }.toMutableList(), responseIDs.orNull()
        )
    }
}

class EndRemixEntityObject : DatamodelObject() {
    override fun producePerfectADT(): EndRemixEntity {
        return EndRemixEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException())
    }

    override fun produceImperfectADT(): EndRemixEntity {
        return EndRemixEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""))
    }
}

class ShakeEntityObject : DatamodelObject() {
    override fun producePerfectADT(): ShakeEntity {
        return ShakeEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException())
    }

    override fun produceImperfectADT(): ShakeEntity {
        return ShakeEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""))
    }
}

class TextureEntityObject : DatamodelObject() {
    override fun producePerfectADT(): TextureEntity {
        return TextureEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException())
    }

    override fun produceImperfectADT(): TextureEntity {
        return TextureEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""))
    }
}

class TapeMeasureObject : DatamodelObject() {
    override fun producePerfectADT(): TapeMeasure {
        return TapeMeasure(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException())
    }

    override fun produceImperfectADT(): TapeMeasure {
        return TapeMeasure(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""))
    }
}

class SubtitleEntityObject : DatamodelObject() {
    val subtitleType: Result<SubtitleTypes> by Property(Transformers.subtitleTypesTransformer)

    override fun producePerfectADT(): SubtitleEntity {
        return SubtitleEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException(), subtitleType.orException().type)
    }

    override fun produceImperfectADT(): SubtitleEntity {
        return SubtitleEntity(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""), subtitleType.orNull()?.type ?: ""
        )
    }
}

class PlayalongEntityObject : DatamodelObject() {
    var stretchable: Result<Boolean> by Property(Transformers.booleanTransformer)
    var input: Result<PlayalongInput> by Property(Transformers.playalongInputTransformer)
    var method: Result<PlayalongMethod> by Property(Transformers.playalongMethodTransformer)

    override fun producePerfectADT(): PlayalongEntity {
        return PlayalongEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException(),
            stretchable.orException(), method.orException().name, input.orException().id)
    }

    override fun produceImperfectADT(): PlayalongEntity {
        return PlayalongEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""),
            stretchable.orElse(false), method.orElse(PlayalongMethod.PRESS).name, input.orElse(PlayalongInput.BUTTON_A).id)
    }
}

class MusicDistortEntityObject : DatamodelObject() {
    override fun producePerfectADT(): MusicDistortEntity {
        return MusicDistortEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException())
    }

    override fun produceImperfectADT(): MusicDistortEntity {
        return MusicDistortEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""))
    }
}

class PitchBenderEntityObject : DatamodelObject() {
    override fun producePerfectADT(): PitchBenderEntity {
        return PitchBenderEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException())
    }

    override fun produceImperfectADT(): PitchBenderEntity {
        return PitchBenderEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""))
    }
}

class PitchDependentEntityObject : DatamodelObject() {
    var intervals: Result<MutableMap<String, String>> by Property(Transformers.stringStringMapTransformer, mutableMapOf())
    // Optional after this line
    var responseIDs: Result<MutableList<String>> by Property(Transformers.responseIDsTransformer, mutableListOf())
    
    override fun producePerfectADT(): PitchDependentEntity {
        return PitchDependentEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtext.orException(),
            intervals.orException(), responseIDs.orException())
    }

    override fun produceImperfectADT(): PitchDependentEntity {
        return PitchDependentEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtext.orElse(""),
                intervals.orElse(mutableMapOf()), responseIDs.orElse(mutableListOf()))
    }
}
