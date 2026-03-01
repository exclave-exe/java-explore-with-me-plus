package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.main.event.model.Event;

import java.util.List;

public interface StatService {
    void hit(String appName, HttpServletRequest request);

    void fillListWithViews(List<Event> events);

    void fillWithViews(Event event);
}
