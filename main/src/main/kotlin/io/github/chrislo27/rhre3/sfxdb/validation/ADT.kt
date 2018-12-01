package io.github.chrislo27.rhre3.sfxdb.validation

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.SubtitleTypes
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
    val group: Result<String> by Property(Transformers.stringTransformer, "")
    val groupDefault: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    val priority: Result<Int> by Property(Transformers.intTransformer, 0)
    val searchHints: Result<MutableList<String>> by Property(Transformers.stringArrayTransformer, mutableListOf())
    val noDisplay: Result<Boolean> by Property(Transformers.booleanTransformer, false)

    override fun producePerfectADT(): Game {
        return Game(
            id.orException(), name.orException(), series.orException(),
            group.orException(), groupDefault.orException(), priority.orException(), searchHints.orException(), noDisplay.orException(),
            objects.orException().map { it.orException().producePerfectADT() }.toMutableList()
        )
    }

    override fun produceImperfectADT(): Game {
        return Game(
            id.orElse(""), name.orElse(""), series.orElse(Series.OTHER),
            group.orElse(""), groupDefault.orElse(false), priority.orElse(0), searchHints.orElse(mutableListOf()), noDisplay.orElse(false),
            objects.orElse(mutableListOf()).mapNotNull { it.orNull()?.produceImperfectADT() }.toMutableList()
        )
    }
}

abstract class DatamodelObject : Struct {
    val id: Result<String> by Property(Transformers.idTransformer)
    val name: Result<String> by Property(Transformers.nonEmptyStringTransformer)
    val deprecatedIDs: Result<MutableList<String>> by Property(Transformers.stringArrayTransformer, mutableListOf())

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
    var introSound: Result<String> by Property(Transformers.idTransformer, "")
    var endingSound: Result<String> by Property(Transformers.idTransformer, "")
    var responseIDs: Result<MutableList<String>> by Property(Transformers.responseIDsTransformer, mutableListOf())

    override fun producePerfectADT(): Cue {
        return Cue(
            id.orException(), name.orException(), deprecatedIDs.orException(), duration.orException(),
            stretchable.orException(), repitchable.orException(), fileExtension.orException(), introSound.orException(),
            endingSound.orException(), responseIDs.orException(), baseBpm.orException(), loops.orException()
        )
    }

    override fun produceImperfectADT(): Cue {
        return Cue(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), duration.orElse(0f),
            stretchable.orElse(false), repitchable.orElse(false), fileExtension.orElse(""), introSound.orNull(),
            endingSound.orNull(), responseIDs.orNull(), baseBpm.orElse(0f), loops.orElse(false)
        )
    }
}

class PatternObject : DatamodelObject() {
    var cues: Result<MutableList<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer()))

    // Optional after this line
    var stretchable: Result<Boolean> by Property(Transformers.booleanTransformer, false)

    override fun producePerfectADT(): Pattern {
        return Pattern(
            id.orException(), name.orException(), deprecatedIDs.orException(),
            cues.orException().map { it.orException().producePerfectADT() }.toMutableList(),
            stretchable.orException()
        )
    }

    override fun produceImperfectADT(): Pattern {
        return Pattern(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()),
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
            id.orException(), name.orException(), deprecatedIDs.orException(),
            cues.orException().map { it.orException().producePerfectADT() }.toMutableList(), distance.orException(), stretchable.orException()
        )
    }

    override fun produceImperfectADT(): Equidistant {
        return Equidistant(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()),
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
            id.orException(), name.orException(), deprecatedIDs.orException(),
            cues.orException().map { it.orException().producePerfectADT() }.toMutableList(), defaultDuration.orException()
        )
    }

    override fun produceImperfectADT(): KeepTheBeat {
        return KeepTheBeat(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()),
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
            id.orException(), name.orException(), deprecatedIDs.orException(),
            cues.orException().map { it.orException().producePerfectADT() }.toMutableList(), responseIDs.orException()
        )
    }

    override fun produceImperfectADT(): RandomCue {
        return RandomCue(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()),
            cues.orElse(mutableListOf()).mapNotNull { it.orNull()?.producePerfectADT() }.toMutableList(), responseIDs.orNull()
        )
    }
}

class EndRemixEntityObject : DatamodelObject() {
    override fun producePerfectADT(): EndRemixEntity {
        return EndRemixEntity(id.orException(), name.orException(), deprecatedIDs.orException())
    }

    override fun produceImperfectADT(): EndRemixEntity {
        return EndRemixEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()))
    }
}

class ShakeEntityObject : DatamodelObject() {
    override fun producePerfectADT(): ShakeEntity {
        return ShakeEntity(id.orException(), name.orException(), deprecatedIDs.orException())
    }

    override fun produceImperfectADT(): ShakeEntity {
        return ShakeEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()))
    }
}

class TextureEntityObject : DatamodelObject() {
    override fun producePerfectADT(): TextureEntity {
        return TextureEntity(id.orException(), name.orException(), deprecatedIDs.orException())
    }

    override fun produceImperfectADT(): TextureEntity {
        return TextureEntity(id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()))
    }
}

class SubtitleEntityObject : DatamodelObject() {
    val subtitleType: Result<SubtitleTypes> by Property(Transformers.subtitleTypesTransformer)

    override fun producePerfectADT(): SubtitleEntity {
        return SubtitleEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtitleType.orException().type)
    }

    override fun produceImperfectADT(): SubtitleEntity {
        return SubtitleEntity(
            id.orElse(""), name.orElse(""), deprecatedIDs.orElse(mutableListOf()), subtitleType.orNull()?.type ?: ""
        )
    }
}
