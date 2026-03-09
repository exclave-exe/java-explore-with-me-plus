package ru.practicum.main.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static Pageable createPageRequest(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Параметры пагинации некорректные");
        }
        int page = from / size;
        return PageRequest.of(page, size, Sort.by("id").ascending());
    }
}
