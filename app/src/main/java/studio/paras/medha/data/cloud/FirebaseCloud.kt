package studio.paras.medha.data.cloud

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        val payload = hashMapOf(
            "uid" to uid,
            "displayName" to profile.displayName,
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
            "updatedAt" to System.currentTimeMillis(),
        )
        db.collection("players").document(uid).set(payload, SetOptions.merge()).await()
        db.collection("leaderboard").document(uid).set(payload, SetOptions.merge()).await()
    }
}
