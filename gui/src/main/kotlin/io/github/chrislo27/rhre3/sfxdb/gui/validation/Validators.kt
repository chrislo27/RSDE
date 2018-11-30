package io.github.chrislo27.rhre3.sfxdb.gui.validation

import io.github.chrislo27.rhre3.sfxdb.gui.util.Localization
import io.github.chrislo27.rhre3.sfxdb.validation.Transformers
import javafx.scene.control.TextField
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.Validator


object Validators {

    val OBJ_ID_BLANK: Validator<TextField> = Validator { t, u ->
        ValidationResult.fromErrorIf(t, Localization["validation.objIDBlank"], u.text.isNullOrBlank())
    }
    val OBJ_ID_REGEX: Validator<TextField> = Validator { t, u ->
        ValidationResult.fromErrorIf(t, Localization["validation.objIDRegex"], !Transformers.ID_REGEX.matches(u.text))
    }
    val OBJ_ID_STAR_SUB: Validator<TextField> = Validator { t, u ->
        ValidationResult.fromErrorIf(t, Localization["validation.objIDStarSub"], !u.text.startsWith("*_"))
    }
    val CUE_ID_STAR_SUB: Validator<TextField> = Validator { t, u ->
        ValidationResult.fromErrorIf(t, Localization["validation.cueIDStarSub"], !u.text.startsWith("*/"))
    }

}
