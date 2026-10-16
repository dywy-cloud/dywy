package cloud.dywy

import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.statements.StatementInterceptor
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(TestAuthenticationConfig::class, MailpitTestConfig::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql(scripts = ["classpath:data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
abstract class AbstractIntegrationTest {

    @Autowired
    private lateinit var platformTransactionManager: PlatformTransactionManager

    companion object {
        private const val MAILPIT_SMTP_PORT = 1025
        private const val MAILPIT_HTTP_PORT = 8025

        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("wedding_db")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-test-db.sql")

        @Container
        @JvmStatic
        val mailpit = GenericContainer("axllent/mailpit:v1.27.4")
            .withExposedPorts(MAILPIT_SMTP_PORT, MAILPIT_HTTP_PORT)

        @JvmStatic
        @DynamicPropertySource
        fun registerMailProperties(registry: DynamicPropertyRegistry) {
            if (!mailpit.isRunning) mailpit.start()

            registry.add("spring.mail.host") { mailpit.host }
            registry.add("spring.mail.port") { mailpit.getMappedPort(MAILPIT_SMTP_PORT) }
            registry.add("app.mail.provider") { "smtp" }
            registry.add("app.mail.from") { "no-reply@localhost" }
        }
    }

    protected fun mailpitApiBaseUrl() = "http://${mailpit.host}:${mailpit.getMappedPort(MAILPIT_HTTP_PORT)}"

    protected fun <T> withExecutedSql(function: () -> T): Pair<T, List<String>> =
        mutableListOf<String>().let { statements ->
            checkNotNull(
                TransactionTemplate(platformTransactionManager).execute {
                    TransactionManager.current().registerInterceptor(object : StatementInterceptor {
                        override fun beforeExecution(transaction: Transaction, context: StatementContext) {
                            statements += context.sql(transaction)
                        }
                    })
                    function() to statements
                }
            )
        }
}