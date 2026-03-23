package com.contactgraph.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contactgraph.data.model.ContactNode
import kotlin.math.*

// ---------------------------------------------------------------------------
// Layout: force-directed spring simulation
// ---------------------------------------------------------------------------

private data class NodeState(
    val contact: ContactNode,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
)

private fun initLayout(
    contacts: List<ContactNode>,
    width: Float,
    height: Float,
): List<NodeState> {
    val cx = width / 2f
    val cy = height / 2f
    val radius = min(width, height) * 0.35f
    return contacts.mapIndexed { i, c ->
        val angle = 2 * PI.toFloat() * i / contacts.size
        NodeState(
            contact = c,
            x = cx + cos(angle) * radius * (0.4f + 0.6f * c.connectionStrength),
            y = cy + sin(angle) * radius * (0.4f + 0.6f * c.connectionStrength),
        )
    }
}

/** One iteration of the force-directed layout (simplified Barnes-Hut). */
private fun simulateStep(nodes: List<NodeState>, cx: Float, cy: Float) {
    val repulsion = 4000f
    val centerPull = 0.005f
    val damping = 0.85f

    for (i in nodes.indices) {
        val a = nodes[i]
        var fx = 0f; var fy = 0f

        // Repulsion from other nodes
        for (j in nodes.indices) {
            if (i == j) continue
            val b = nodes[j]
            val dx = a.x - b.x
            val dy = a.y - b.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val force = repulsion / (dist * dist)
            fx += dx / dist * force
            fy += dy / dist * force
        }

        // Weak pull toward center
        fx += (cx - a.x) * centerPull
        fy += (cy - a.y) * centerPull

        a.vx = (a.vx + fx) * damping
        a.vy = (a.vy + fy) * damping
        a.x += a.vx
        a.y += a.vy
    }
}

// ---------------------------------------------------------------------------
// Colour helpers
// ---------------------------------------------------------------------------

private val COLD = Color(0xFF4FC3F7)   // light blue = weak connection
private val HOT  = Color(0xFFFF6F00)   // deep orange = strong connection
private val CENTER_COLOR = Color(0xFF1A237E) // you (the user node)

private fun strengthColor(s: Float) = lerp(COLD, HOT, s)

// ---------------------------------------------------------------------------
// Composable
// ---------------------------------------------------------------------------

@Composable
fun GraphScreen(contacts: List<ContactNode>) {
    if (contacts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No interactions found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    // Limit to top 50 for readability
    val displayContacts = remember(contacts) { contacts.take(50) }

    var canvasWidth by remember { mutableFloatStateOf(1f) }
    var canvasHeight by remember { mutableFloatStateOf(1f) }

    val nodes = remember(displayContacts, canvasWidth, canvasHeight) {
        initLayout(displayContacts, canvasWidth, canvasHeight).also { nodeList ->
            repeat(120) { simulateStep(nodeList, canvasWidth / 2f, canvasHeight / 2f) }
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNode by remember { mutableStateOf<ContactNode?>(null) }

    // Animate nodes appearing
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(nodes) { alpha.animateTo(1f, tween(600)) }

    val textMeasurer = rememberTextMeasurer()

    Box(Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.3f, 5f)
                        offset += pan
                    }
                }
                .pointerInput(nodes) {
                    detectTapGestures { tapOffset ->
                        val worldTap = (tapOffset - offset) / scale
                        selectedNode = nodes.minByOrNull { node ->
                            val dx = node.x - worldTap.x
                            val dy = node.y - worldTap.y
                            dx * dx + dy * dy
                        }?.contact
                    }
                }
        ) {
            canvasWidth = size.width
            canvasHeight = size.height

            val cx = canvasWidth / 2f
            val cy = canvasHeight / 2f

            // Draw edges first
            for (node in nodes) {
                drawEdge(
                    from = Offset(cx, cy),
                    to = Offset(node.x, node.y).applyTransform(offset, scale, cx, cy),
                    strength = node.contact.connectionStrength,
                    alpha = alpha.value,
                )
            }

            // Draw contact nodes
            for (node in nodes) {
                val pos = Offset(node.x, node.y).applyTransform(offset, scale, cx, cy)
                val radius = (8f + 28f * node.contact.connectionStrength) * scale
                val color = strengthColor(node.contact.connectionStrength)
                val isSelected = selectedNode?.id == node.contact.id

                drawCircle(color.copy(alpha = alpha.value * 0.25f), radius + 6f * scale, pos)
                drawCircle(color.copy(alpha = alpha.value), radius, pos)
                if (isSelected) {
                    drawCircle(Color.White, radius, pos, style = Stroke(width = 3f * scale))
                }

                // Label for top contacts or selected
                if (node.contact.connectionStrength > 0.5f || isSelected) {
                    drawLabel(textMeasurer, node.contact.name, pos, radius, alpha.value)
                }
            }

            // Draw center "You" node
            val center = Offset(cx, cy).applyTransform(offset, scale, cx, cy)
            drawCircle(CENTER_COLOR.copy(alpha = alpha.value), 20f * scale, center)
            drawLabel(textMeasurer, "You", center, 20f * scale, alpha.value)
        }

        // Selected contact info card
        selectedNode?.let { contact ->
            ContactInfoCard(
                contact = contact,
                onDismiss = { selectedNode = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }

        // Legend
        Column(
            Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Connection strength", style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(12.dp)) { drawCircle(COLD) }
                Spacer(Modifier.width(4.dp))
                Text("Weak", style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(12.dp)) { drawCircle(HOT) }
                Spacer(Modifier.width(4.dp))
                Text("Strong", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun Offset.applyTransform(pan: Offset, scale: Float, cx: Float, cy: Float): Offset {
    // Scale around centre then apply pan
    return Offset(
        (x - cx) * scale + cx + pan.x,
        (y - cy) * scale + cy + pan.y,
    )
}

private fun DrawScope.drawEdge(
    from: Offset, to: Offset, strength: Float, alpha: Float
) {
    drawLine(
        color = strengthColor(strength).copy(alpha = alpha * (0.15f + 0.45f * strength)),
        start = from,
        end = to,
        strokeWidth = (1f + 4f * strength),
    )
}

private fun DrawScope.drawLabel(
    textMeasurer: TextMeasurer,
    text: String,
    center: Offset,
    radius: Float,
    alpha: Float,
) {
    val style = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = alpha),
    )
    val measured = textMeasurer.measure(text, style)
    drawText(
        textMeasurer = textMeasurer,
        text = text,
        style = style,
        topLeft = Offset(
            center.x - measured.size.width / 2f,
            center.y + radius + 4f,
        )
    )
}

@Composable
private fun ContactInfoCard(
    contact: ContactNode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats = contact.stats
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(contact.name, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onDismiss) { Text("✕") }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            StatRow("Calls", "${stats.totalCalls} (${formatDuration(stats.totalCallDurationSeconds)})")
            StatRow("SMS sent / received", "${stats.sentSms} / ${stats.receivedSms}")
            StatRow("Message saliency", "%.0f%%".format(stats.avgSaliency * 100))
            StatRow("Avg response time",
                if (stats.avgResponseGapSeconds > 0) formatDuration(stats.avgResponseGapSeconds) else "—")
            StatRow("Connection score", "%.0f%%".format(contact.connectionStrength * 100))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatDuration(seconds: Long): String = when {
    seconds < 60 -> "${seconds}s"
    seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
    else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
}
