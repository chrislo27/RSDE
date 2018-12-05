package io.github.chrislo27.rhre3.sfxdb.gui.validation

import io.github.chrislo27.rhre3.sfxdb.Constants
import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.SoundFileExtensions
import io.github.chrislo27.rhre3.sfxdb.adt.Cue
import io.github.chrislo27.rhre3.sfxdb.adt.Datamodel
import io.github.chrislo27.rhre3.sfxdb.adt.Game
import io.github.chrislo27.rhre3.sfxdb.gui.editor.panes.GameObjPane
import io.github.chrislo27.rhre3.sfxdb.gui.util.UiLocalization
import io.github.chrislo27.rhre3.sfxdb.validation.Transformers
import javafx.beans.value.ObservableValue
import javafx.scene.control.Control
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.Validator
import java.io.File
import kotlin.math.absoluteValue


object Validators {

    fun fromErrorIf(control: Control, text: ObservableValue<String>, condition: Boolean): ValidationResult {
        return fromMessageIf(control, text, Severity.ERROR, condition)
    }

    fun fromWarningIf(control: Control, text: ObservableValue<String>, condition: Boolean): ValidationResult {
        return fromMessageIf(control, text, Severity.WARNING, condition)
    }

    fun fromErrorIf(control: Control, text: ObservableValue<String>, predicate: () -> Boolean): ValidationResult {
        return fromMessageIf(control, text, Severity.ERROR, predicate)
    }

    fun fromWarningIf(control: Control, text: ObservableValue<String>, predicate: () -> Boolean): ValidationResult {
        return fromMessageIf(control, text, Severity.WARNING, predicate)
    }

    fun fromMessageIf(control: Control, text: ObservableValue<String>, severity: Severity, condition: Boolean): ValidationResult {
        return ValidationResult().apply {
            if (condition) add(L10NValidationMessage(control, text, severity))
        }
    }

    fun fromMessageIf(control: Control, text: ObservableValue<String>, severity: Severity, predicate: () -> Boolean): ValidationResult {
        return ValidationResult().apply {
            if (predicate()) add(L10NValidationMessage(control, text, severity))
        }
    }

    // General
    val GAME_ID: Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.objIDRegex", Transformers.GAME_ID_REGEX.pattern], !Transformers.GAME_ID_REGEX.matches(u))
    }
    val OBJ_ID_BLANK: Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.objIDBlank"], u.isNullOrBlank())
    }
    val OBJ_ID_REGEX: Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.objIDRegex", Transformers.ID_REGEX.pattern], !Transformers.ID_REGEX.matches(u))
    }
    val OBJ_ID_STAR_SUB: Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.objIDStarSub"], u != null && !u.startsWith("*_"))
    }
    val CUE_ID_STAR_SUB: Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.cueIDStarSub"], u != null && !u.startsWith("*/"))
    }
    val NAME_BLANK: Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.nameBlank"], u.isNullOrBlank())
    }

    fun identicalObjID(game: Game, datamodel: Datamodel) = Validator<String> { t, u ->
        fromErrorIf(t, UiLocalization["validation.identicalObjID"], u != null && game.objects.any { it != datamodel && it.id == u.trim() })
    }

    // CuePointer
    val EXTERNAL_CUE_POINTER: Validator<String> = Validator { t, u ->
        fromWarningIf(t, UiLocalization["validation.cuePointerExtDependency"], u != null && !u.startsWith("*") && u.isNotEmpty())
    }
    val TRACK_TOO_TALL: Validator<Int> = Validator { t, u ->
        fromWarningIf(t, UiLocalization["validation.cuePointerTooTall", Constants.TRACK_RANGE.first - 1], u.absoluteValue >= Constants.TRACK_RANGE.first)
    }
    val ABNORMAL_SEMITONE: Validator<Int> = Validator { t, u ->
        fromWarningIf(t, UiLocalization["validation.abnormalSemitone", Constants.SEMITONE_RANGE.toString()], u !in Constants.SEMITONE_RANGE)
    }

    fun cuePointerPointsNowhere(gameObj: Game): Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.invalidCuePointer"], u != null && u.startsWith("*") && u.isNotEmpty() && u !in gameObj.objects.map { it.id })
    }

    // Response IDs
    val EXTERNAL_RESPONSE_IDS: Validator<List<String>> = Validator { t, u ->
        val external = u.filter { !it.startsWith("*") }
        fromWarningIf(t, UiLocalization["validation.responseIDsExtDependency", external], external.isNotEmpty())
    }

    fun responseIDsPointsNowhere(game: Game): Validator<List<String>> = Validator { t, u ->
        ValidationResult().apply {
            val allIDs = game.objects.map { it.id }
            val invalidIDs: List<String> = u.filter { it !in allIDs }
            if (invalidIDs.isNotEmpty()) {
                add(L10NValidationMessage(t, UiLocalization["validation.invalidResponseIDs", invalidIDs], Severity.ERROR))
            }
        }
    }

    // GameObject
    val NO_DISPLAY: Validator<Boolean> = Validator { t, u ->
        fromWarningIf(t, UiLocalization["validation.noDisplay"], u)
    }

    fun appropriateNameSuffixFromSeries(gameObjPane: GameObjPane): Validator<String> = Validator { t, u ->
        val selectedSeries: Series? = gameObjPane.seriesComboBox.value
        val wrongSeries: Series? = Series.VALUES.find {
            "(${it.properName})" in u && selectedSeries != it
        }
        fromWarningIf(t, UiLocalization["validation.gameNameWithSeries", "(${wrongSeries?.properName})", selectedSeries], !(selectedSeries == null || wrongSeries == null || selectedSeries.properName.isEmpty()))
    }

    fun deprecatedFeverName(gameObjPane: GameObjPane): Validator<String> = Validator { t, u ->
        val selectedSeries: Series? = gameObjPane.seriesComboBox.value
        fromWarningIf(t, UiLocalization["validation.deprecatedFeverName"], if (selectedSeries == null || selectedSeries.properName.isEmpty()) false else (selectedSeries == Series.FEVER && u.contains("Wii")))
    }

    // CueObject
    val FILE_EXT_NOT_OGG: Validator<String> = Validator { t, u ->
        fromWarningIf(t, UiLocalization["validation.cueFileExt", SoundFileExtensions.DEFAULT.fileExt], !u.isNullOrEmpty() && u.toLowerCase() != SoundFileExtensions.DEFAULT.fileExt)
    }

    fun soundFileNotFound(parentFolder: File, cue: Cue): Validator<String> = Validator { t, u ->
        val expectedFile = parentFolder.resolve("${cue.id.replaceFirst("*/", "")}.${cue.fileExtension}")
        fromWarningIf(t, UiLocalization["validation.cueFileNotFound", expectedFile.name], cue.id.startsWith("*/") && !expectedFile.exists())
    }

    // MultipartObject


}
