package com.example.demo.spring.webflux

import org.jboss.jandex.ParameterizedType
import org.reactivestreams.Publisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters.fromPublisher
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Component
class Routing {
    @Bean
    fun routes(service: MeetingService, publisher: ApplicationEventPublisher): RouterFunction<ServerResponse> = router {

        ("/meetings").nest {

            contentType(MediaType.APPLICATION_JSON)

            GET("/") {
                val interval = Flux.interval(Duration.ofSeconds(1L))

                var response = Flux.zip(service.getAll(), interval)

                ok().body(
                        response, response.javaClass
                )
            }

            GET("/{id}") {request ->
                service.get(UUID.fromString(request.pathVariable("id")))
                        .flatMap { meeting -> ok().body(meeting) }
                        .switchIfEmpty(notFound().build())
            }

            POST("/") { req ->
                var meetingMono: Mono<Meeting> = req.bodyToMono(Meeting::class.java)
                val uuid: UUID = UUID.randomUUID()
                val body = fromPublisher(
                        meetingMono
                                .map { m -> Meeting(uuid, m.date, m.title)}
                                .flatMap { m -> service.save(m) },
                        Meeting::class.java
                )

                created(UriComponentsBuilder
                        .fromPath("/meetings/$uuid").build().toUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
            }
        }
    }
}