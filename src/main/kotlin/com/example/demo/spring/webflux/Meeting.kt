package com.example.demo.spring.webflux

import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.lang.String.format
import java.net.URI
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class Meeting(
        @Id
        val id: UUID,
        val date: Instant,
        val title: String) {

    fun getUri(): URI {
        return URI(format("/meetings/$id"))
    }

    override fun toString(): String {
        return "[Meeting] $title: $date"
    }
}

data class MeetingsDTO(var id: UUID?, var date: Instant?, var title: String?) {
    var meetings: MutableList<Meeting> = ArrayList()

    constructor(meetings: MutableList<Meeting>): this(null, null, null) {
        this.meetings = meetings
    }
}

@Service
class MeetingService(val meetingRepository: MeetingRepository,
                     val publisher: ApplicationEventPublisher) {
    fun getAll(): Flux<Meeting> {
        return meetingRepository.findAll()
    }

    fun get(id: UUID): Mono<Meeting> {
        return meetingRepository
                .findById(id)
                .switchIfEmpty(Mono.empty())
    }

    fun save(meeting: Meeting): Mono<Meeting> {
        val savedMeeting: Mono<Meeting> = meetingRepository.save(meeting)
        return savedMeeting
                .publishOn(Schedulers.elastic())
                .doOnSuccess { newMeeting ->
                    publisher.publishEvent(MeetingCreatedEvent(newMeeting))
                }
                .doOnError { error ->
                    status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(RestExceptionHandler.ErrorResponse(
                                    error(error.localizedMessage)
                            ))
                }
    }
}

class MeetingCreatedEvent(source: Any) : ApplicationEvent(source)

interface MeetingRepository : ReactiveCrudRepository<Meeting, UUID>
