package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointServiceTest {

    @Test
    void 정상_조회() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table);

        // When
        long result = service.getPoint(userId);

        // Then
        assertEquals(1000, result);
    }

    @Test
    void 존재하지_않는_사용자_조회() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table);

        // When Then
        assertThrows(NullPointerException.class, () -> service.getPoint(999L));
    }

    @Test
    void 포인트가_0인_사용자_조회() {
        // Given
        long userId = 2L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 0);

        PointService service = new PointService(table);

        // When Then
        assertEquals(0, service.getPoint(2L));
    }

    @Test
    void 잘못된_ID() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table);

        // When Then
        assertThrows(IllegalArgumentException.class, () -> service.getPoint(-1L));
    }

    @Test
    void 정상_충전() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 5000);

        PointService service = new PointService(table);

        // When
        service.charge(userId, 3000); // 5000 + 3000 = 8000

        // Then
        long result = service.getPoint(userId);
        assertEquals(8000, result);
    }

    @Test
    void 포인트가_0일_때_첫충전() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 0);

        PointService service = new PointService(table);

        // When
        service.charge(userId, 1000);

        // Then
        assertEquals(1000, service.getPoint(userId));
    }

    @Test
    void 존재하지_않는_사용자_충전() {
        // Given
        long userId = 999L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(1L, 500);

        PointService service = new PointService(table);

        // When Then
        assertThrows(NullPointerException.class, () -> service.charge(userId, 1000));
    }

    @Test
    void 음수_포인트_충전_시도() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 500);

        PointService service = new PointService(table);

        // When Then
        assertThrows(IllegalArgumentException.class, () -> service.charge(userId, -500));
    }

    @Test
    void 포인트_0_충전() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table);

        // When
        service.charge(userId, 0);

        // Then
        assertEquals(1000, service.getPoint(userId));
    }

    @Test
    void 매우_큰_금액_충전() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 0);

        PointService service = new PointService(table);

        // When
        service.charge(userId, Long.MAX_VALUE);

        // Then
        assertEquals(Long.MAX_VALUE, service.getPoint(userId));
    }

    @Test
    void 여러_번_연속_충전() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table);

        // When
        service.charge(userId, 100);
        service.charge(userId, 200);
        service.charge(userId, 300);

        // Then
        assertEquals(1600, service.getPoint(userId));
    }
}