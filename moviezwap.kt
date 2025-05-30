moviezwap.kt
package com.lagradost.cloudstream3

class MoviezwapPlugin : MainAPI() {
    override var mainUrl = "https://moviezwap.org"
    override var name = "Moviezwap HD"
    override val supportedTypes = setOf(TvType.Movie)

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select(".item").map {
            MovieSearchResponse(
                it.selectFirst("h2 a")?.text() ?: "",
                fixUrl(it.selectFirst("a")?.attr("href") ?: ""),
                this.name,
                TvType.Movie,
                it.selectFirst("img")?.attr("src"),
                null,
                null
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val sources = doc.select("source").map {
            val quality = it.attr("label")
            val src = it.attr("src")
            // Prioritize 1080p
            if (quality.contains("1080")) {
                ExtractorLink(this.name, quality, src, "", Qualities.FullHd.value)
            } else {
                ExtractorLink(this.name, quality, src, "", Qualities.Unknown.value)
            }
        }
        return MovieLoadResponse(
            doc.selectFirst("h1.entry-title")?.text() ?: "",
            url,
            TvType.Movie,
            sources.firstOrNull()?.url ?: "",
            doc.selectFirst(".post-thumbnail img")?.attr("src"),
            null,
            doc.selectFirst(".entry-content p")?.text(),
            null,
            null,
            null,
            null
        )
    }
}
