package cloud.dywy.api.dashboard.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.api.dashboard.response.DashboardStatisticsResponseFixtures.statisticsResponse
import cloud.dywy.domain.dashboard.entity.DashboardStatisticsFixtures.statistics
import kotlin.test.Test

class DashboardStatisticsResponseTest {

    @Test
    fun `should map dashboard statistics to a response`() {
        assertThat(statistics.toResponse()).isEqualTo(statisticsResponse)
    }
}

