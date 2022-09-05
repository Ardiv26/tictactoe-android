/*
 *     Dooz
 *     GameState.kt Created by Yamin Siahmargooei at 2022/8/26
 *     This file is part of Dooz.
 *     Copyright (C) 2022  Yamin Siahmargooei
 *
 *     Dooz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Dooz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Dooz.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.yamin8000.dooz.ui.game

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import io.github.yamin8000.dooz.game.GameConstants.gameDefaultSize
import io.github.yamin8000.dooz.game.GamePlayersType
import io.github.yamin8000.dooz.game.GameType
import io.github.yamin8000.dooz.game.logic.GameLogic
import io.github.yamin8000.dooz.game.logic.SimpleGameLogic
import io.github.yamin8000.dooz.model.DoozCell
import io.github.yamin8000.dooz.model.Player

class GameState(
    var gameCells: MutableState<List<List<DoozCell>>>,
    val gameSize: MutableState<Int>,
    var currentPlayer: MutableState<Player?>,
    var players: MutableState<List<Player>>,
    var gamePlayersType: MutableState<GamePlayersType>,
    var isGameStarted: MutableState<Boolean>,
    var isGameFinished: MutableState<Boolean>,
    var winner: MutableState<Player?>,
    var gameType: MutableState<GameType>
) {
    private var gameLogic: GameLogic? = null

    init {
        newGame()
    }

    fun startGame() {
        newGame()
        isGameStarted.value = true
    }

    fun playItemByUser(
        cell: DoozCell
    ) {
        checkIfGameIsFinished()
        changeCellOwner(cell)
        checkIfGameIsFinished()
    }

    private fun newGame() {
        resetGame()
        preparePlayers()
        prepareGameLogic()
    }

    private fun prepareGameLogic() {
        when (gameType.value) {
            GameType.Simple -> gameLogic = SimpleGameLogic(gameCells.value, gameSize.value)
        }
    }

    private fun resetGame() {
        winner.value = null
        isGameFinished.value = false
        isGameStarted.value = false
        gameCells.value = getEmptyBoard()
    }

    private fun getEmptyBoard(): List<List<DoozCell>> {
        val columns = mutableListOf<List<DoozCell>>()
        for (x in 0 until gameSize.value) {
            val row = mutableListOf<DoozCell>()
            for (y in 0 until gameSize.value)
                row.add(DoozCell(x, y))
            columns.add(row)
        }
        return columns
    }

    private fun preparePlayers() {
        if (players.value.isEmpty())
            players.value = getDefaultPlayers()
        currentPlayer.value = players.value.first()
    }

    private fun getDefaultPlayers(): List<Player> {
        return listOf(Player("Player 1"), Player("Player 2"))
    }

    fun getOwnerShape(owner: Player): Shape {
        return if (owner == players.value.first()) CircleShape else RectangleShape
    }

    private fun changePlayer() {
        if (currentPlayer.value == players.value.first()) currentPlayer.value = players.value.last()
        else currentPlayer.value = players.value.first()
    }

    private fun changeCellOwner(
        cell: DoozCell
    ) {
        if (cell.owner == null && isGameStarted.value) {
            cell.owner = currentPlayer.value
            changePlayer()
        }
    }

    private fun checkIfGameIsFinished() {
        winner.value = findWinner()
        if (winner.value != null)
            finishGame()
    }

    private fun finishGame() {
        isGameFinished.value = true
        isGameStarted.value = false
    }

    private fun findWinner(): Player? {
        return when (gameType.value) {
            GameType.Simple -> gameLogic?.findWinner()
        }
    }
}

@Composable
fun rememberHomeState(
    doozCells: MutableState<List<List<DoozCell>>> = rememberSaveable { mutableStateOf(emptyList()) },
    gameSize: MutableState<Int> = rememberSaveable { mutableStateOf(gameDefaultSize) },
    currentPlayer: MutableState<Player?> = rememberSaveable { mutableStateOf(null) },
    playerPair: MutableState<List<Player>> = rememberSaveable { mutableStateOf(listOf()) },
    gamePlayersType: MutableState<GamePlayersType> = rememberSaveable {
        mutableStateOf(
            GamePlayersType.PvP
        )
    },
    isGameStarted: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    isGameFinished: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    winner: MutableState<Player?> = rememberSaveable { mutableStateOf(null) },
    gameType: MutableState<GameType> = rememberSaveable { mutableStateOf(GameType.Simple) }
) = remember(
    doozCells,
    gameSize,
    currentPlayer,
    playerPair,
    gamePlayersType,
    isGameStarted,
    isGameFinished,
    winner,
    gameType
) {
    GameState(
        doozCells,
        gameSize,
        currentPlayer,
        playerPair,
        gamePlayersType,
        isGameStarted,
        isGameFinished,
        winner,
        gameType
    )
}