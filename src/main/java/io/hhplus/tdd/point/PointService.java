package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

import java.util.List;

public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    public UserPoint getPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public UserPoint charge(long userId, long amount) {
        if(amount <= 0) throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");

        UserPoint current = userPointTable.selectById(userId);
        long newPoint = current.point() + amount;
        UserPoint updated = new UserPoint(userId, newPoint, System.currentTimeMillis());

        userPointTable.insertOrUpdate(userId, newPoint);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return updated;
    }

    public UserPoint use(long userId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");

        UserPoint current = userPointTable.selectById(userId);
        if (current.point() < amount) throw new IllegalArgumentException("잔고 부족");

        long newPoint = current.point() - amount;
        userPointTable.insertOrUpdate(userId, newPoint);
        UserPoint updated = new UserPoint(userId, newPoint, System.currentTimeMillis());
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return updated;
    }

    public List<PointHistory> getHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
