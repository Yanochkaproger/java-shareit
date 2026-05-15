package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Поиск по владельцу вещи
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findByOwnerItems(@Param("ownerId") Long ownerId);

    // Поиск по бронеру (арендатору)
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    // Фильтрация по статусу для арендатора
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    // Активные бронирования арендатора
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    // Прошедшие бронирования арендатора
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.end < :now ORDER BY b.end DESC")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    // Будущие бронирования арендатора
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    // Последнее бронирование для вещи (для ItemDto)
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.end < :now ORDER BY b.end DESC")
    Optional<Booking> findLastByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    // Ближайшее будущее бронирование для вещи (для ItemDto)
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.start > :now ORDER BY b.start ASC")
    Optional<Booking> findNextByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    // Проверка: есть ли завершенное бронирование у пользователя на эту вещь
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.status = 'APPROVED' AND b.end < :now")
    boolean existsFinishedBooking(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("now") LocalDateTime now);
}
