package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exception.BookingNotFoundException;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    Optional<Booking> findWithRelationsById(long bookingId);

    default Booking findWithRelationsOrThrow(long bookingId) {
        return findWithRelationsById(bookingId)
                .orElseThrow(
                        () -> new BookingNotFoundException(String.format("Booking with %d not found", bookingId))
                );
    }

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker bkr
            JOIN FETCH b.item i
            JOIN FETCH i.owner o
            WHERE i.owner.id = :ownerId
            AND (:state = 'ALL'
                OR (:state = 'CURRENT' AND b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP)
                OR (:state = 'PAST' AND b.end < CURRENT_TIMESTAMP)
                OR (:state = 'FUTURE' AND b.start > CURRENT_TIMESTAMP)
                OR (:state = 'WAITING' AND b.status = 'WAITING')
                OR (:state = 'REJECTED' AND b.status = 'REJECTED')
            )
            ORDER BY b.start DESC
            """)
    List<Booking> findOwnerBookingsByState(Long ownerId, String state);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker bkr
            JOIN FETCH b.item i
            JOIN FETCH i.owner o
            WHERE bkr.id = :bookerId
            AND (:state = 'ALL'
                OR (:state = 'CURRENT' AND b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP)
                OR (:state = 'PAST' AND b.end < CURRENT_TIMESTAMP)
                OR (:state = 'FUTURE' AND b.start > CURRENT_TIMESTAMP)
                OR (:state = 'WAITING' AND b.status = 'WAITING')
                OR (:state = 'REJECTED' AND b.status = 'REJECTED')
            )
            ORDER BY b.start DESC
            """)
    List<Booking> findUserBookingsByState(Long bookerId, String state);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.item i
            WHERE b.item.id IN :itemIds
            AND b.end = (
                SELECT MAX(b2.end) FROM Booking b2
                WHERE b2.item.id = b.item.id
                AND b2.status = 'APPROVED'
                and b2.start < CURRENT_TIMESTAMP
            )
            """)
    List<Booking> findLastItemsBooking(List<Long> itemIds);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.item i
            WHERE b.item.id IN :itemIds
            AND b.end = (
                SELECT MIN(b2.end) FROM Booking b2
                WHERE b2.item.id = b.item.id
                AND b2.status = 'APPROVED'
                and b2.start > CURRENT_TIMESTAMP
            )
            """)
    List<Booking> findNextItemsBooking(List<Long> itemIds);

    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.item.id = :itemId AND b.booker.id = :userId
            AND b.end < CURRENT_TIMESTAMP
            """)
    boolean hasUserCompletedBooking(long userId, long itemId);
}
