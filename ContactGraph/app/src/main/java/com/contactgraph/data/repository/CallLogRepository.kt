package com.contactgraph.data.repository

import android.content.Context
import android.provider.CallLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CallStats(
    val totalCalls: Int,
    val missedCalls: Int,
    val totalDurationSeconds: Long,
    val lastMs: Long,
)

class CallLogRepository(private val context: Context) {

    suspend fun loadStats(maxDays: Int = 180): Map<String, CallStats> =
        withContext(Dispatchers.IO) {

        val cutoff = System.currentTimeMillis() - maxDays * 86_400_000L

        data class Call(val number: String, val type: Int, val duration: Long, val dateMs: Long)

        val calls = mutableListOf<Call>()

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE,
            ),
            "${CallLog.Calls.DATE} >= ?",
            arrayOf(cutoff.toString()),
            "${CallLog.Calls.DATE} DESC"
        )?.use { cursor ->
            val numCol = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val typeCol = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val durCol = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)

            while (cursor.moveToNext()) {
                val number = ContactsRepository.normalizeNumber(
                    cursor.getString(numCol) ?: continue
                )
                val type = cursor.getInt(typeCol)
                val duration = cursor.getLong(durCol)
                val date = cursor.getLong(dateCol)
                calls.add(Call(number, type, duration, date))
            }
        }

        buildMap {
            calls.groupBy { it.number }.forEach { (number, callList) ->
                val total = callList.size
                val missed = callList.count {
                    it.type == CallLog.Calls.MISSED_TYPE ||
                    it.type == CallLog.Calls.REJECTED_TYPE
                }
                val duration = callList.sumOf { it.duration }
                val lastMs = callList.maxOf { it.dateMs }
                put(number, CallStats(total, missed, duration, lastMs))
            }
        }
    }
}
