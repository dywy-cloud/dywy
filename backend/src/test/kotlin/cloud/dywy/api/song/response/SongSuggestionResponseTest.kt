package cloud.dywy.api.song.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.song.response.SongSuggestionResponseFixtures.laVieEnRose
import cloud.dywy.domain.song.entity.SongSuggestionFixtures
import kotlin.test.Test

class SongSuggestionResponseTest {

    @Test
    fun `should map a suggestion to a response`() {
        assertThat(SongSuggestionFixtures.laVieEnRose.toResponse()).isEqualTo(laVieEnRose)
    }
}

