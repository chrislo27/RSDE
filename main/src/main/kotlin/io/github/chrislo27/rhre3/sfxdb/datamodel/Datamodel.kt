package io.github.chrislo27.rhre3.sfxdb.datamodel

import io.github.chrislo27.rhre3.sfxdb.Game


abstract class Datamodel(val game: Game, val id: String, val deprecatedIDs: List<String>, val name: String)
