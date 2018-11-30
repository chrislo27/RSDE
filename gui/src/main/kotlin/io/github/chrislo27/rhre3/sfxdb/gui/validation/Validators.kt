package io.github.chrislo27.rhre3.sfxdb.gui.validation

import io.github.chrislo27.rhre3.sfxdb.Series
import io.github.chrislo27.rhre3.sfxdb.SoundFileExtensions
import io.github.chrislo27.rhre3.sfxdb.gui.editor.panes.GamePane
import io.github.chrislo27.rhre3.sfxdb.gui.util.UiLocalization
import io.github.chrislo27.rhre3.sfxdb.validation.Transformers
import javafx.beans.value.ObservableValue
import javafx.scene.control.Control
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.Validator


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
        fromErrorIf(t, UiLocalization["validation.objIDStarSub"], !u.startsWith("*_"))
    }
    val CUE_ID_STAR_SUB: Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.cueIDStarSub"], !u.startsWith("*/"))
    }
    val NAME_BLANK: Validator<String> = Validator { t, u ->
        fromErrorIf(t, UiLocalization["validation.nameBlank"], u.isNullOrBlank())
    }

    // GameObject
    val NO_DISPLAY: Validator<Boolean> = Validator { t, u ->
        fromWarningIf(t, UiLocalization["validation.noDisplay"], u)
    }
    fun nameSuffixFromSeries(gamePane: GamePane): Validator<String> = Validator { t, u ->
        // FIXME not very good at detecting when this is necessary. Lots of edge cases
        val selectedSeries: Series? = gamePane.seriesComboBox.value
        val suffix = "(${selectedSeries?.properName})"
        fromWarningIf(t, UiLocalization["validation.gameNameSuffixFromSeries", selectedSeries, suffix], if (selectedSeries == null || selectedSeries.properName.isEmpty()) false else (!u.endsWith(" $suffix")))
    }

    // CueObject
    val FILE_EXT_NOT_OGG: Validator<String> = Validator { t, u ->
        fromWarningIf(t, UiLocalization["validation.cueFileExt", SoundFileExtensions.DEFAULT.fileExt], u.toLowerCase() != SoundFileExtensions.DEFAULT.fileExt)
    }

}
