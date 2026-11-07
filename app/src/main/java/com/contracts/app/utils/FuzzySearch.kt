package com.contracts.app.utils

object FuzzySearch {
    fun score(query: String, target: String): Int {
        if (query.isEmpty()) return 100
        val q = query.lowercase().trim()
        val t = target.lowercase().trim()
        if (t.contains(q)) return 90
        val words = q.split("\\s+".toRegex())
        val wordScore = words.count { w -> t.contains(w) } * 20
        if (wordScore > 0) return wordScore.coerceAtMost(85)
        val overlap = q.count { c -> t.contains(c) }
        val ratio = (overlap * 100) / q.length.coerceAtLeast(1)
        if (q.length >= 3 && levenshtein(q, t.take(q.length + 3)) <= 2) return 50
        return if (ratio >= 60) ratio / 2 else 0
    }

    fun matches(query: String, vararg fields: String): Boolean {
        if (query.isBlank()) return true
        return fields.any { score(query, it) > 0 }
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) {
            dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
            else minOf(dp[i-1][j]+1, dp[i][j-1]+1, dp[i-1][j-1]+1)
        }
        return dp[a.length][b.length]
    }
}
