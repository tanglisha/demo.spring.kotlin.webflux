package com.example.demo.spring.webflux

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@WebFluxTest(controllers = [Routing::class])
@ExtendWith(MockKExtension::class)
class MeetingTests {
    @TestConfiguration
    class MeetingTestConfig {
        @Bean
        fun meetingService() = mockk<MeetingService>()
    }

    @Autowired
    lateinit var meetingService: MeetingService

    @Autowired
    lateinit var client: WebTestClient

    private var now: Instant = Instant.parse("2000-01-01T01:01:01.01Z")

    @Test
    fun listMeetings() {
        val expectedUUID = UUID.randomUUID()

        mockkStatic("java.util.UUID")
        every { UUID.randomUUID() } returns expectedUUID

        var tomorrow = now.plus(1L, ChronoUnit.DAYS)
        var expectedMeeting = Meeting(UUID.randomUUID(), tomorrow, "test meeting")
        var anotherExpectedMeeting = Meeting(UUID.randomUUID(), tomorrow, "test2 meeting")
        every { meetingService.getAll() } returns Flux.just(expectedMeeting, anotherExpectedMeeting)

        client.get().uri("/meetings")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectStatus()
                .is2xxSuccessful
                .expectBodyList(Meeting::class.java)
                .consumeWith<WebTestClient.ListBodySpec<Meeting>> { list ->
                    assertThat(list.responseBody)
                            .isEqualTo(listOf(expectedMeeting, anotherExpectedMeeting))
                }
    }

    @Test
    fun addMeeting() {
        val expectedUUID = UUID.randomUUID()
        val expectedURI = URI("/meetings/$expectedUUID")

        mockkStatic("java.util.UUID")
        every { UUID.randomUUID() } returns expectedUUID

        var tomorrow = now.plus(1L, ChronoUnit.DAYS)
        var newMeeting = Meeting(UUID.randomUUID(), tomorrow, "test meeting")
        var saveResponse: Mono<Meeting> = Mono.from(Flux.just(newMeeting))
        every { meetingService.save(any()) } returns saveResponse

        client.post().uri("/meetings")
                .body(newMeeting)
                .exchange()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectHeader()
                .valueEquals("Location", expectedURI.toString())
                .expectStatus()
                .isCreated
                .expectBody(Meeting::class.java)
    }

    @Test
    fun fetchMeeting() {
        val expectedUUID = UUID.randomUUID()
        val uri = URI("/meetings/$expectedUUID")

        var tomorrow = now.plus(1L, ChronoUnit.DAYS)
        var expectedMeeting = Meeting(expectedUUID, tomorrow, "test meeting")
        every { meetingService.get(any()) } returns Mono.just(expectedMeeting)

        val returnResult = client.get().uri(uri)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectStatus()
                .isOk
                .expectBody(Meeting::class.java)
                .returnResult()
        assertThat(returnResult.responseBody).isEqualToComparingFieldByField(expectedMeeting)
    }
}