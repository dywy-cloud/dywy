package cloud.dywy.api.dashboard

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.dywy.AbstractEndpointIntegrationTest
import cloud.dywy.api.dashboard.response.DashboardStatisticsResponse
import cloud.dywy.api.dashboard.response.DashboardStatisticsResponseFixtures.seededDashboardStatistics
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import kotlin.test.Test

class DashboardEndpointIT : AbstractEndpointIntegrationTest() {

    @Test
    fun `should return dashboard statistics to an admin`() {
        assertThat(getDashboard("gregory@example.com")).isEqualTo(seededDashboardStatistics)
    }

    @Test
    fun `should return dashboard statistics to a read-only user`() {
        assertThat(getDashboard("viewer@example.com")).isEqualTo(seededDashboardStatistics)
    }

    @Test
    fun `should forbid an authenticated but unlisted user from reading the dashboard`() {
        val csrf = authenticatedCsrfContext("stranger@example.com")

        restTestClient.get().uri("/api/dashboard")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should forbid read-only user from mutating the dashboard`() {
        val csrf = authenticatedCsrfContext("viewer@example.com")

        restTestClient.post().uri("/api/dashboard")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    private fun getDashboard(email: String): DashboardStatisticsResponse =
        restTestClient.get().uri("/api/dashboard")
            .header(HttpHeaders.COOKIE, authenticatedCsrfContext(email).cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(DashboardStatisticsResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Missing dashboard response")
}
