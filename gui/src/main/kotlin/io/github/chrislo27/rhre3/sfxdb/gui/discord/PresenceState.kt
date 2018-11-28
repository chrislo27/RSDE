package io.github.chrislo27.rhre3.sfxdb.gui.discord

import kotlin.math.roundToLong


sealed class PresenceState(open val state: String = "", open val smallIcon: String = "", open val smallIconText: String = state) {

    open fun getPartyCount(): Pair<Int, Int> = DefaultRichPresence.DEFAULT_PARTY

    open fun modifyRichPresence(richPresence: DefaultRichPresence) {
    }

    fun toRichPresenceObj(): DefaultRichPresence = DefaultRichPresence(this)

    // ---------------- IMPLEMENTATIONS BELOW ----------------

    object WelcomeScreen
        : PresenceState("On Welcome Screen")

    object PreparingNewDef
        : PresenceState("Preparing a New Game Definition")

    class InEditor(val current: String?)
        : PresenceState("Editing ${current ?: "nothing"}")

    sealed class Elapsable(state: String, val duration: Float, smallIcon: String = "", smallIconText: String = state)
        : PresenceState(state, smallIcon, smallIconText) {
        override fun modifyRichPresence(richPresence: DefaultRichPresence) {
            super.modifyRichPresence(richPresence)
            if (duration > 0f) {
                richPresence.endTimestamp = System.currentTimeMillis() / 1000L + duration.roundToLong()
            }
        }
    }

}
