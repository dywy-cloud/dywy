package cloud.dywy.application.song

import io.github.oshai.kotlinlogging.KotlinLogging
import cloud.dywy.application.song.result.SongSearchResult
import cloud.dywy.domain.song.SongCatalog
import cloud.dywy.domain.song.SongCatalogUnavailableException
import cloud.dywy.infrastructure.shared.warnWithDetails
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SongSearcher(private val songCatalog: SongCatalog) {

    fun search(query: String): SongSearchResult {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return SongSearchResult.Suggestions(emptyList())

        return try {
            SongSearchResult.Suggestions(songCatalog.search(trimmed))
        } catch (e: SongCatalogUnavailableException) {
            logger.warnWithDetails(e, "Song catalog unavailable") { "Song catalog unavailable for query '$trimmed'" }
            SongSearchResult.Unavailable
        }
    }
}




