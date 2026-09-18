package cloud.dywy.api.rsvp.response

import cloud.dywy.domain.rsvp.entity.SongChoice

data class SongResponse(
    val deezerId: Long,
    val title: String,
    val artist: String,
    val link: String,
    val preview: String?,
)

internal fun SongChoice.toResponse() = SongResponse(
    deezerId = deezerId,
    title = title,
    artist = artist,
    link = link,
    preview = preview,
)

