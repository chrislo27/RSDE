package io.github.chrislo27.rhre3.sfxdb.gui.discord

import club.minnced.discord.rpc.DiscordRichPresence
import io.github.chrislo27.rhre3.sfxdb.gui.RSDE


class DefaultRichPresence(state: String = "",
                          party: Pair<Int, Int> = DEFAULT_PARTY,
                          smallIcon: String = "",
                          smallIconText: String = state)
    : DiscordRichPresence() {

    companion object {
        val DEFAULT_PARTY: Pair<Int, Int> = 0 to 0
    }

    constructor(presenceState: PresenceState)
            : this(presenceState.state, presenceState.getPartyCount(), presenceState.smallIcon, presenceState.smallIconText) {
        presenceState.modifyRichPresence(this)
    }

    init {
        details = if (RSDE.VERSION.suffix.startsWith("DEV")) {
            "Developing ${RSDE.VERSION.copy(suffix = "")}"
        } else if (RSDE.VERSION.suffix.startsWith("RC") || RSDE.VERSION.suffix.startsWith("SNAPSHOT")) {
            "Testing ${RSDE.VERSION}"
        } else {
            "Using ${RSDE.VERSION}"
        }
        startTimestamp = RSDE.startTimeMillis / 1000L // Epoch seconds
        largeImageKey = DiscordHelper.DEFAULT_LARGE_IMAGE
        largeImageText = "RSDE is the SFX Database editor for the Rhythm Heaven Remix Editor"
        smallImageKey = smallIcon
        smallImageText = smallIconText
        this.state = state
        if (party.first > 0 && party.second > 0) {
            partySize = party.first
            partyMax = party.second
        }
    }

}