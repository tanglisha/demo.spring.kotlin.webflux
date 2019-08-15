package com.example.demo.spring.webflux

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Routing {
    @Bean
    fun meetingsRouter(service: MeetingService) = router {
        ("/meetings" and accept(MediaType.APPLICATION_JSON))
                .nest {
                    GET("/", service::getAll)
                }
    }
}