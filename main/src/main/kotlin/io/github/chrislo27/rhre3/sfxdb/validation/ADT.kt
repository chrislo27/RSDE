package io.github.chrislo27.rhre3.sfxdb.validation

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.SubtitleTypes
import io.github.chrislo27.rhre3.sfxdb.adt.*


interface Struct {
    fun produceImmutableADT(): Any
}

class GameObject : Struct {
    val id: Result<String> by Property(Transformers.gameIdTransformer)
    val name: Result<String> by Property(Transformers.nonEmptyStringTransformer)
    val series: Result<Series> by Property(Transformers.seriesTransformer, Series.OTHER)
    val objects: Result<List<Result<DatamodelObject>>> by Property(Transformers.transformerToList(Transformers.datamodelTransformer))

    // Optional after this line
    val group: Result<String> by Property(Transformers.stringTransformer, "")
    val groupDefault: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    val priority: Result<Int> by Property(Transformers.intTransformer, 0)
    val searchHints: Result<List<String>> by Property(Transformers.stringArrayTransformer, listOf())
    val noDisplay: Result<Boolean> by Property(Transformers.booleanTransformer, false)

    override fun produceImmutableADT(): Game {
        return Game(
            id.orException(), name.orException(), series.orException(),
            objects.orException().map { it.orException().produceImmutableADT() },
            group.orException(), groupDefault.orException(), priority.orException(), searchHints.orException(), noDisplay.orException()
        )
    }
}

abstract class DatamodelObject : Struct {
    val id: Result<String> by Property(Transformers.idTransformer)
    val name: Result<String> by Property(Transformers.nonEmptyStringTransformer)
    val deprecatedIDs: Result<List<String>> by Property(Transformers.stringArrayTransformer, listOf())

    abstract override fun produceImmutableADT(): Datamodel
}

class CuePointerObject : Struct {
    val id: Result<String> by Property(Transformers.idTransformer)

    var beat: Result<Float> by Property(Transformers.floatTransformer)
    var duration: Result<Float> by Property(Transformers.positiveFloatTransformer("Duration must be positive", true), 0f)
    val track: Result<Int> by Property(Transformers.intTransformer, 0)
    val semitone: Result<Int> by Property(Transformers.intTransformer, 0)
    val volume: Result<Int> by Property(Transformers.volumeTransformer, 100)
    val metadata: Result<Map<String, Any?>?> by Property(Transformers.cuePointerMetadataTransformer, null as Map<String, Any?>?)

    override fun produceImmutableADT(): CuePointer {
        return CuePointer(
            id.orException(), beat.orElse(Float.MIN_VALUE), duration.orException(), semitone.orException(),
            volume.orException(), track.orException(), metadata.orNull()
        )
    }
}

class CueObject : DatamodelObject() {
    val duration: Result<Float> by Property(Transformers.floatTransformer)

    // Optional after this line
    val stretchable: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    val repitchable: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    val fileExtension: Result<String> by Property(Transformers.soundFileExtensionTransformer, "ogg")
    val loops: Result<Boolean> by Property(Transformers.booleanTransformer, false)
    val baseBpm: Result<Float> by Property(Transformers.positiveFloatTransformer("Base BPM must be positive"), 0f)
    val introSound: Result<String> by Property(Transformers.idTransformer, "")
    val endingSound: Result<String> by Property(Transformers.idTransformer, "")
    val responseIDs: Result<List<String>> by Property(Transformers.responseIDsTransformer, listOf())

    override fun produceImmutableADT(): Cue {
        return Cue(
            id.orException(), name.orException(), deprecatedIDs.orException(), duration.orException(),
            stretchable.orException(), repitchable.orException(), fileExtension.orException(), introSound.orException(),
            endingSound.orException(), responseIDs.orException(), baseBpm.orException(), loops.orException()
        )
    }
}

class PatternObject : DatamodelObject() {
    val cues: Result<List<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer()))

    // Optional after this line
    val stretchable: Result<Boolean> by Property(Transformers.booleanTransformer, false)

    override fun produceImmutableADT(): Pattern {
        return Pattern(
            id.orException(), name.orException(), deprecatedIDs.orException(),
            cues.orException().map { it.orException().produceImmutableADT() },
            stretchable.orException()
        )
    }
}

class EquidistantObject : DatamodelObject() {
    val cues: Result<List<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer(beatNotUsed = true, durationNotUsed = true)))
    val distance: Result<Float> by Property(Transformers.positiveFloatTransformer("Distance must be positive, non-zero", false))
    val stretchable: Result<Boolean> by Property(Transformers.booleanTransformer)

    override fun produceImmutableADT(): Equidistant {
        return Equidistant(
            id.orException(), name.orException(), deprecatedIDs.orException(),
            cues.orException().map { it.orException().produceImmutableADT() }, distance.orException(), stretchable.orException()
        )
    }
}

class KeepTheBeatObject : DatamodelObject() {
    val cues: Result<List<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer()))
    val defaultDuration: Result<Float> by Property(Transformers.positiveFloatTransformer("Default duration must be positive, non-zero", false))

    override fun produceImmutableADT(): KeepTheBeat {
        return KeepTheBeat(
            id.orException(), name.orException(), deprecatedIDs.orException(),
            cues.orException().map { it.orException().produceImmutableADT() }, defaultDuration.orException()
        )
    }
}

class RandomCueObject : DatamodelObject() {
    val cues: Result<List<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer(beatNotUsed = true)))

    // Optional after this line
    val responseIDs: Result<List<String>> by Property(Transformers.responseIDsTransformer, listOf())

    override fun produceImmutableADT(): RandomCue {
        return RandomCue(
            id.orException(), name.orException(), deprecatedIDs.orException(),
            cues.orException().map { it.orException().produceImmutableADT() }, responseIDs.orException()
        )
    }
}

class EndRemixEntityObject : DatamodelObject() {
    override fun produceImmutableADT(): EndRemixEntity {
        return EndRemixEntity(id.orException(), name.orException(), deprecatedIDs.orException())
    }
}

class ShakeEntityObject : DatamodelObject() {
    override fun produceImmutableADT(): ShakeEntity {
        return ShakeEntity(id.orException(), name.orException(), deprecatedIDs.orException())
    }
}

class TextureEntityObject : DatamodelObject() {
    override fun produceImmutableADT(): TextureEntity {
        return TextureEntity(id.orException(), name.orException(), deprecatedIDs.orException())
    }
}

class SubtitleEntityObject : DatamodelObject() {
    val subtitleType: Result<SubtitleTypes> by Property(Transformers.subtitleTypesTransformer)

    override fun produceImmutableADT(): SubtitleEntity {
        return SubtitleEntity(id.orException(), name.orException(), deprecatedIDs.orException(), subtitleType.orException().type)
    }
}
