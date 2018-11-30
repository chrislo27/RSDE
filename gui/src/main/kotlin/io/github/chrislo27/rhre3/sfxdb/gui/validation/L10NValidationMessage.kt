package io.github.chrislo27.rhre3.sfxdb.gui.validation

import javafx.beans.value.ObservableValue
import javafx.scene.control.Control
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage


class L10NValidationMessage(private val targetControl: Control, val textProperty: ObservableValue<String>, private val severity: Severity)
    : ValidationMessage {

    override fun getTarget(): Control {
        return targetControl
    }

    override fun getText(): String {
        return textProperty.value
    }

    override fun getSeverity(): Severity {
        return severity
    }

}