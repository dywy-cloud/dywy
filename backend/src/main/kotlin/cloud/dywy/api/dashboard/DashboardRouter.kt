package cloud.dywy.api.dashboard

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class DashboardRouter(private val dashboardEndpoint: DashboardEndpoint) {

    @Bean
    fun dashboardRoute() = router {
        GET("/api/dashboard", dashboardEndpoint::getDashboard)
    }
}
