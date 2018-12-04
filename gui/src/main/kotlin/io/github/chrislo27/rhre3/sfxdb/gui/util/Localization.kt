package io.github.chrislo27.rhre3.sfxdb.gui.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE
import java.io.InputStream
import java.io.InputStreamReader
import java.text.MessageFormat
import java.util.*


object Localization {

    init {
        System.setProperty("java.util.PropertyResourceBundle.encoding", "UTF-8") // Java 9+
    }

    fun loadLangsFromFile(path: String = "localization/langs.json"): List<NamedLocale> {
        val array = ObjectMapper().readTree(Thread.currentThread().contextClassLoader.getResource(path).readText())
        return array.map { obj ->
            val name = obj["name"]?.asText("NO_NAME") ?: "NO_NAME"
            val localeObj = obj["locale"]
            val language = localeObj["language"]?.asText(null)
            val country = localeObj["country"]?.asText(null)
            val variant = localeObj["variant"]?.asText(null)

            val locale = if (variant == null) {
                if (country == null) Locale(language) else Locale(language, country)
            } else if (country == null) Locale(language) else Locale(language, country, variant)
            NamedLocale(name, locale)
        }
    }

    lateinit var langs: Map<NamedLocale, Bundle>
        private set

    init {
        refreshLangs()
    }

    lateinit var currentBundle: Bundle
        private set
    private val bundle: ResourceBundle
        get() = currentBundle.bundle
    private val missingKeys: MutableSet<String>
        get() = currentBundle.missingKeys
    private val messageFormat: MessageFormat
        get() = currentBundle.messageFormat

    fun refreshLangs() {
        RSDE.LOGGER.info("Loading langs from file")
        val langsList = loadLangsFromFile()
        ResourceBundle.clearCache()
        langs = langsList.associateWith { nl ->
            Bundle(nl, ResourceBundle.getBundle("localization/default", nl.locale, UTF8ResourceBundleControl()))
        }
        val firstLang = langsList.firstOrNull() ?: error("No languages defined!")
        currentBundle = langs[firstLang]!!
        missingKeys.clear()
        UiLocalization.fireLocaleChanged()
    }

    fun changeLang(namedLocale: NamedLocale) {
        currentBundle = langs[namedLocale] ?: error("Named locale $namedLocale is not loaded")
        UiLocalization.fireLocaleChanged()
    }

    /**
     * Returns the string associated with the key from the resource bundle.
     * If there is no string present, the key is returned.
     */
    operator fun get(key: String): String {
        return try {
            if (key in missingKeys) key else bundle.getString(key)
        } catch (e: MissingResourceException) {
            RSDE.LOGGER.warn("Missing resource for key \"$key\"")
            missingKeys += key
            key
        }
    }

    /**
     * Formats the key string using [MessageFormat] with the given parameters.
     * Note that escaping is different: the only escape required is that left curly braces are escaped with two ({{).
     * If there is no string associated with the key, the key itself is returned.
     * @see String.escapeMessageFormat
     */
    operator fun get(key: String, vararg params: Any?): String {
        if (params.isEmpty()) return get(key)
        return try {
            if (key in missingKeys) key else messageFormat.apply { applyPattern(bundle.getString(key).escapeMessageFormat()) }.format(
                params
            )
        } catch (e: MissingResourceException) {
            RSDE.LOGGER.warn("Missing resource for key \"$key\"")
            missingKeys += key
            key
        }
    }

    /**
     * Escapes a normal string to be compatible with [MessageFormat].
     * This escapes single quotes (' to '') and escaped left curly braces ({{ to '{').
     */
    fun String.escapeMessageFormat(): String {
        return this.replace("'", "''").replace("{{", "'{'")
    }

    class UTF8ResourceBundleControl : ResourceBundle.Control() {
        override fun newBundle(baseName: String, locale: Locale?, format: String?, loader: ClassLoader, reload: Boolean): ResourceBundle {
            val bundleName = toBundleName(baseName, locale)
            val resourceName = toResourceName(bundleName, "properties")
            var bundle: ResourceBundle? = null
            var stream: InputStream? = null
            if (reload) {
                val url = loader.getResource(resourceName)
                if (url != null) {
                    val connection = url.openConnection()
                    if (connection != null) {
                        connection.useCaches = false
                        stream = connection.getInputStream()
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName)
            }
            if (stream != null) {
                try {
                    bundle = PropertyResourceBundle(InputStreamReader(stream, "UTF-8"))
                } finally {
                    stream.close()
                }
            }
            return bundle!!
        }
    }

    data class NamedLocale(val name: String, val locale: Locale)
    data class Bundle(val locale: NamedLocale, val bundle: ResourceBundle,
                      val messageFormat: MessageFormat = MessageFormat("", locale.locale),
                      val missingKeys: MutableSet<String> = mutableSetOf())

}