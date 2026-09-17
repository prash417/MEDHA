package studio.paras.medha.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import studio.paras.medha.data.repository.PlayerRepository
import studio.paras.medha.data.repository.withResult
import studio.paras.medha.domain.model.AppLanguage
import studio.paras.medha.domain.model.GameResult
import studio.paras.medha.domain.model.PlayerProfile

private val Context.dataStore by preferencesDataStore("medha_player")

class PlayerStore(private val context: Context) : PlayerRepository {

    private object Keys {
        val name = stringPreferencesKey("name")
        val avatar = intPreferencesKey("avatar")
        val language = stringPreferencesKey("language")
        val onboarded = booleanPreferencesKey("onboarded")
        val music = booleanPreferencesKey("music")
        val sfx = booleanPreferencesKey("sfx")
        val voice = booleanPreferencesKey("voice")
        val xp = intPreferencesKey("xp")
        val coins = intPreferencesKey("coins")
        val gems = intPreferencesKey("gems")
        val level = intPreferencesKey("level")
        val games = intPreferencesKey("games")
        val best = longPreferencesKey("best")
        val correct = intPreferencesKey("correct")
        val wrong = intPreferencesKey("wrong")
        val streak = intPreferencesKey("streak")
        val lastDay = longPreferencesKey("last_day")
        val seen = stringPreferencesKey("seen")
    }

    override val profile: Flow<PlayerProfile> = context.dataStore.data.map { it.toProfile() }

    override suspend fun current(): PlayerProfile = context.dataStore.data.first().toProfile()

    override suspend fun update(transform: (PlayerProfile) -> PlayerProfile) {
        context.dataStore.edit { prefs ->
            val next = transform(prefs.toProfile())
            prefs.write(next)
        }
    }

    override suspend fun applyGameResult(result: GameResult) {
        update { it.withResult(result).let { profile ->
            val seen = (profile.seenQuestionIds).distinct().takeLast(180)
            profile.copy(seenQuestionIds = seen)
        } }
    }

    fun rememberQuestions(ids: List<String>) {
        // applied with update from the game session
    }

    private fun Preferences.toProfile(): PlayerProfile {
        val lang = AppLanguage.entries.find { it.code == this[Keys.language] } ?: AppLanguage.ENGLISH
        val seen = this[Keys.seen].orEmpty().split(',').filter { it.isNotBlank() }
        return PlayerProfile(
            displayName = this[Keys.name].orEmpty(),
            avatarId = this[Keys.avatar] ?: 0,
            language = lang,
            onboardingComplete = this[Keys.onboarded] ?: false,
            musicEnabled = this[Keys.music] ?: true,
            sfxEnabled = this[Keys.sfx] ?: true,
            voiceEnabled = this[Keys.voice] ?: false,
            xp = this[Keys.xp] ?: 0,
            coins = this[Keys.coins] ?: 0,
            gems = this[Keys.gems] ?: 0,
            level = this[Keys.level] ?: 1,
            gamesPlayed = this[Keys.games] ?: 0,
            bestPrize = this[Keys.best] ?: 0L,
            correctAnswers = this[Keys.correct] ?: 0,
            wrongAnswers = this[Keys.wrong] ?: 0,
            dailyStreak = this[Keys.streak] ?: 0,
            lastPlayEpochDay = this[Keys.lastDay] ?: 0L,
            seenQuestionIds = seen,
        )
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.write(p: PlayerProfile) {
        this[Keys.name] = p.displayName
        this[Keys.avatar] = p.avatarId
        this[Keys.language] = p.language.code
        this[Keys.onboarded] = p.onboardingComplete
        this[Keys.music] = p.musicEnabled
        this[Keys.sfx] = p.sfxEnabled
        this[Keys.voice] = p.voiceEnabled
        this[Keys.xp] = p.xp
        this[Keys.coins] = p.coins
        this[Keys.gems] = p.gems
        this[Keys.level] = p.level
        this[Keys.games] = p.gamesPlayed
        this[Keys.best] = p.bestPrize
        this[Keys.correct] = p.correctAnswers
        this[Keys.wrong] = p.wrongAnswers
        this[Keys.streak] = p.dailyStreak
        this[Keys.lastDay] = p.lastPlayEpochDay
        this[Keys.seen] = p.seenQuestionIds.joinToString(",")
    }
}
