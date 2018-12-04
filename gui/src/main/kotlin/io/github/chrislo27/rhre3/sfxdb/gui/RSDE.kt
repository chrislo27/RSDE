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
import javafx.scene.Scene
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.*
import kotlin.system.exitProcess


class RSDE : Application() {

    companion object {
        const val TITLE = "RHRE SFX Database Editor"
        val VERSION = Version(0, 1, 0, "alpha.DEVELOPMENT")
        val MIN_RHRE_VERSION = Version(3, 15, 0)
        val rootFolder: File = File(System.getProperty("user.home")).resolve(".rsde/").apply { mkdirs() }
        val rhreRoot: File = File(System.getProperty("user.home")).resolve(".rhre3/")
        val customSFXFolder: File = rhreRoot.resolve("customSounds/")
        val SFX_DB_BRANCH = "master"
        const val GITHUB = "https://github.com/chrislo27/RSDE"
        const val RHRE_GITHUB = "https://github.com/chrislo27/RhythmHeavenRemixEditor"
        val rhreSfxRoot: File = rhreRoot.resolve("sfx/$SFX_DB_BRANCH/")

        val LOGGER: Logger = LogManager.getContext(RSDE::class.java.classLoader, false).getLogger("RSDE")
        val startTimeMillis: Long = System.currentTimeMillis()

        val windowIcons: List<Image> by lazy { listOf(Image("icon/16.png"), Image("icon/24.png"), Image("icon/32.png")) }

        @JvmStatic
        fun main(args: Array<String>) {
            LOGGER.info("Launching $TITLE $VERSION...")
            Application.launch(RSDE::class.java, *args)
        }
    }

    lateinit var primaryStage: Stage
        private set
    var databasePresent: DatabaseStatus = DatabaseStatus.DOES_NOT_EXIST
        private set
    lateinit var gameRegistry: GameRegistry
        private set

    private val editorPane: EditorPane by lazy { EditorPane(this) }

    override fun init() {
        DiscordHelper.init(enabled = true)

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
                gameRegistry = GameRegistry(verNum)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            databasePresent = DatabaseStatus.ERROR
        }
    }

    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage
        primaryStage.title = "$TITLE $VERSION"
        primaryStage.icons.addAll(windowIcons)

        val scene = Scene(WelcomePane(this)).apply {
            addDebugAccelerators()
            stylesheets += "style/main.css"
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

        DiscordHelper.updatePresence((scene.root as? ChangesPresenceState?)?.getPresenceState() ?: DefaultRichPresence())
        Thread.currentThread().setUncaughtExceptionHandler { t, e ->
            e.printStackTrace()
            Platform.runLater {
                val exitButton = ButtonType(Localization["opts.closeProgram"])
                val buttonType: Optional<ButtonType> = ExceptionAlert(e, "An uncaught exception occurred in thread ${t.name}", "An uncaught exception occurred").apply {
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

    override fun stop() {
        super.stop()
        exitProcess(0)
    }

    fun switchToEditorPane(apply: EditorPane.() -> Unit) {
        val stage = primaryStage
        stage.scene.root = editorPane.apply {
            apply()
        }
//        stage.sizeToScene()
    }
}