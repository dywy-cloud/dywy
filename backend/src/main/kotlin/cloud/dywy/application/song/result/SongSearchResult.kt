package cloud.dywy.application.song.result

import cloud.dywy.domain.song.entity.SongSuggestion

sealed interface SongSearchResult {
    data class Suggestions(val suggestions: List<SongSuggestion>) : SongSearchResult
    data object Unavailable : SongSearchResult
}

