package cloud.dywy.api.common

import io.mockk.every
import io.mockk.mockk
import org.springframework.web.servlet.function.ServerRequest
import java.util.Optional

object ServerRequestTestHelper {

    fun requestWithParams(vararg params: Pair<String, String>): ServerRequest =
        mockk<ServerRequest>().also { request ->
            every { request.param(any()) } answers { Optional.ofNullable(params.toMap()[firstArg<String>()]) }
        }
}
