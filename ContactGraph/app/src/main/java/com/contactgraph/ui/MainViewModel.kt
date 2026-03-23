package com.contactgraph.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contactgraph.data.model.ConnectionStats
import com.contactgraph.data.model.ContactNode
import com.contactgraph.data.repository.CallLogRepository
import com.contactgraph.data.repository.ContactsRepository
import com.contactgraph.data.repository.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val contacts: List<ContactNode>) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val contactsRepo = ContactsRepository(app)
    private val smsRepo = SmsRepository(app)
    private val callRepo = CallLogRepository(app)

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun load() {
        if (_state.value is UiState.Loading) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching { buildContactNodes() }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Unknown error") }
        }
    }

    private suspend fun buildContactNodes(): List<ContactNode> {
        val rawContacts = contactsRepo.loadContacts()
        val smsStats = smsRepo.loadStats()
        val callStats = callRepo.loadStats()

        // Build a reverse lookup: normalised phone number -> contact id
        val phoneToContactId = mutableMapOf<String, String>()
        for (c in rawContacts) {
            for (phone in c.phoneNumbers) {
                phoneToContactId[phone] = c.id
            }
        }

        // Accumulate per-contact stats
        val statsByContactId = mutableMapOf<String, ConnectionStats>()

        fun ensureStats(cid: String) = statsByContactId.getOrPut(cid) { ConnectionStats() }

        for ((phone, sms) in smsStats) {
            val cid = resolveContact(phone, phoneToContactId) ?: continue
            val existing = ensureStats(cid)
            statsByContactId[cid] = existing.copy(
                sentSms = existing.sentSms + sms.sent,
                receivedSms = existing.receivedSms + sms.received,
                avgSaliency = mergeAverage(
                    existing.avgSaliency, existing.totalSms,
                    sms.avgSaliency, sms.sent + sms.received
                ),
                avgResponseGapSeconds = if (sms.avgResponseGapSeconds > 0)
                    (existing.avgResponseGapSeconds + sms.avgResponseGapSeconds) / 2
                else existing.avgResponseGapSeconds,
                lastInteractionMs = maxOf(existing.lastInteractionMs, sms.lastMs),
            )
        }

        for ((phone, call) in callStats) {
            val cid = resolveContact(phone, phoneToContactId) ?: continue
            val existing = ensureStats(cid)
            statsByContactId[cid] = existing.copy(
                totalCalls = existing.totalCalls + call.totalCalls,
                totalCallDurationSeconds = existing.totalCallDurationSeconds + call.totalDurationSeconds,
                missedCalls = existing.missedCalls + call.missedCalls,
                lastInteractionMs = maxOf(existing.lastInteractionMs, call.lastMs),
            )
        }

        // Only keep contacts that have at least one interaction
        val nodes = rawContacts
            .mapNotNull { rc ->
                val stats = statsByContactId[rc.id] ?: return@mapNotNull null
                if (stats.totalCalls == 0 && stats.totalSms == 0) return@mapNotNull null
                ContactNode(rc.id, rc.name, rc.phoneNumbers, stats)
            }

        // Normalise composite scores
        val maxRaw = nodes.maxOfOrNull { it.stats.rawScore() }?.takeIf { it > 0f } ?: 1f
        nodes.forEach { it.stats.compositeScore = it.stats.rawScore() / maxRaw }

        return nodes.sortedByDescending { it.connectionStrength }
    }

    private fun resolveContact(
        phone: String,
        map: Map<String, String>
    ): String? {
        if (map.containsKey(phone)) return map[phone]
        // Try suffix matching (last 7 digits) for numbers stored differently
        val suffix = phone.takeLast(7)
        return map.entries.firstOrNull { it.key.takeLast(7) == suffix }?.value
    }

    private fun mergeAverage(
        avgA: Float, countA: Int,
        avgB: Float, countB: Int
    ): Float {
        val total = countA + countB
        if (total == 0) return 0f
        return (avgA * countA + avgB * countB) / total
    }
}
