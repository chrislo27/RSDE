@file:Suppress("ReplaceGetOrSet")

// Spread operator is weird with indexing operator so the suppress there

package io.github.chrislo27.rhre3.sfxdb.gui.util

import javafx.beans.InvalidationListener
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.ReadOnlyStringPropertyBase
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.Labeled
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import java.util.*

/**
 * Localization specialized for UI where instead of returning plain Strings, we return bindable StringProperties
 */
object UiLocalization {

    private val instantiatedKeys: MutableMap<String, ReadOnlyStringWrapper> = WeakHashMap()

    operator fun get(key: String): ReadOnlyStringProperty =
        instantiatedKeys.getOrPut(key) { ReadOnlyStringWrapper(Localization[key]) }.readOnlyProperty

    operator fun get(key: String, vararg initialParams: Any?): FormattableStringProperty =
        FormattableStringProperty(key).apply { parameters.addAll(*initialParams) }

    /**
     * Call this when the language has changed.
     */
    fun fireLocaleChanged() {
        instantiatedKeys.forEach { key, prop -> prop.value = Localization[key] }
    }

    /**
     * StringProperty that is also formattable by applying `parameters`.
     * Note that the paremeters take an Any, but if the argument is an ObservableValue, it will react to its changes.
     * Example
     * ```
     *   val counterDoubleProperty: DoubleProperty = ...
     *   val formattableProperty = UiLocalization["counter-format", 0.0]
     *   formattableProperty.parameters.setAll(counterDoubleProperty) // now the formattableProperty will update its value whenever the counter updates
     * ```
     */
    class FormattableStringProperty(val key: String) : ReadOnlyStringPropertyBase() {
        val parameters: ObservableList<Any?> = FXCollections.observableArrayList()
        private val paramsObservable: InvalidationListener = InvalidationListener { fireValueChangedEvent() }

        init {
            parameters.addListener(ListChangeListener { evt ->
                while (evt.next()) {
                    evt.removed.forEach { (it as? ObservableValue<*>)?.removeListener(paramsObservable) }
                    evt.addedSubList.forEach { (it as? ObservableValue<*>)?.addListener(paramsObservable) }
                }
                fireValueChangedEvent()
            })
        }

        private val underlying: ReadOnlyStringProperty = UiLocalization[key].apply { addListener(paramsObservable) }

        override fun getName(): String = key
        override fun get(): String {
            underlying.get() // This "pings" it, helps to resolve some invalidation states
            return Localization.get(key, *parameters.map { (it as? ObservableValue<*>)?.value ?: it }.toTypedArray())
        }

        override fun getBean(): FormattableStringProperty = this
    }
}

fun <T : Labeled> T.bindLocalized(key: String): T {
    textProperty().bind(UiLocalization[key])
    return this
}

fun <T : Labeled> T.bindLocalized(key: String, vararg initialParams: Any?): T {
    textProperty().bind(UiLocalization.get(key, *initialParams))
    return this
}

fun Tooltip.bindLocalized(key: String): Tooltip = this.apply { textProperty().bind(UiLocalization[key]) }
fun Tooltip.bindLocalized(key: String, vararg initialParams: Any?): Tooltip = this.apply { textProperty().bind(UiLocalization.get(key, *initialParams)) }
fun Menu.bindLocalized(key: String): Menu = this.apply { textProperty().bind(UiLocalization[key]) }
fun Menu.bindLocalized(key: String, vararg initialParams: Any?): Menu = this.apply { textProperty().bind(UiLocalization.get(key, *initialParams)) }
fun MenuItem.bindLocalized(key: String): MenuItem = this.apply { textProperty().bind(UiLocalization[key]) }
fun MenuItem.bindLocalized(key: String, vararg initialParams: Any?): MenuItem = this.apply { textProperty().bind(UiLocalization.get(key, *initialParams)) }
