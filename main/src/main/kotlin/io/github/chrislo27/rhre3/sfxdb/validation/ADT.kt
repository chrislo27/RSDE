package io.github.chrislo27.rhre3.sfxdb.validation

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.SubtitleTypes


interface Struct

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
}

abstract class DatamodelObject : Struct {
    val id: Result<String> by Property(Transformers.idTransformer)
    val name: Result<String> by Property(Transformers.nonEmptyStringTransformer)
    val deprecatedIDs: Result<List<String>> by Property(Transformers.stringArrayTransformer, listOf())
}

class CuePointerObject : Struct {
    val id: Result<String> by Property(Transformers.idTransformer)

    var beat: Result<Float> by Property(Transformers.floatTransformer)
    var duration: Result<Float> by Property(Transformers.positiveFloatTransformer("Duration must be positive", true), 0f)
    val track: Result<Int> by Property(Transformers.intTransformer, 0)
    val semitone: Result<Int> by Property(Transformers.intTransformer, 0)
    val volume: Result<Int> by Property(Transformers.volumeTransformer, 100)
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
}

class PatternObject : DatamodelObject() {
    val cues: Result<List<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer()))

    // Optional after this line
    val stretchable: Result<Boolean> by Property(Transformers.booleanTransformer, false)
}

class EquidistantObject : DatamodelObject() {
    val cues: Result<List<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer(beatNotUsed = true, durationNotUsed = true)))
    val distance: Result<Float> by Property(Transformers.positiveFloatTransformer("Distance must be positive, non-zero", false))
    val stretchable: Result<Boolean> by Property(Transformers.booleanTransformer)
}

class KeepTheBeatObject : DatamodelObject() {
    val cues: Result<List<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer()))
    val defaultDuration: Result<Float> by Property(Transformers.positiveFloatTransformer("Default duration must be positive, non-zero", false))
}

class RandomCueObject : DatamodelObject() {
    val cues: Result<List<Result<CuePointerObject>>> by Property(Transformers.transformerToList(Transformers.cuePointerTransformer(beatNotUsed = true)))

    // Optional after this line
    val responseIDs: Result<List<String>> by Property(Transformers.responseIDsTransformer, listOf())
}

class EndRemixEntityObject : DatamodelObject()
class ShakeEntityObject : DatamodelObject()
class TextureEntityObject : DatamodelObject()
class SubtitleEntityObject : DatamodelObject() {
    val subtitleType: Result<SubtitleTypes> by Property(Transformers.subtitleTypesTransformer)
}
