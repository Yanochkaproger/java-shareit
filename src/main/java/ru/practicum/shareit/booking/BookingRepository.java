package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Все бронирования пользователя-арендатора (сортировка по дате начала, новые первыми)
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    // Все бронирования вещей владельца (сортировка по дате начала, новые первыми)
    @Query("SELECT b FROM Booking b WHERE b.itemId IN " +
            "(SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) " +
            "ORDER BY b.start DESC")
    List<Booking> findByOwnerItems(@Param("ownerId") Long ownerId);

    // Бронирования по статусу для арендатора
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    // Текущие бронирования (активные сейчас)
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    // Прошлые бронирования
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.end < :now ORDER BY b.end DESC")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    // Будущие бронирования
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    // Ближайшее будущее подтверждённое бронирование для вещи
    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.status = 'APPROVED' AND b.start > :now ORDER BY b.start ASC LIMIT 1")
    Booking findNextApproved(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    // Последнее подтверждённое бронирование для вещи
    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.status = 'APPROVED' AND b.end < :now ORDER BY b.end DESC LIMIT 1")
    Booking findLastApproved(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);
}

