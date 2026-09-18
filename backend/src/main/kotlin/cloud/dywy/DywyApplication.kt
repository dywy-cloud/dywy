package cloud.dywy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DywyApplication

fun main(args: Array<String>) {
    runApplication<DywyApplication>(*args)
}
