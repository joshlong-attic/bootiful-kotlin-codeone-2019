package com.example.reservationservice

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.ApplicationListener
import org.springframework.context.support.beans
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.reactive.function.server.*
import java.util.*

@SpringBootApplication
class ReservationServiceApplication


fun mainDsls(args: Array<String>) {

    runApplication<ReservationServiceApplication>(*args) {

        val config = beans {
            bean {
                router {
                    val customers = ref<CustomerRepository>()
                    GET("/customers") {
                        ServerResponse.ok().body(customers.findAll())
                    }
                }
            }
            bean {
                ref<RouteLocatorBuilder>().routes {
                    route {
                        host ("*.spring.io") and path("/proxy")
                        filters {
                            setPath("/guides")
                        }
                        uri ("https://spring.io")
                    }
                }
            }
        }
        addInitializers(config)
    }
}


fun main (args : Array<String>) {
    mainFlow(args)
}

@FlowPreview
fun mainFlow(args: Array<String>) {
    runApplication<ReservationServiceApplication>(*args) {

        val config = beans {
            bean {
                coRouter {
                    val customers = ref<CustomerRepository>()
                    GET("/customers"){
                        ServerResponse.ok().bodyAndAwait(customers.findAll().asFlow())
                    }
                }
            }
            bean {
                ApplicationListener<ApplicationReadyEvent> {
                    val customers = ref<CustomerRepository>()
                    runBlocking {
                        listOf("Gabriel", "Tammie", "Kimly", "Madhura", "Eddù", "Zhen", "Dave", "Stephane", "George")
                                .map { Customer(UUID.randomUUID().toString(), it) }
                                .map { customers.save(it).awaitFirst() }
                                .forEach { println(it) }
                    }
                }
            }
        }
        addInitializers(config)
    }
}


interface CustomerRepository : ReactiveCrudRepository<Customer, String>

/*
@Configuration
class ExposedConfig {

    @Bean
    fun springTransactionManager(ds: DataSource) = SpringTransactionManager(ds)
}

@Configuration
class CustomerHttpConfiguration {

    @Bean
    fun routes(customers:CustomerRepository) =
       route()
        .GET("/customers" ) { r ->  ServerResponse.ok().body (customers.findAll()) }
        .build()
}

@Component
class Initializer(private val customerService: CustomerService) {

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        customerService.insert("Jane")
        customerService.insert("John")
        customerService.all().forEach { println(it) }
    }
}

@Profile("jdbc")
@Service
@Transactional
class JdbcCustomerService(private val jdbcTemplate: JdbcTemplate) : CustomerService {

    override fun all(): Collection<Customer> =
            this.jdbcTemplate.query("select * from CUSTOMERS") { rs, _ ->
                Customer(rs.getInt("id"), rs.getString("name"))
            }

    override fun insert(name: String) {
        this.jdbcTemplate.update("insert into CUSTOMERS(name) values(?)", name)
    }
}

object Customers : Table() {
    val id = integer("id").primaryKey()
    val name = varchar("name", 255).nullable()
}

//@Profile("exposed")
@Service
@Transactional
class ExposedCustomerService(private val txt: TransactionTemplate) : CustomerService, InitializingBean {

    override fun afterPropertiesSet() {
        this.txt.execute {
            SchemaUtils.create(Customers)
        }
    }

    override fun all(): Collection<Customer> =
            Customers.selectAll().map { Customer(it[Customers.id], it[Customers.name]) }

    override fun insert(name: String) {
        Customers.insert { it[Customers.name] = name }
    }
}

interface CustomerService {
    fun all(): Collection<Customer>
    fun insert(name: String)
}
*/

data class Customer(val id: String? = null, val name: String? = null)
//data class Customer(val id: Int? = null, val name: String? = null)

