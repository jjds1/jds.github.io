package com.contactgraph.data.repository

import android.content.Context
import android.provider.Telephony
import com.contactgraph.data.analyzer.SaliencyAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SmsStats(
    val sent: Int,
    val received: Int,
    val avgSaliency: Float,
    val avgResponseGapSeconds: Long,
    val lastMs: Long,
)

class SmsRepository(private val context: Context) {

    /**
     * Returns a map of normalised phone number -> SmsStats.
     * Only looks at the last [maxDays] days of messages.
     */
    suspend fun loadStats(maxDays: Int = 180): Map<String, SmsStats> =
        withContext(Dispatchers.IO) {

        val cutoff = System.currentTimeMillis() - maxDays * 86_400_000L

        // phone -> list of Triple(dateMs, type, body)
        val byPhone = mutableMapOf<String, MutableList<Triple<Long, Int, String>>>()

        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.BODY,
            ),
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(cutoff.toString()),
            "${Telephony.Sms.DATE} ASC"
        )?.use { cursor ->
            val addrCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeCol = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)

            while (cursor.moveToNext()) {
                val addr = ContactsRepository.normalizeNumber(
                    cursor.getString(addrCol) ?: continue
                )
                val date = cursor.getLong(dateCol)
                val type = cursor.getInt(typeCol)
                val body = cursor.getString(bodyCol) ?: ""
                byPhone.getOrPut(addr) { mutableListOf() }
                    .add(Triple(date, type, body))
            }
        }

        buildMap {
            for ((phone, msgs) in byPhone) {
                val sent = msgs.count { it.second == Telephony.Sms.MESSAGE_TYPE_SENT }
                val received = msgs.count { it.second == Telephony.Sms.MESSAGE_TYPE_INBOX }

                val receivedBodies = msgs
                    .filter { it.second == Telephony.Sms.MESSAGE_TYPE_INBOX }
                    .map { it.third }
                val avgSaliency = SaliencyAnalyzer.averageScore(receivedBodies)

                val avgGap = computeAvgResponseGap(msgs)
                val lastMs = msgs.maxOf { it.first }

                put(phone, SmsStats(sent, received, avgSaliency, avgGap, lastMs))
            }
        }
    }

    private fun computeAvgResponseGap(msgs: List<Triple<Long, Int, String>>): Long {
        // msgs is sorted by date ascending; find gaps between received -> next sent
        val gaps = mutableListOf<Long>()
        var lastReceivedMs = -1L
        for ((dateMs, type, _) in msgs) {
            if (type == Telephony.Sms.MESSAGE_TYPE_INBOX) {
                lastReceivedMs = dateMs
            } else if (type == Telephony.Sms.MESSAGE_TYPE_SENT && lastReceivedMs > 0) {
                gaps.add(((dateMs - lastReceivedMs) / 1000L).coerceAtLeast(0))
                lastReceivedMs = -1L
            }
        }
        return if (gaps.isEmpty()) 0L else gaps.average().toLong()
    }
}
