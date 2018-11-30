package io.github.chrislo27.rhre3.sfxdb.gui.validation

import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import org.controlsfx.control.decoration.Decoration
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.ValidationSupport
import org.controlsfx.validation.Validator
import org.controlsfx.validation.decoration.GraphicValidationDecoration


class L10NValidationSupport : ValidationSupport() {
    init {
        validationDecorator = object : GraphicValidationDecoration() {
            override fun createRequiredDecorations(target: Control?): MutableCollection<Decoration> {
                return mutableListOf()
            }

            override fun createTooltip(message: ValidationMessage?): Tooltip {
                val original = super.createTooltip(message)
                if (message is L10NValidationMessage) {
                    original.textProperty().bind(message.textProperty)
                }
                return original
            }
        }
    }

    override fun <T : Any?> registerValidator(c: Control, validator: Validator<T>): Boolean {
        return registerValidator(c, false, validator)
    }

    fun <T : Any?> registerValidators(c: Control, vararg validators: Validator<T>): Boolean {
        return registerValidator(c, false, Validator.combine(*validators))
    }
}