package cloud.dywy.api.rsvp.request

import cloud.dywy.domain.rsvp.entity.SongChoice

data class SubmitSongRequest(
    val deezerId: Long,
    val title: String,
    val artist: String,
    val link: String,
    val preview: String? = null,
) {
    internal fun toSongChoice() = SongChoice(
        deezerId = deezerId,
        title = title,
        artist = artist,
        link = link,
        preview = preview,
    )
}

