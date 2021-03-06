package io.github.chrislo27.rhre3.sfxdb.gui.editor

import org.controlsfx.validation.ValidationResult


interface HasValidator {

    fun hasErrors(): Boolean

    fun forceUpdate()

    fun getValidationResult(): ValidationResult

}