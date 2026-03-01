package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stats, Long> {
    @Query("""
            select s.app as app, s.uri as uri, count(s) as hits
            from Stats s
            where s.timestamp between :start and :end
            group by s.app, s.uri
            order by count(s) desc
            """)
    List<StatView> findStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    @Query("""
            select s.app as app, s.uri as uri, count(s) as hits
            from Stats s
            where s.timestamp between :start and :end
              and s.uri in :uris
            group by s.app, s.uri
            order by count(s) desc
            """)
    List<StatView> findStatsWithUris(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("uris") List<String> uris);

    @Query("""
            select s.app as app, s.uri as uri, count(distinct s.ip) as hits
            from Stats s
            where s.timestamp between :start and :end
            group by s.app, s.uri
            order by count(distinct s.ip) desc
            """)
    List<StatView> findUniqueStats(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Query("""
            select s.app as app, s.uri as uri, count(distinct s.ip) as hits
            from Stats s
            where s.timestamp between :start and :end
              and s.uri in :uris
            group by s.app, s.uri
            order by count(distinct s.ip) desc
            """)
    List<StatView> findUniqueStatsWithUris(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("uris") List<String> uris);
}
