package com.xzzpig.pigutils.dao;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.database.DBField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public interface ResultSetSolver {
    boolean solve(@NotNull AtomicReference<Object> result, @NotNull DBField dbField, @NotNull ResultSet resultSet) throws SQLException;
}
