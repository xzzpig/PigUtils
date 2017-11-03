package com.github.xzzpig.pigutils.dao.exception;


import com.github.xzzpig.pigutils.database.DBField;

import java.sql.SQLException;

public class ResultSetSolveFailedException extends SQLException {
    public ResultSetSolveFailedException(DBField dbField, SQLException e) {
        super("Unable to transfer JDBCType:" + dbField.getType().getName() + " to Java Class:" + dbField.DaoFiled.getType().getName(), e);
    }
}
