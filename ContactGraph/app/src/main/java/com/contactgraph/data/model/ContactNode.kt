package com.contactgraph.data.model

/**
 * Represents a contact with all aggregated communication stats.
 */
data class ContactNode(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>,
    val stats: ConnectionStats,
) {
    /** Composite score 0..1 used for node sizing and ranking. */
    val connectionStrength: Float get() = stats.compositeScore
}

data class ConnectionStats(
    // Calls
    val totalCalls: Int = 0,
    val totalCallDurationSeconds: Long = 0L,
    val missedCalls: Int = 0,

    // SMS
    val sentSms: Int = 0,
    val receivedSms: Int = 0,
    val avgSaliency: Float = 0f,       // 0..1 from heuristics
    val avgResponseGapSeconds: Long = 0L,

    // Recency: epoch millis of last interaction
    val lastInteractionMs: Long = 0L,
) {
    val totalSms: Int get() = sentSms + receivedSms

    /**
     * Composite score weighted across all dimensions, normalised to 0..1
     * relative to the maximum possible raw value (called at ranking time).
     */
    fun rawScore(): Float {
        val callScore = totalCallDurationSeconds.toFloat() + totalCalls * 30f
        val smsScore = totalSms * 10f * (1f + avgSaliency)
        val recencyBoost = recencyFactor()
        return (callScore + smsScore) * recencyBoost
    }

    /** Decays linearly from 1.0 (today) to 0.3 (>180 days ago). */
    private fun recencyFactor(): Float {
        if (lastInteractionMs == 0L) return 0.3f
        val daysSince = (System.currentTimeMillis() - lastInteractionMs) / 86_400_000L
        return (1.0f - (daysSince.coerceAtMost(180) / 180f) * 0.7f)
    }

    /** Normalised 0..1 score, set after all contacts are loaded. */
    var compositeScore: Float = 0f
}
