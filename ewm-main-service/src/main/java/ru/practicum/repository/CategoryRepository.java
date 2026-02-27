package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    @Query(value = """
            SELECT *
            FROM categories
            ORDER BY id
            LIMIT :size OFFSET :from
            """, nativeQuery = true)
    List<Category> findAllWithOffset(@Param("from") int from,
                                     @Param("size") int size);
}