ibomma.kt
package com.lagradost.cloudstream3

class iBOMMAPlugin : MainAPI() {
    override var mainUrl = "https://ibomma.one"
    override var name = "iBOMMA"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(mainUrl).document
        val movies = doc.select("section#movies").first()?.select("article.movie")?.map {
            MovieSearchResponse(
                it.selectFirst("h2 a")?.text() ?: "",
                fixUrl(it.selectFirst("a")?.attr("href") ?: ""),
                this.name,
                TvType.Movie,
                it.selectFirst("img")?.attr("data-src"),
                null,
                null
            )
        } ?: listOf()

        val series = doc.select("section#series").first()?.select("article.movie")?.map {
            TvSeriesSearchResponse(
                it.selectFirst("h2 a")?.text() ?: "",
                fixUrl(it.selectFirst("a")?.attr("href") ?: ""),
                this.name,
                TvType.TvSeries,
                it.selectFirst("img")?.attr("data-src"),
                null,
                null
            )
        } ?: listOf()

        return HomePageResponse(listOf(
            HomePageList("Latest Movies", movies),
            HomePageList("TV Series", series)
        ))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("article.movie").map {
            if (it.selectFirst(".type")?.text() == "TV") {
                TvSeriesSearchResponse(
                    it.selectFirst("h2 a")?.text() ?: "",
                    fixUrl(it.selectFirst("a")?.attr("href") ?: ""),
                    this.name,
                    TvType.TvSeries,
                    it.selectFirst("img")?.attr("data-src"),
                    null,
                    null
                )
            } else {
                MovieSearchResponse(
                    it.selectFirst("h2 a")?.text() ?: "",
                    fixUrl(it.selectFirst("a")?.attr("href") ?: ""),
                    this.name,
                    TvType.Movie,
                    it.selectFirst("img")?.attr("data-src"),
                    null,
                    null
                )
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val isMovie = !url.contains("/series/")
        val title = doc.selectFirst(".info h1")?.text() ?: ""
        val poster = doc.selectFirst(".poster img")?.attr("src")
        val year = doc.selectFirst(".info:contains(Year) a")?.text()?.toIntOrNull()
        val plot = doc.selectFirst(".wp-content")?.text()
        
        return if (isMovie) {
            MovieLoadResponse(
                title,
                url,
                TvType.Movie,
                doc.selectFirst("iframe")?.attr("src") ?: "",
                poster,
                year,
                plot,
                null,
                null,
                null
            )
        } else {
            val episodes = doc.select(".eplist li").map {
                val epNum = it.attr("data-index").toIntOrNull() ?: 0
                Episode(
                    it.selectFirst("a")?.attr("href") ?: "",
                    "Episode $epNum",
                    season = 1,
                    episode = epNum
                )
            }.reversed()
            
            TvSeriesLoadResponse(
                title,
                url,
                TvType.TvSeries,
                null,
                poster,
                year,
                plot,
                null,
                episodes,
                null
            )
        }
    }
    
    override suspend fun loadLinks(data: String, isCasting: Boolean, callback: (ExtractorLink) -> Unit): Boolean {
        listOf(
            ExtractorLink(this.name, "1080p", data, "", Qualities.Unknown.value, false)
        ).forEach(callback)
        return true
    }
}
