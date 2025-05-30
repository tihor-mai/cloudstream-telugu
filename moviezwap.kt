telugupalaka.kt
package com.lagradost.cloudstream3

class TeluguPalakaPlugin : MainAPI() {
    override var mainUrl = "https://telugupalaka.com"
    override var name = "TeluguPalaka"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(mainUrl).document
        val items = doc.select(".item").mapNotNull {
            val type = if (it.selectFirst(".type")?.text()?.contains("TV") == true) TvType.TvSeries else TvType.Movie
            val title = it.selectFirst("h2 a")?.text() ?: return@mapNotNull null
            val href = fixUrl(it.selectFirst("a")?.attr("href") ?: "")
            val poster = it.selectFirst("img")?.attr("src")
            
            if (type == TvType.Movie) {
                MovieSearchResponse(title, href, this.name, type, poster, null, null)
            } else {
                TvSeriesSearchResponse(title, href, this.name, type, poster, null, null)
            }
        }
        return HomePageResponse(listOf(HomePageList("Latest Updates", items)))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select(".item").map {
            val type = if (it.selectFirst(".type")?.text()?.contains("TV") == true) TvType.TvSeries else TvType.Movie
            val title = it.selectFirst("h2 a")?.text() ?: ""
            val href = fixUrl(it.selectFirst("a")?.attr("href") ?: "")
            val poster = it.selectFirst("img")?.attr("src")
            
            if (type == TvType.Movie) {
                MovieSearchResponse(title, href, this.name, type, poster, null, null)
            } else {
                TvSeriesSearchResponse(title, href, this.name, type, poster, null, null)
            }
        }
    }

    // Load function similar to iBOMMA with site-specific parsing
}
