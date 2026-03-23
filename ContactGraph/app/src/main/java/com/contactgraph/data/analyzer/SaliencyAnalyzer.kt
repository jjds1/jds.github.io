package com.contactgraph.data.analyzer

import kotlin.math.ln
import kotlin.math.min

/**
 * Scores a single SMS message body for saliency using local heuristics.
 * Returns a value in [0, 1].
 *
 * Heuristics (weighted sum):
 *  - Length score   : longer messages carry more content (log-scaled, capped)
 *  - Question score : messages with questions invite engagement
 *  - Exclamation    : emotional intensity indicator
 *  - Lexical density: ratio of unique words (avoids repetitive filler)
 *  - Negative filler: penalise "ok", "k", "yes", "no", "lol" etc.
 */
object SaliencyAnalyzer {

    private val FILLER_WORDS = setOf(
        "ok", "okay", "k", "yes", "no", "yep", "nope", "lol", "haha",
        "hehe", "hmm", "uh", "um", "sure", "fine", "cool", "wow", "oh",
        "ah", "bye", "hi", "hey", "hello", "thanks", "thx", "ty", "np",
        "sorry", "lmao", "omg", "wtf", "idk"
    )

    fun score(body: String): Float {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return 0f

        // 1. Length score (0..1): ln(len+1) / ln(201) maps 0-200 chars to 0-1
        val lengthScore = min(1f, (ln(trimmed.length.toFloat() + 1f) / ln(201f)))

        // 2. Question indicator
        val questionScore = if (trimmed.contains('?')) 0.6f else 0f

        // 3. Exclamation indicator
        val exclamScore = if (trimmed.contains('!')) 0.3f else 0f

        // 4. Lexical density: unique tokens / total tokens
        val tokens = trimmed.lowercase().split(Regex("\\W+")).filter { it.isNotEmpty() }
        val lexicalDensity = if (tokens.size < 2) 0f
        else tokens.toSet().size.toFloat() / tokens.size.toFloat()

        // 5. Filler penalty: if the whole message is a filler word, score tanks
        val isOnlyFiller = tokens.all { it in FILLER_WORDS }
        val fillerPenalty = if (isOnlyFiller) 0.8f else 0f

        val raw = (lengthScore * 0.35f) +
                  (questionScore * 0.25f) +
                  (exclamScore * 0.10f) +
                  (lexicalDensity * 0.30f) -
                  fillerPenalty

        return raw.coerceIn(0f, 1f)
    }

    /** Average saliency of a list of message bodies. */
    fun averageScore(bodies: List<String>): Float {
        if (bodies.isEmpty()) return 0f
        return bodies.map { score(it) }.average().toFloat()
    }
}
