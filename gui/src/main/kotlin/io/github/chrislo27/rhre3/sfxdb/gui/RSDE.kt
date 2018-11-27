package io.github.chrislo27.rhre3.sfxdb.gui

import io.github.chrislo27.rhre3.sfxdb.gui.discord.ChangesPresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DefaultRichPresence
import io.github.chrislo27.rhre3.sfxdb.gui.discord.DiscordHelper
import io.github.chrislo27.rhre3.sfxdb.gui.registry.GameRegistry
import io.github.chrislo27.rhre3.sfxdb.gui.scene.WelcomePane
import io.github.chrislo27.rhre3.sfxdb.gui.util.JsonHandler
import io.github.chrislo27.rhre3.sfxdb.gui.util.Version
import io.github.chrislo27.rhre3.sfxdb.gui.util.addDebugAccelerators
import io.github.chrislo27.rhre3.sfxdb.gui.util.setMinimumBoundsToSized
import javafx.animation.Interpolator
import javafx.animation.RotateTransition
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.effect.Bloom
import javafx.scene.effect.Glow
import javafx.scene.image.Image
import javafx.scene.layout.StackPane
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.stage.Stage
import javafx.util.Duration
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.system.exitProcess


class RSDE : Application() {

    companion object {
        const val TITLE = "RHRE SFX Database Editor"
        val VERSION = Version(1, 0, 0, "DEVELOPMENT")
        val MIN_RHRE_VERSION = Version(3, 15, 0)
        val rootFolder: File = File(System.getProperty("user.home")).resolve(".rsde/").apply { mkdirs() }
        val rhreRoot: File = File(System.getProperty("user.home")).resolve(".rhre3/")
        val customSFXFolder: File = rhreRoot.resolve("customSounds/")
        const val SFX_DB_BRANCH = "prototype"
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

        val stackPane = StackPane(WelcomePane(this))
        val scene = Scene(stackPane).apply {
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

        val region = StackPane().apply {
            children += Circle(scene.width / 2, scene.height / 2, Math.max(scene.width, scene.height)).apply {
                this.style = "-fx-fill: rgba(0, 0, 0, 0.8)"
            }
            effect = Bloom(0.0).apply {
                input = Glow(0.8)
            }
        }
        stackPane.children += region

        val lineCount = 30
        (0 until lineCount).forEach { i ->
            val x = scene.width / 2
            val y = scene.height / 2
            val smallerAxis = Math.min(scene.width, scene.height)
            val line = Line(x, y, x + Math.cos(i.toFloat() / lineCount * 2 * Math.PI) * smallerAxis * 2, y + Math.sin(i.toFloat() / lineCount * 2 * Math.PI) * smallerAxis * 2)
            line.strokeWidth = 5.0
            line.style = "-fx-stroke: radial-gradient(center 50% 50%, radius 75%, repeat, red, orange, yellow, green, blue, indigo, darkviolet);"

            region.children += line
        }

        val rt = RotateTransition(Duration.millis(5000.0), region).apply {
            cycleCount = -1
            byAngle = 360.0
            this.interpolator = Interpolator.LINEAR
        }
        rt.play()

        DiscordHelper.updatePresence((scene.root as? ChangesPresenceState?)?.getPresenceState() ?: DefaultRichPresence())
    }

    override fun stop() {
        super.stop()
        exitProcess(0)
    }
}