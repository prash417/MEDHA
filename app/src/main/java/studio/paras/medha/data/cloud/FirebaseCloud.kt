package studio.paras.medha.data.cloud

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import studio.paras.medha.data.prefs.PlayerStore
import studio.paras.medha.domain.model.PlayerProfile

class FirebaseCloud(
    private val playerStore: PlayerStore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "default"),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            runCatching { ensureUser() }
            playerStore.profile.distinctUntilChanged().collect { profile ->
                runCatching { push(profile) }
            }
        }
    }

    private suspend fun ensureUser() {
        if (auth.currentUser != null) return
        auth.signInAnonymously().await()
    }

    private suspend fun push(profile: PlayerProfile) {
        val uid = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()
        val player = hashMapOf(
            "uid" to uid,
            "displayName" to profile.displayName.take(16),
            "avatarId" to profile.avatarId,
            "xp" to profile.xp,
            "coins" to profile.coins,
            "gems" to profile.gems,
            "level" to profile.level,
            "gamesPlayed" to profile.gamesPlayed,
            "bestPrize" to profile.bestPrize,
            "correctAnswers" to profile.correctAnswers,
            "wrongAnswers" to profile.wrongAnswers,
            "dailyStreak" to profile.dailyStreak,
            "wins" to profile.wins,
            "updatedAt" to now,
        )
        db.collection("players").document(uid).set(player, SetOptions.merge()).await()
        // Only players who have finished a game show on the public board.
        if (profile.gamesPlayed <= 0) return
        val board = hashMapOf(
            "uid" to uid,
            "displayName" to profile.displayName.take(16),
            "avatarId" to profile.avatarId,
            "xp" to profile.xp,
            "level" to profile.level,
            "bestPrize" to profile.bestPrize,
            "correctAnswers" to profile.correctAnswers,
            "wins" to profile.wins,
            "updatedAt" to now,
        )
        db.collection("leaderboard").document(uid).set(board).await()
    }

    /** This player's id once signed in, so the board can highlight "you". */
    fun uid(): String? = auth.currentUser?.uid

    /** Top players by [field] ("xp" or "bestPrize"). */
    suspend fun topPlayers(field: String, limit: Long = 50): List<LeaderRow> {
        runCatching { ensureUser() }
        val snap = db.collection("leaderboard")
            .orderBy(field, Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        return snap.documents.mapIndexed { i, doc ->
            LeaderRow(
                rank = i + 1,
                uid = doc.id,
                name = doc.getString("displayName").orEmpty().ifBlank { "Player" },
                avatarId = (doc.getLong("avatarId") ?: 0L).toInt(),
                level = (doc.getLong("level") ?: 1L).toInt(),
                xp = (doc.getLong("xp") ?: 0L).toInt(),
                bestPrize = doc.getLong("bestPrize") ?: 0L,
                wins = (doc.getLong("wins") ?: 0L).toInt(),
            )
        }
    }
}

data class LeaderRow(
    val rank: Int,
    val uid: String,
    val name: String,
    val avatarId: Int,
    val level: Int,
    val xp: Int,
    val bestPrize: Long,
    val wins: Int,
)
