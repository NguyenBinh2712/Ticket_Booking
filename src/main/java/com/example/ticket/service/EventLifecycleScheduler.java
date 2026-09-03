package com.example.ticket.service;

import com.example.ticket.entity.Event;
import com.example.ticket.entity.Showtime;
import com.example.ticket.enums.EventStatus;
import com.example.ticket.enums.ShowtimeStatus;
import com.example.ticket.repository.EventRepository;
import com.example.ticket.repository.ShowtimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventLifecycleScheduler {

    EventRepository eventRepository;
    ShowtimeRepository showtimeRepository;
    EventService eventService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateEventLifecycle() {
        LocalDateTime now = LocalDateTime.now();

        List<Showtime> toFinish = showtimeRepository.findByStatusAndEndTimeBefore(ShowtimeStatus.SCHEDULED, now);
        for (Showtime s : toFinish) {
            s.setStatus(ShowtimeStatus.FINISHED);
        }
        showtimeRepository.saveAll(toFinish);
        List<Event> publishedEvents = eventRepository.findByStatus(EventStatus.PUBLISHED);
        for (Event event : publishedEvents) {
            boolean hasStarted = showtimeRepository.findByContract_EventOrderByStartTimeAsc(event)
                    .stream().anyMatch(s -> !s.getStartTime().isAfter(now));
            if (hasStarted) {
                eventService.changeStatus(event, EventStatus.ONGOING, "Tự động: suất chiếu đầu tiên đã bắt đầu");
            }
        }

        List<Event> ongoingEvents = eventRepository.findByStatus(EventStatus.ONGOING);
        for (Event event : ongoingEvents) {
            boolean allFinished = showtimeRepository.findByContract_EventOrderByStartTimeAsc(event)
                    .stream().allMatch(s -> s.getStatus() == ShowtimeStatus.FINISHED
                            || s.getStatus() == ShowtimeStatus.CANCELLED);
            if (allFinished) {
                eventService.changeStatus(event, EventStatus.COMPLETED, "Tự động: toàn bộ suất chiếu đã kết thúc");
            }
        }
    }
}