package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findByOwnerItems(@Param("ownerId") Long ownerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.end < :now ORDER BY b.end DESC")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    // Для одиночного поиска (если используется в других местах)
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.end < :now ORDER BY b.end DESC")
    Optional<Booking> findLastByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.start > :now ORDER BY b.start ASC")
    Optional<Booking> findNextByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    //
    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.status = 'APPROVED' AND b.end < :now ORDER BY b.end DESC")
    List<Booking> findLastBookingsByItemIds(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.status = 'APPROVED' AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findNextBookingsByItemIds(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    // Проверка для комментария
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.status = 'APPROVED' AND b.end < :now")
    boolean existsFinishedBooking(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("now") LocalDateTime now);
}
