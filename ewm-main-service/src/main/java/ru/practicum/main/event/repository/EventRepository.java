package ru.practicum.main.event.repository;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.main.category.model.Category_;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.model.Event_;
import ru.practicum.main.request.model.Request;
import ru.practicum.main.request.model.RequestStatus;
import ru.practicum.main.request.model.Request_;
import ru.practicum.main.user.model.User_;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    interface Specs {
        static Specification<Event> byAnnotationOrDescription(String text) {
            String searchText = text.toLowerCase();
            return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(Event_.annotation)), "%" + searchText + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(Event_.description)), "%" + searchText + "%")
            );
        }

        static Specification<Event> inCategories(List<Long> categories) {
            return (root, query, criteriaBuilder) ->
                    root.get(Event_.category).get(Category_.id).in(categories);
        }

        static Specification<Event> byPaid(Boolean paid) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(Event_.paid), paid);
        }

        static Specification<Event> betweenTime(LocalDateTime startTime, LocalDateTime endTime) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(Event_.eventDate), startTime, endTime);
        }

        static Specification<Event> afterNow() {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(Event_.eventDate), LocalDateTime.now());
        }

        static Specification<Event> byState(EventState state) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(Event_.state), state);
        }

        static Specification<Event> inStates(List<String> states) {
            return (root, query, criteriaBuilder) ->
                    root.get(Event_.state).in(states);
        }

        static Specification<Event> inInitiators(List<Long> users) {
            return ((root, query, criteriaBuilder)
                    -> root.get(Event_.initiator).get(User_.id).in(users));
        }

        static Specification<Event> onlyAvailable() {
            return (root, query, criteriaBuilder) -> {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Request> requestRoot = subquery.from(Request.class);

                subquery.select(criteriaBuilder.count(requestRoot))
                        .where(criteriaBuilder.equal(requestRoot.get(Request_.event), root),
                                criteriaBuilder.equal(requestRoot.get(Request_.status), RequestStatus.CONFIRMED));

                return criteriaBuilder.lt(subquery, root.get(Event_.participantLimit));
            };
        }
    }

    Optional<Event> findByIdAndStateIs(Long eventId, EventState state);

    List<Event> findAllByInitiator_Id(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiator_Id(Long eventId, Long userId);

    boolean existsByCategory_Id(Long categoryId);

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findAllByIdIn(Set<Long> eventsId);
}
