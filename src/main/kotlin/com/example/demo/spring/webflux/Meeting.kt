package com.example.demo.spring.webflux

import org.reactivestreams.Publisher
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyWithType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.persistence.Entity

@Entity
data class Meeting(
        val id: UUID,
        val date: Instant,
        val title: String
)

@Service
class MeetingService(val meetingRepository: MeetingRepository) {
    fun getAll(request: ServerRequest): Mono<ServerResponse> {
        val interval = Flux.interval(Duration.ofSeconds(1L))

        val meetings = meetingRepository.findAll()

        return ok().bodyWithType<Publisher<Meeting>>(
                Flux.zip(interval, meetings)
                        .map { it.t2 })
    }
}

@Repository
interface MeetingRepository : ReactiveCrudRepository<Meeting, UUID> {
    override fun findAll(): Flux<Meeting>
}