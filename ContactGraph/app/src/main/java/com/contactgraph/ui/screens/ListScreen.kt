package com.contactgraph.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contactgraph.data.model.ContactNode

private val COLD = Color(0xFF4FC3F7)
private val HOT  = Color(0xFFFF6F00)

@Composable
fun ListScreen(contacts: List<ContactNode>) {
    if (contacts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No interactions found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Your Connections",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                "Ranked by interaction strength • last 6 months",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        itemsIndexed(contacts) { index, contact ->
            ContactCard(rank = index + 1, contact = contact)
        }
    }
}

@Composable
private fun ContactCard(rank: Int, contact: ContactNode) {
    val stats = contact.stats
    val color = lerp(COLD, HOT, contact.connectionStrength)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "#$rank",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(contact.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        "Score: %.0f%%".format(contact.connectionStrength * 100),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Strength bar
            StrengthBar(
                value = contact.connectionStrength,
                color = color,
                label = "Overall connection",
            )

            Spacer(Modifier.height(8.dp))

            // Sub-bars
            val maxCalls = 3600f // normalise call duration to 1h cap
            val callNorm = (stats.totalCallDurationSeconds / maxCalls).coerceIn(0f, 1f)
            val smsNorm = (stats.totalSms / 200f).coerceIn(0f, 1f)

            StrengthBar(callNorm, Color(0xFF66BB6A), "Calls (${formatDuration(stats.totalCallDurationSeconds)})")
            Spacer(Modifier.height(4.dp))
            StrengthBar(smsNorm, Color(0xFF42A5F5), "SMS (${stats.totalSms} messages)")
            Spacer(Modifier.height(4.dp))
            StrengthBar(stats.avgSaliency, Color(0xFFAB47BC), "Message saliency")

            // Details row
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("${stats.totalCalls} calls", Color(0xFF66BB6A))
                Chip("${stats.receivedSms} received SMS", Color(0xFF42A5F5))
                if (stats.missedCalls > 0) Chip("${stats.missedCalls} missed", Color(0xFFEF5350))
            }
        }
    }
}

@Composable
private fun StrengthBar(value: Float, color: Color, label: String) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 600),
        label = "bar_$label",
    )
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("%.0f%%".format(value * 100), style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(2.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedValue)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
