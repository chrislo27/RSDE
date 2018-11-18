package io.github.chrislo27.rhre3.sfxdb.gui

import io.github.chrislo27.rhre3.sfxdb.gui.discord.DiscordHelper
import io.github.chrislo27.rhre3.sfxdb.gui.discord.PresenceState
import io.github.chrislo27.rhre3.sfxdb.gui.scene.WelcomePane
import io.github.chrislo27.rhre3.sfxdb.gui.util.Version
import io.github.chrislo27.rhre3.sfxdb.gui.util.addDebugAccelerators
import io.github.chrislo27.rhre3.sfxdb.gui.util.setMinimumBoundsToSized
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File


class RSDE : Application() {

    companion object {
        const val TITLE = "RHRE SFX Database Editor"
        val VERSION = Version(1, 0, 0, "DEVELOPMENT")
        val MIN_RHRE_VERSION = Version(3, 16, 0)
        val rootFolder: File = File(System.getProperty("user.home")).resolve(".rsde/").apply { mkdirs() }

        val LOGGER: Logger = LogManager.getContext(RSDE::class.java.classLoader, false).getLogger("RSDE")

        val startTimeMillis: Long = System.currentTimeMillis()

        @JvmStatic
        fun main(args: Array<String>) {
            LOGGER.info("Launching $TITLE $VERSION...")
            Application.launch(RSDE::class.java, *args)
        }
    }

    private lateinit var primaryStage: Stage

    override fun init() {
        DiscordHelper.init(enabled = false)
    }

    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage
        primaryStage.title = "$TITLE $VERSION"
        primaryStage.icons.addAll(Image("icon/16.png"), Image("icon/24.png"), Image("icon/32.png"))

        val welcomeScene = Scene(WelcomePane(this)).apply {
            addDebugAccelerators()
            stylesheets += "style/main.css"
            stylesheets += "style/welcomePane.css"
        }
        primaryStage.scene = welcomeScene
        primaryStage.setMinimumBoundsToSized()
        primaryStage.show()

        DiscordHelper.updatePresence(PresenceState.WelcomeScreen)
    }
}