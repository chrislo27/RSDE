package io.github.chrislo27.rhre3.sfxdb.gui

import io.github.chrislo27.rhre3.sfxdb.gui.util.Version
import javafx.application.Application
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class RSDE : Application() {

    companion object {
        val TITLE = "RHRE SFX Database Editor"
        val VERSION = Version(1, 0, 0, "DEVELOPMENT")
        val MIN_RHRE_VERSION = Version(3, 16, 0)

        val LOGGER: Logger = LogManager.getContext(RSDE::class.java.classLoader, false).getLogger("RSDE")
    }

    private lateinit var primaryStage: Stage

    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage

    }
}