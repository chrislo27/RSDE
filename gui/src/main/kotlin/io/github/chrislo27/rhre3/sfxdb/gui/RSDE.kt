package io.github.chrislo27.rhre3.sfxdb.gui

import io.github.chrislo27.rhre3.sfxdb.gui.discord.ChangesPresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DefaultRichPresence
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DiscordHelper
import io.github.chrislo27.rhre3.sfxdb.gui.registry.GameRegistry
import io.github.chrislo27.rhre3.sfxdb.gui.scene.EditorPane
import io.github.chrislo27.rhre3.sfxdb.gui.scene.WelcomePane
import io.github.chrislo27.rhre3.sfxdb.gui.util.*
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.ButtonType
import javafx.scene.control.DialogPane
import javafx.scene.image.Image
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.system.exitProcess


class RSDE : Application() {

    companion object {
        const val TITLE = "RHRE SFX Database Editor"
        val VERSION = Version(1, 0, 4, "DEVELOPMENT")
        val MIN_RHRE_VERSION = Version(3, 15, 0)
        val rootFolder: File = File(System.getProperty("user.home")).resolve(".rsde/").apply { mkdirs() }
        val rhreRoot: File = File(System.getProperty("user.home")).resolve(".rhre3/").apply {
            mkdirs()
        }
        val customSFXFolder: File = rhreRoot.resolve("customSounds/").apply {
            mkdirs()
        }
        val SFX_DB_BRANCH = "master"
        const val GITHUB = "https://github.com/chrislo27/RSDE"
        const val RHRE_GITHUB = "https://github.com/chrislo27/RhythmHeavenRemixEditor"
        val rhreSfxRoot: File = rhreRoot.resolve("sfx/$SFX_DB_BRANCH/")
        const val LICENSE_NAME = "Apache License 2.0"

        val LOGGER: Logger = LogManager.getContext(RSDE::class.java.classLoader, false).getLogger("RSDE")
        val startTimeMillis: Long = System.currentTimeMillis()

        val windowIcons: List<Image> by lazy { listOf(Image("icon/16.png"), Image("icon/24.png"), Image("icon/32.png")) }

        @JvmStatic
        fun main(args: Array<String>) {
            LOGGER.info("Launching $TITLE $VERSION...")
            Application.launch(RSDE::class.java, *args)
        }

        /**
         * @param version "dev" or "latest"
         */
        fun getDocsUrl(version: String): String = "https://rhre.readthedocs.io/en/$version/JSON-object-definitions/"
    }

    lateinit var primaryStage: Stage
        private set
    var databasePresent: DatabaseStatus = DatabaseStatus.DOES_NOT_EXIST
        private set
    lateinit var gameRegistry: GameRegistry
        private set

    val settings: Settings = Settings(this)
    val githubVersion: ObjectProperty<Version> = SimpleObjectProperty(Version.RETRIEVING)

    private val editorPane: EditorPane by lazy { EditorPane(this) }

    override fun init() {
        val currentJson = rhreSfxRoot.resolve("current.json")
        if (rhreSfxRoot.exists() && currentJson.exists())
            databasePresent = DatabaseStatus.EXISTS

        try {
            val root = JsonHandler.OBJECT_MAPPER.readTree(currentJson)
            val verNum = root["v"].asInt(0)
            val editor: String = root["editor"].asText()
            val editorVersion = Version.fromString(editor)

            if (editorVersion.minor > MIN_RHRE_VERSION.minor) {
                databasePresent = DatabaseStatus.INCOMPATIBLE
            } else {
                gameRegistry = GameRegistry(verNum, editorVersion)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            databasePresent = DatabaseStatus.ERROR
        }

        GlobalScope.launch {
            try {
                val apiUrl = URL("https://api.github.com/repos/chrislo27/RSDE/releases/latest")
                val con = apiUrl.openConnection() as HttpURLConnection
                con.requestMethod = "GET"
                val status = con.responseCode
                if (status == 200) {
                    val content = con.inputStream.bufferedReader().let {
                        val text = it.readText()
                        it.close()
                        text
                    }

                    Platform.runLater {
                        githubVersion.set(Version.fromStringOrNull(JsonHandler.OBJECT_MAPPER.readTree(content)["tag_name"].asText()) ?: Version.UNKNOWN)
                        LOGGER.info("Got version from server: ${githubVersion.get()}")
                    }
                }

                con.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                Platform.runLater {
                    githubVersion.set(Version.UNKNOWN)
                }
            }
        }
    }

    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage
        primaryStage.title = "$TITLE $VERSION"
        primaryStage.icons.addAll(windowIcons)

        val scene = Scene(WelcomePane(this)).apply {
            addDebugAccelerators()
            addBaseStyleToScene(this)
        }
        scene.rootProperty().addListener { _, _, newValue ->
            if (newValue is ChangesPresenceState) {
                DiscordHelper.updatePresence(newValue.getPresenceState())
            } else {
                DiscordHelper.updatePresence(DefaultRichPresence())
            }
        }
        primaryStage.scene = scene
        primaryStage.setMinimumBoundsToSized()
        primaryStage.show()

        settings.loadFromStorage()

        DiscordHelper.init(enabled = settings.richPresence)
        DiscordHelper.updatePresence((scene.root as? ChangesPresenceState?)?.getPresenceState() ?: DefaultRichPresence())
        Thread.currentThread().setUncaughtExceptionHandler { t, e ->
            e.printStackTrace()
            Platform.runLater {
                val exitButton = ButtonType(Localization["opts.closeProgram"])
                val buttonType: Optional<ButtonType> = ExceptionAlert(null, e, "An uncaught exception occurred in thread ${t.name}\n${e::class.java.simpleName}", "An uncaught exception occurred").apply {
                    this.buttonTypes += exitButton
                }.showAndWait()
                if (buttonType.isPresent) {
                    if (buttonType.get() == exitButton) {
                        exitProcess(0)
                    }
                }
            }
        }

    }

    /**
     * Adds the base style + night mode listener
     */
    fun addBaseStyleToScene(scene: Scene) {
        scene.stylesheets += "style/main.css"
        val nightStyle = "style/nightMode.css"
        settings.nightModeProperty.addListener { _, _, newValue ->
            if (newValue) {
                if (nightStyle !in scene.stylesheets) scene.stylesheets += nightStyle
            } else {
                scene.stylesheets -= nightStyle
            }
        }
        if (settings.nightMode) scene.stylesheets += nightStyle
    }

    fun addBaseStyleToDialog(dialogPane: DialogPane) {
        dialogPane.stylesheets += "style/main.css"
        val nightStyle = "style/nightMode.css"
        settings.nightModeProperty.addListener { _, _, newValue ->
            if (newValue) {
                if (nightStyle !in dialogPane.stylesheets) dialogPane.stylesheets += nightStyle
            } else {
                dialogPane.stylesheets -= nightStyle
            }
        }
        if (settings.nightMode) dialogPane.stylesheets += nightStyle
    }

    override fun stop() {
        super.stop()
        settings.persistToStorage()
        exitProcess(0)
    }

    fun switchToEditorPane(apply: EditorPane.() -> Unit) {
        val stage = primaryStage
        val scene = stage.scene
        scene.root = object : StackPane(), ChangesPresenceState {
            private val editorPane: EditorPane = this@RSDE.editorPane.apply {
                apply()
            }

            init {
                children += editorPane
            }

            override fun getPresenceState(): DefaultRichPresence {
                return editorPane.getPresenceState()
            }
        }
//        stage.sizeToScene()
    }
}