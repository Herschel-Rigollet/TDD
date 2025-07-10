package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PointServiceTest {

    @Test
    void 포인트_정상_조회() {
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
    void 잘못된_ID의_포인트_조회() {
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
}
