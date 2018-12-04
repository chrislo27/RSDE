package io.github.chrislo27.rhre3.sfxdb.gui.editor

import org.controlsfx.validation.ValidationResult


interface HasValidator {

    fun isInvalid(): Boolean

    fun forceUpdate()

    fun getValidationResult(): ValidationResult

}