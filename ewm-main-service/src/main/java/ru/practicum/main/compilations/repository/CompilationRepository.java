package ru.practicum.main.compilations.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.compilations.model.Compilation;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT c FROM Compilation AS c LEFT JOIN FETCH c.events WHERE c.id = :id")
    Optional<Compilation> findByIdWithEvents(@Param("id") Long id);

    @Query("SELECT c FROM Compilation AS c LEFT JOIN FETCH c.events WHERE c.id IN :ids")
    List<Compilation> findAllWithEvents(@Param("ids") List<Long> ids, Sort sort);

    @Query("SELECT c.id FROM Compilation AS c WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    List<Long> findIds(@Param("pinned") Boolean pinned, Pageable pageable);
}
