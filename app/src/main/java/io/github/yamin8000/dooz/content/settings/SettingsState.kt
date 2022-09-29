/*
 *     Dooz
 *     SettingsState.kt Created/Updated by Yamin Siahmargooei at 2022/9/6
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

package io.github.yamin8000.dooz.content.settings

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import io.github.yamin8000.dooz.R
import io.github.yamin8000.dooz.content.settings
import io.github.yamin8000.dooz.game.FirstPlayerPolicy
import io.github.yamin8000.dooz.game.GameConstants
import io.github.yamin8000.dooz.model.AiDifficulty
import io.github.yamin8000.dooz.model.GamePlayersType
import io.github.yamin8000.dooz.util.Constants
import io.github.yamin8000.dooz.util.DataStoreHelper
import kotlinx.coroutines.launch

class SettingsState(
    private val context: Context,
    private val scope: LifecycleCoroutineScope,
    var gamePlayersType: MutableState<GamePlayersType>,
    var gameSize: MutableState<Int>,
    var firstPlayerName: MutableState<String>,
    var secondPlayerName: MutableState<String>,
    var firstPlayerShape: MutableState<String>,
    var secondPlayerShape: MutableState<String>,
    var aiDifficulty: MutableState<AiDifficulty>,
    var themeSetting: MutableState<ThemeSetting>,
    var firstPlayerPolicy: MutableState<FirstPlayerPolicy>,
) {
    val snackbarHostState: SnackbarHostState = SnackbarHostState()

    private val dataStore = DataStoreHelper(context.settings)

    init {
        scope.launch {
            themeSetting.value = ThemeSetting.valueOf(
                dataStore.getString(Constants.theme) ?: ThemeSetting.System.name
            )
            gamePlayersType.value = GamePlayersType.valueOf(
                dataStore.getString(Constants.gamePlayersType) ?: GamePlayersType.PvC.name
            )
            gameSize.value = dataStore.getInt(Constants.gameSize) ?: GameConstants.gameDefaultSize
            firstPlayerName.value = dataStore.getString(Constants.firstPlayerName) ?: "Player 1"
            secondPlayerName.value = dataStore.getString(Constants.secondPlayerName) ?: "Player 2"
            firstPlayerShape.value =
                dataStore.getString(Constants.firstPlayerShape) ?: Constants.Shapes.xShape
            secondPlayerShape.value =
                dataStore.getString(Constants.secondPlayerShape) ?: Constants.Shapes.ringShape
            aiDifficulty.value = AiDifficulty.valueOf(
                dataStore.getString(Constants.aiDifficulty) ?: AiDifficulty.Easy.name
            )
            firstPlayerPolicy.value = FirstPlayerPolicy.valueOf(
                dataStore.getString(Constants.firstPlayerPolicy)
                    ?: FirstPlayerPolicy.DiceRolling.name
            )
        }
    }

    fun setFirstPlayerPolicy(
        firstPlayerPolicy: FirstPlayerPolicy
    ) {
        this.firstPlayerPolicy.value = firstPlayerPolicy
        scope.launch { dataStore.setString(Constants.firstPlayerPolicy, firstPlayerPolicy.name) }
    }

    fun setThemeSetting(
        newTheme: ThemeSetting
    ) {
        themeSetting.value = newTheme
        scope.launch { dataStore.setString(Constants.theme, newTheme.name) }
    }

    fun setPlayersType(
        gamePlayersType: GamePlayersType
    ) {
        this.gamePlayersType.value = gamePlayersType
        scope.launch { dataStore.setString(Constants.gamePlayersType, gamePlayersType.name) }
    }

    fun increaseGameSize() {
        if (gameSize.value < GameConstants.gameMaxSize) {
            gameSize.value = gameSize.value + 1
            setGameSize()
        }
    }

    fun decreaseGameSize() {
        if (gameSize.value > GameConstants.gameDefaultSize) {
            gameSize.value = gameSize.value - 1
            setGameSize()
        }
    }

    fun savePlayerInfo() {
        val isAnyNameEmpty =
            firstPlayerName.value.trim().isEmpty() || secondPlayerName.value.trim().isEmpty()

        var errorText: String? = null
        if (firstPlayerShape.value == secondPlayerShape.value) {
            errorText = context.getString(R.string.player_shapes_equal)
        } else if (firstPlayerName.value == secondPlayerName.value) {
            errorText = context.getString(R.string.player_names_equal)
        } else if (isAnyNameEmpty) {
            errorText = context.getString(R.string.player_names_empty)
        } else {
            scope.launch {
                context.settings.edit {
                    it[stringPreferencesKey(Constants.firstPlayerName)] =
                        firstPlayerName.value.trim()
                    it[stringPreferencesKey(Constants.secondPlayerName)] =
                        secondPlayerName.value.trim()
                    it[stringPreferencesKey(Constants.firstPlayerShape)] = firstPlayerShape.value
                    it[stringPreferencesKey(Constants.secondPlayerShape)] = secondPlayerShape.value
                }
                snackbarHostState.showSnackbar(context.getString(R.string.data_saved))
            }
        }
        if (errorText != null)
            scope.launch { snackbarHostState.showSnackbar(errorText) }
    }

    private fun setGameSize() {
        scope.launch { dataStore.setInt(Constants.gameSize, gameSize.value) }
    }

    fun setAiDifficulty(
        aiDifficulty: AiDifficulty
    ) {
        this.aiDifficulty.value = aiDifficulty
        scope.launch { dataStore.setString(Constants.aiDifficulty, aiDifficulty.name) }
    }
}

@Composable
fun rememberSettingsState(
    context: Context = LocalContext.current,
    coroutineScope: LifecycleCoroutineScope = LocalLifecycleOwner.current.lifecycleScope,
    gamePlayersType: MutableState<GamePlayersType> = rememberSaveable {
        mutableStateOf(
            GamePlayersType.PvC
        )
    },
    gameSize: MutableState<Int> = rememberSaveable { mutableStateOf(GameConstants.gameDefaultSize) },
    firstPlayerName: MutableState<String> = rememberSaveable { mutableStateOf("Player 1") },
    secondPlayerName: MutableState<String> = rememberSaveable { mutableStateOf("Player 2") },
    firstPlayerShape: MutableState<String> = rememberSaveable { mutableStateOf(Constants.Shapes.xShape) },
    secondPlayerShape: MutableState<String> = rememberSaveable { mutableStateOf(Constants.Shapes.ringShape) },
    aiDifficulty: MutableState<AiDifficulty> = rememberSaveable { mutableStateOf(AiDifficulty.Easy) },
    themeSetting: MutableState<ThemeSetting> = rememberSaveable { mutableStateOf(ThemeSetting.System) },
    firstPlayerPolicy: MutableState<FirstPlayerPolicy> = rememberSaveable {
        mutableStateOf(
            FirstPlayerPolicy.DiceRolling
        )
    }
) = remember(
    context,
    coroutineScope,
    gamePlayersType,
    gameSize,
    firstPlayerName,
    secondPlayerName,
    firstPlayerShape,
    secondPlayerShape,
    aiDifficulty,
    themeSetting,
    firstPlayerPolicy
) {
    SettingsState(
        context,
        coroutineScope,
        gamePlayersType,
        gameSize,
        firstPlayerName,
        secondPlayerName,
        firstPlayerShape,
        secondPlayerShape,
        aiDifficulty,
        themeSetting,
        firstPlayerPolicy
    )
}