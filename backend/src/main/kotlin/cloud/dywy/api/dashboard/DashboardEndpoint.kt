package cloud.dywy.api.dashboard

import cloud.dywy.api.dashboard.response.toResponse
import cloud.dywy.application.dashboard.DashboardStatisticsGetter
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class DashboardEndpoint(private val dashboardStatisticsGetter: DashboardStatisticsGetter) {

    @Suppress("UNUSED_PARAMETER")
    fun getDashboard(request: ServerRequest): ServerResponse =
        ServerResponse.ok().body(dashboardStatisticsGetter.get().toResponse())
}
