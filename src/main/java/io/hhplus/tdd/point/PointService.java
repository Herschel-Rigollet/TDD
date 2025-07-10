package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;

public class PointService {
    private final UserPointTable userPointTable;

    public PointService(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    public long getPoint(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);
        return userPoint.point();
    }
}
