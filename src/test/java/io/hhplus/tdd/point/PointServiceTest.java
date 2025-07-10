package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointServiceTest {

    @Test
    void 정상_조회() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table, historyTable);

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
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table, historyTable);

        // When Then
        assertThrows(NullPointerException.class, () -> service.getPoint(999L));
    }

    @Test
    void 포인트가_0인_사용자_조회() {
        // Given
        long userId = 2L;
        UserPointTable table = new UserPointTable();
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 0);

        PointService service = new PointService(table, historyTable);

        // When Then
        assertEquals(0, service.getPoint(2L));
    }

    @Test
    void 잘못된_ID() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table, historyTable);

        // When Then
        assertThrows(IllegalArgumentException.class, () -> service.getPoint(-1L));
    }

    @Test
    void 정상_충전() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 5000);

        PointService service = new PointService(table, historyTable);

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
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 0);

        PointService service = new PointService(table, historyTable);

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
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(1L, 500);

        PointService service = new PointService(table, historyTable);

        // When Then
        assertThrows(NullPointerException.class, () -> service.charge(userId, 1000));
    }

    @Test
    void 음수_포인트_충전_시도() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 500);

        PointService service = new PointService(table, historyTable);

        // When Then
        assertThrows(IllegalArgumentException.class, () -> service.charge(userId, -500));
    }

    @Test
    void 포인트_0_충전() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 1000);

        PointService service = new PointService(table, historyTable);

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
        PointHistoryTable historyTable = new PointHistoryTable();
        table.insertOrUpdate(userId, 0);

        PointService service = new PointService(table, historyTable);

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
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When
        service.charge(userId, 100);
        service.charge(userId, 200);
        service.charge(userId, 300);

        // Then
        //assertEquals(1600, service.getPoint(userId));
        List<PointHistory> histories = historyTable.selectAllByUserId(userId);
        assertEquals(3, histories.size());
        assertEquals(100, histories.get(0).amount());
        assertEquals(200, histories.get(1).amount());
    }

    @Test
    void 포인트_사용_및_내역_저장() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 1000);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When
        service.use(userId, 300);

        // Then
        //assertEquals(700, service.getPoint(userId));
        List<PointHistory> histories = historyTable.selectAllByUserId(userId);
        assertEquals(1, histories.size());

        PointHistory entry = histories.get(0);
        assertEquals(userId, entry.userId());
        assertEquals(300, entry.amount());
        assertEquals(TransactionType.USE, entry.type());
    }

    @Test
    void 잔고_부족_시_내역_저장_및_예외() {
        // Given
        long userId = 2L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 100);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When Then
        assertThrows(IllegalStateException.class, () -> service.use(userId, 500));
        assertEquals(0, historyTable.selectAllByUserId(userId).size());
    }

    @Test
    void 음수_혹은_0_포인트_사용_시_내역_저장_및_예외() {
        // Given
        long userId = 3L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 0);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When Then
        assertThrows(IllegalArgumentException.class, () -> service.use(userId, 0));
        assertThrows(IllegalArgumentException.class, () -> service.use(userId, -500));
        assertEquals(0, historyTable.selectAllByUserId(userId).size());
        assertEquals(0, historyTable.selectAllByUserId(userId).size());
    }

    @Test
    void 포인트_충전_시_내역_저장() {
        // Given
        long userId = 1L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 0);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When
        service.charge(userId, 1000);

        // Then
        List<PointHistory> histories = historyTable.selectAllByUserId(userId);
        assertEquals(1, histories.size());

        PointHistory history = histories.get(0);
        assertEquals(userId, history.userId());
        assertEquals(1000, history.amount());
        assertEquals(TransactionType.CHARGE, history.type());
    }

    @Test
    void 포인트_0_충전_시_내역_저장() {
        // Given
        long userId = 2L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 0);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When Then
        assertThrows(IllegalArgumentException.class, () -> service.charge(userId, 0));
        assertEquals(0, historyTable.selectAllByUserId(userId).size());
    }

    @Test
    void 음수_충전_시_내역_저장() {
        // Given
        long userId = 3L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 1000);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When Then
        assertThrows(IllegalArgumentException.class, () -> service.charge(userId, -500));
        assertEquals(0, historyTable.selectAllByUserId(userId).size());
    }

    @Test
    void 여러_번_충전_시_내역_저장() {
        // Given
        long userId = 4L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 0);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When
        service.charge(userId, 100);
        service.charge(userId, 200);

        // Then
        List<PointHistory> histories = historyTable.selectAllByUserId(userId);
        assertEquals(2, histories.size());

        assertEquals(100, histories.get(0).amount());
        assertEquals(200, histories.get(1).amount());
    }

    @Test
    void 여러_사용자_충전_시_내역_저장() {
        // Given
        long userA = 10L;
        long userB = 20L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userA, 0);
        table.insertOrUpdate(userB, 0);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When
        service.charge(userA, 1000);
        service.charge(userB, 2000);

        // Then
        assertEquals(1, historyTable.selectAllByUserId(userA).size());
        assertEquals(1, historyTable.selectAllByUserId(userB).size());
    }

    @Test
    void 최대값_충전_시_내역_저장() {
        // Given
        long userId = 99L;
        UserPointTable table = new UserPointTable();
        table.insertOrUpdate(userId, 0);
        PointHistoryTable historyTable = new PointHistoryTable();

        PointService service = new PointService(table, historyTable);

        // When
        service.charge(userId, Long.MAX_VALUE);

        // Then
        List<PointHistory> histories = historyTable.selectAllByUserId(userId);
        assertEquals(1, histories.size());
        assertEquals(Long.MAX_VALUE, histories.get(0).amount());
    }
}