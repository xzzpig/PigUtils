package com.xzzpig.pigutils.database

import com.xzzpig.pigutils.core.TransformManager
import com.xzzpig.pigutils.core.to
import com.xzzpig.pigutils.data.toBean
import com.xzzpig.pigutils.data.toIData
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.*
import java.sql.Array

class DBUtils(connectionCreator: () -> Connection) {

    private val creator = connectionCreator
    private val threadLocal = ThreadLocal<Connection>()

    /**
     * 是否宽容处理事务开始与结束<br/>
     * @see startTransaction
     * @see endTransaction
     * @see commit
     * @see rollback
     */
    var smartTransaction: Boolean = true

    companion object {
        @JvmStatic
        fun with(conn: Connection, block: Connection.() -> Unit) {
            conn.with(block)
        }

        @JvmStatic
        var DefaultUtils: DBUtils? = null
    }

    /**
     * 获取Connection实例<br/>
     * 如果已开启事务则返回事务所用的实例
     *
     * @see startTransaction
     */
    fun getConnection(): Connection = threadLocal.get() ?: creator()

    /**
     * 开启事务
     * @see endTransaction
     */
    @Throws(TransactionException::class)
    fun startTransaction() {
        val oldConn = threadLocal.get()
        if (oldConn != null) {
            if (smartTransaction) {
                rollback()
            } else {
                throw TransactionException("the transaction is not at end")
            }
        }
        creator().apply { threadLocal.set(this);autoCommit = false }
    }


    /**
     * 关闭事务
     * @param rollback 是否rollback,否则commit
     * @see commit
     * @see DBUtils.rollback
     */
    @Throws(TransactionException::class)
    fun endTransaction(rollback: Boolean = false) {
        val conn = threadLocal.get() ?: if (smartTransaction) return
        else throw TransactionException("thr transaction is not started")
        try {
            if (rollback)
                conn.rollback()
            else
                conn.commit()
        } catch (e: SQLException) {
            throw e
        } finally {
            conn.close()
            threadLocal.remove()
        }
    }

    /**
     * commit并关闭事务
     * @see endTransaction
     */
    @Throws(TransactionException::class)
    fun commit() {
        endTransaction(false)
    }

    /**
     * rollback并关闭事务
     * @see endTransaction
     */
    @Throws(TransactionException::class)
    fun rollback() {
        endTransaction(true)
    }

}

@Throws(SQLException::class)
fun Connection.update(sql: String, vararg params: Any): Int =
        prepareStatement(sql).apply {
            setParams(params)
        }.executeUpdate()

@Throws(SQLException::class)
fun Connection.insert(sql: String, vararg parms: Any): Boolean =
        prepareStatement(sql).apply {
            setParams(parms)
        }.execute()


@Throws(SQLException::class)
fun <T> Connection.query(sql: String, resultSetHandler: ResultSetHandler<T>, vararg params: Any): T =
        prepareStatement(sql).apply {
            setParams(params)
        }.executeQuery().let(resultSetHandler::handle)

fun PreparedStatement.setParams(vararg params: Any) {
    for (i in 1..params.size)
        this.setObject(i, params[i - 1])
}

fun Connection.with(block: Connection.() -> Unit) {
    this.autoCommit = false
    try {
        block(this)
        this.commit()
    } catch (e: SQLException) {
        this.rollback()
    } finally {
        this.close()
    }
}

class TransactionException : SQLException {
    constructor(reason: String?, SQLState: String?, vendorCode: Int) : super(reason, SQLState, vendorCode)
    constructor(reason: String?, SQLState: String?) : super(reason, SQLState)
    constructor(reason: String?) : super(reason)
    constructor() : super()
    constructor(cause: Throwable?) : super(cause)
    constructor(reason: String?, cause: Throwable?) : super(reason, cause)
    constructor(reason: String?, sqlState: String?, cause: Throwable?) : super(reason, sqlState, cause)
    constructor(reason: String?, sqlState: String?, vendorCode: Int, cause: Throwable?) : super(reason, sqlState, vendorCode, cause)
}

interface ResultSetHandler<out T> {
    companion object {
        fun <T : Any> getTransFormaterHandler(clazz: Class<out T>, transformManager: TransformManager = TransformManager.DefaultManager): ResultSetHandler<T> =
                object : ResultSetHandler<T> {
                    override fun handle(resultSet: ResultSet): T {
                        resultSet.next()
                        return resultSet.to(clazz)
                    }

                }

        fun <T : Any> getBeanHandler(clazz: Class<T>, transformManager: TransformManager = TransformManager.DefaultManager): ResultSetHandler<T> {
            return object : ResultSetHandler<T> {
                override fun handle(resultSet: ResultSet): T {
                    resultSet.next()
                    return resultSet.toMap().toIData().toBean(clazz = clazz, transformManager = transformManager)
                }
            }
        }

        fun <T : Any> getBeanListHandler(clazz: Class<T>, transformManager: TransformManager = TransformManager.DefaultManager): ResultSetHandler<List<T>> {
            return object : ResultSetHandler<List<T>> {
                override fun handle(resultSet: ResultSet): List<T> {
                    val list = mutableListOf<T>()
                    while (resultSet.next()) {
                        list.add(resultSet.toMap().toIData().toBean(clazz, transformManager))
                    }
                    return list
                }
            }
        }

        fun getMapHandler(): ResultSetHandler<Map<String, Any?>> {
            return object : ResultSetHandler<Map<String, Any?>> {
                override fun handle(resultSet: ResultSet): Map<String, Any?> {
                    resultSet.next()
                    return resultSet.toMap()
                }
            }
        }

        fun getMapListHandler(): ResultSetHandler<List<Map<String, Any?>>> {
            return object : ResultSetHandler<List<Map<String, Any?>>> {
                override fun handle(resultSet: ResultSet): List<Map<String, Any?>> {
                    val list = mutableListOf<Map<String, Any?>>()
                    while (resultSet.next()) {
                        list.add(resultSet.toMap())
                    }
                    return list
                }
            }
        }


        fun getSingleHandler(): ResultSetHandler<Any?> {
            return object : ResultSetHandler<Any?> {
                override fun handle(resultSet: ResultSet): Any? {
                    return resultSet.toMap().values.iterator().takeIf { it.hasNext() }?.next()
                }
            }
        }
    }

    fun handle(resultSet: ResultSet): T
}

operator fun <T> ResultSetHandler<T>.invoke(resultSet: ResultSet): T = handle(resultSet)

val JDBC_TYPEGETTER_MAP: Map<Int, (ResultSet, String) -> Any?> by lazy {
    val jdbcTypeGetters = object {
        fun getBoolean(resultSet: ResultSet, rowID: String): Boolean? = resultSet.getBoolean(rowID)

        fun getByte(resultSet: ResultSet, rowID: String): Byte? = resultSet.getByte(rowID)

        fun getInt(resultSet: ResultSet, rowID: String): Int? = resultSet.getInt(rowID)

        fun getLong(resultSet: ResultSet, rowID: String): Long? = resultSet.getLong(rowID)

        fun getBigInteger(resultSet: ResultSet, rowID: String): BigInteger? = resultSet.getBigDecimal(rowID).toBigInteger()

        fun getFloat(resultSet: ResultSet, rowID: String): Float? = resultSet.getFloat(rowID)

        fun getBigDecimal(resultSet: ResultSet, rowID: String): BigDecimal? = resultSet.getBigDecimal(rowID)

        fun getDouble(resultSet: ResultSet, rowID: String): Double? = resultSet.getDouble(rowID)

        fun getString(resultSet: ResultSet, rowID: String): String? = resultSet.getString(rowID)

        fun getTimestamp(resultSet: ResultSet, rowID: String): Timestamp? = resultSet.getTimestamp(rowID)

        fun getDate(resultSet: ResultSet, rowID: String): Date? = resultSet.getDate(rowID)

        fun getTime(resultSet: ResultSet, rowID: String): Time? = resultSet.getTime(rowID)

        fun getBlob(resultSet: ResultSet, rowID: String): Blob? = resultSet.getBlob(rowID)
        fun getClob(resultSet: ResultSet, rowID: String): Clob? = resultSet.getClob(rowID)
        fun getNull(resultSet: ResultSet, rowID: String): Any? = null
        fun getObject(resultSet: ResultSet, rowID: String): Any? = resultSet.getObject(rowID)
        fun getArray(resultSet: ResultSet, rowID: String): Array? = resultSet.getArray(rowID)
        fun getRef(resultSet: ResultSet, rowID: String): Ref? = resultSet.getRef(rowID)
        fun getRowId(resultSet: ResultSet, rowID: String): RowId? = resultSet.getRowId(rowID)
        fun getNClob(resultSet: ResultSet, rowID: String): NClob? = resultSet.getNClob(rowID)


    }
    mapOf(
            Types.BIT to jdbcTypeGetters::getBoolean,
            Types.TINYINT to jdbcTypeGetters::getByte,
            Types.SMALLINT to jdbcTypeGetters::getInt,
            Types.INTEGER to jdbcTypeGetters::getInt,
            Types.BIGINT to jdbcTypeGetters::getLong,
            Types.FLOAT to jdbcTypeGetters::getFloat,
            Types.REAL to jdbcTypeGetters::getBigDecimal,
            Types.DOUBLE to jdbcTypeGetters::getDouble,
            Types.NUMERIC to jdbcTypeGetters::getBigDecimal,
            Types.DECIMAL to jdbcTypeGetters::getBigDecimal,
            Types.CHAR to jdbcTypeGetters::getString,
            Types.VARCHAR to jdbcTypeGetters::getString,
            Types.LONGVARCHAR to jdbcTypeGetters::getString,
            Types.DATE to jdbcTypeGetters::getDate,
            Types.TIME to jdbcTypeGetters::getTime,
            Types.TIMESTAMP to jdbcTypeGetters::getTimestamp,
            Types.BINARY to jdbcTypeGetters::getBlob,
            Types.VARBINARY to jdbcTypeGetters::getBlob,
            Types.LONGVARBINARY to jdbcTypeGetters::getBlob,
            Types.NULL to jdbcTypeGetters::getNull,
            Types.OTHER to jdbcTypeGetters::getObject,
            Types.JAVA_OBJECT to jdbcTypeGetters::getObject,
            Types.DISTINCT to jdbcTypeGetters::getString,
            Types.STRUCT to jdbcTypeGetters::getString,
            Types.ARRAY to jdbcTypeGetters::getArray,
            Types.BLOB to jdbcTypeGetters::getBlob,
            Types.CLOB to jdbcTypeGetters::getClob,
            Types.REF to jdbcTypeGetters::getRef,
            Types.DATALINK to jdbcTypeGetters::getString,
            Types.BOOLEAN to jdbcTypeGetters::getBoolean,
            Types.ROWID to jdbcTypeGetters::getRowId,
            Types.NCHAR to jdbcTypeGetters::getString,
            Types.NVARCHAR to jdbcTypeGetters::getString,
            Types.LONGNVARCHAR to jdbcTypeGetters::getString,
            Types.NCLOB to jdbcTypeGetters::getNClob,
            Types.SQLXML to jdbcTypeGetters::getString,
            Types.REF_CURSOR to jdbcTypeGetters::getRef,
            Types.TIME_WITH_TIMEZONE to jdbcTypeGetters::getTime,
            Types.TIMESTAMP_WITH_TIMEZONE to jdbcTypeGetters::getTimestamp
    )
}

fun ResultSet.toMap(): MutableMap<String, Any?> {
    val metaData = this.metaData
    val map = mutableMapOf<String, Any?>()
    for (i in 1..metaData.columnCount) {
        val columnName = metaData.getColumnName(i)
        val types = metaData.getColumnType(i)
        map[columnName] = JDBC_TYPEGETTER_MAP[types]?.invoke(this, columnName)
    }
    return map
}
