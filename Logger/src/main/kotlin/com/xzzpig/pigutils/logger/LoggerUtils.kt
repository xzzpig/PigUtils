@file:JvmName("LoggerUtils")

package com.xzzpig.pigutils.logger

import java.io.File
import java.io.Writer
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

@LoggerConfigMarker
class LoggerConfig {
    enum class LoggerMergeType {
        /**
         * 清除原来的配置，使用新配置
         */
        RESET,
        /**
         * 保留原来的，将新的填充进去，已存在的替换
         */
        REPLACE,
        /**
         * 保留原来的，将未配置的填充进去
         */
        FILL
    }

    interface ReplaceHandler<in T> {
        /**
         * 测试该值是否要被替换
         * @return true if value needed to be replaced
         */
        fun test(oldValue: T, newValue: T): Boolean
    }

    /**
     * 将 loggerConfig合并到this中
     * @see LoggerMergeType
     */
    fun merge(type: LoggerMergeType, loggerConfig: LoggerConfig) {
        when (type) {
            LoggerMergeType.RESET -> reset(loggerConfig)
            LoggerMergeType.REPLACE -> replace(loggerConfig)
            LoggerMergeType.FILL -> fill(loggerConfig)
        }
    }

    infix fun reset(loggerConfig: LoggerConfig) {
        loggerMap.clear()
        formatterMap.clear()
        targetMap.clear()
        loggerMap.putAll(loggerConfig.loggerMap)
        formatterMap.putAll(loggerConfig.formatterMap)
        targetMap.putAll(loggerConfig.targetMap)
    }

    fun replace(loggerConfig: LoggerConfig, loggerHandler: ReplaceHandler<Logger>? = null, formatterHandler: ReplaceHandler<LogFormatter>? = null, targetHandler: ReplaceHandler<LogTarget>? = null) {
        loggerConfig.formatterMap.forEach { name, formatter ->
            if (!formatterMap.containsKey(name)) setFormatter(name, formatter)
            else if (formatterHandler?.test(getFormatter(name).subFormatter, formatter) != false) setFormatter(name, formatter)
        }
        loggerConfig.targetMap.forEach { name, target ->
            if (!targetMap.containsKey(name)) setTarget(name, target)
            else if (targetHandler?.test(getTarget(name).subTarget, target) != false) setTarget(name, target)
        }
        loggerConfig.loggerMap.forEach { name, lg ->
            if (!loggerMap.containsKey(name)) {
                setLogger(lg)
                rebase(lg)
            } else if (loggerHandler?.test(getLogger(name), lg) != false) {
                setLogger(lg)
                rebase(lg)
            }
        }
    }

    infix fun fill(loggerConfig: LoggerConfig) {
        loggerConfig.formatterMap.forEach { name, formatter ->
            if (!formatterMap.containsKey(name)) setFormatter(name, formatter)
            else if (getFormatter(name).subFormatter == LogFormatter.Companion.FakeLogFormatter) setFormatter(name, formatter)
        }
        loggerConfig.targetMap.forEach { name, target ->
            if (!targetMap.containsKey(name)) setTarget(name, target)
            else if (getTarget(name).subTarget == LogTarget.Companion.FakeLogTarget) setTarget(name, target)
        }
        loggerConfig.loggerMap.forEach { name, lg ->
            if (!loggerMap.containsKey(name)) setLogger(lg)
            else if (getLogger(name).isFakeLogger()) setLogger(lg)
        }
    }

    private val loggerMap = mutableMapOf<String, Logger>()

    private val formatterMap = mutableMapOf<String, LogFormatterWrapper>()

    private val targetMap = mutableMapOf<String, LogTargetWrapper>()

    init {
        setTarget("console", ConsoleTarget)
    }

    fun getLogger(name: String = "default") = loggerMap.getOrPut(name) { Logger(name) }

    fun setLogger(logger: Logger) {
        val lg = getLogger(logger.name)
        lg.formatter = logger.formatter
        lg.targets.clear()
        lg.targets = logger.targets
        lg.level = logger.level
        lg.baseOn = null
        logger.baseOn?.let {
            lg.baseOn = getLogger(it.name)
        }
    }

    fun rebase(logger: Logger) {
        rebase(logger, loggerMap.values)
    }

    fun rebase(logger: Logger, loggersToRebase: Collection<Logger>) {
        val name = logger.name
        loggersToRebase.parallelStream().filter { it.baseOn?.name == name }.forEach { it.baseOn = logger }
        loggersToRebase.parallelStream().filter { it is GroupLogger }.map { it as GroupLogger }.forEach { rebase(logger, it.subLoggers) }
    }

    fun getFormatter(name: String) = formatterMap.getOrPut(name.toLowerCase()) { LogFormatterWrapper(LogFormatter.Companion.FakeLogFormatter) }

    tailrec fun setFormatter(name: String, formatter: LogFormatter) {
        if (formatter !is LogFormatterWrapper) getFormatter(name).subFormatter = formatter
        else setFormatter(name, formatter.subFormatter)
    }

    fun getTarget(name: String) = targetMap.getOrPut(name.toLowerCase()) { LogTargetWrapper(LogTarget.Companion.FakeLogTarget) }

    tailrec fun setTarget(name: String, target: LogTarget) {
        if (target !is LogTargetWrapper) getTarget(name).subTarget = target
        else setTarget(name, target.subTarget)
    }

    inner class LoggersBuilder {
        fun default(block: LoggerBuilder.() -> Unit) {
            logger("default", block)
        }

        infix fun String.to(block: LoggerBuilder.() -> Unit) {
            logger(this, block)
        }

        operator fun String.compareTo(block: LoggerBuilder.() -> Unit): Int {
            this to block
            return 0
        }

        fun logger(name: String, block: LoggerBuilder.() -> Unit) {
            this@LoggerConfig.setLogger(LoggerBuilder(name).apply(block).build())
        }

        operator fun String.plusAssign(names: Array<String>) {
            val logger = this@LoggerConfig.getLogger(this)
            if (logger is GroupLogger) {
                for (name in names) {
                    logger.addSubLogger(this@LoggerConfig.getLogger(name))
                }
            } else {
                GroupLogger(this).apply { addSubLogger(logger) }.apply {
                    for (name in names) {
                        addSubLogger(getLogger(name))
                    }
                }.apply { loggerMap[name] = this }.apply { rebase(this) }
            }
        }

        operator fun String.plusAssign(name: String) {
            this += arrayOf(name)
        }

    }

    @LoggerConfigMarker
    inner class LogFormattersBuilder {
        var default: LogFormatter = LogFormatter.Companion.FakeLogFormatter

        fun formatter(name: String, formatter: LogFormatter) {
            this@LoggerConfig.setFormatter(name, formatter)
        }

        infix fun String.to(formatter: LogFormatter) {
            formatter(this, formatter)
        }

        operator fun String.compareTo(formatter: LogFormatter): Int {
            formatter(this, formatter)
            return 0
        }
    }

    @LoggerConfigMarker
    inner class LogTargetsBuilder {
        var default: LogTarget = LogTarget.Companion.FakeLogTarget

        fun target(name: String, target: LogTarget) {
            this@LoggerConfig.setTarget(name, target)
        }

        infix fun String.to(target: LogTarget) {
            target(this, target)
        }

        operator fun String.compareTo(target: LogTarget): Int {
            target(this, target)
            return 0
        }
    }

    @LoggerConfigMarker
    inner class LoggerBuilder(val name: String) {
        var formatter: LogFormatter = LogFormatter.Companion.FakeLogFormatter
        val targets = mutableSetOf<LogTarget>()
        private var baseOn: Logger? = null

        var level = 0

        fun level(lv: LogLevel) {
            level = lv.level
        }

        fun level(lv: Int) {
            level = lv
        }

        inline fun level(block: LogLevel.Companion.() -> LogLevel) {
            level = block(LogLevel.Companion).level
        }

        infix fun LogFormatter.use(name: String) {
            formatter = this@LoggerConfig.getFormatter(name)
        }

        operator fun MutableSet<LogTarget>.plusAssign(targetName: String) {
            targets.add(this@LoggerConfig.getTarget(targetName))
        }

        fun baseOn(name: String) {
            baseOn = this@LoggerConfig.getLogger(name)
        }

        fun build(): Logger = Logger(name, formatter, targets, level, baseOn)
    }

    inline fun loggers(block: LoggersBuilder.() -> Unit) {
        LoggersBuilder().apply(block)
    }

    inline fun formatters(block: LogFormattersBuilder.() -> Unit) {
        LogFormattersBuilder().apply(block).apply {
            this@LoggerConfig.setFormatter("default", default)
        }
    }

    inline fun targets(block: LogTargetsBuilder.() -> Unit) {
        LogTargetsBuilder().apply(block).apply {
            this@LoggerConfig.setTarget("default", default)
        }
    }
}

private fun Logger.isFakeLogger(): Boolean {
    if (formatter != LogFormatter.Companion.FakeLogFormatter) return false
    if (targets.size == 0) return true
    if (targets.size == 1 && targets.first() == LogTarget.Companion.FakeLogTarget) return true
    return false
}

inline fun loggerConfig(block: LoggerConfig.() -> Unit): LoggerConfig = LoggerConfig().apply(block)

val DEFAULT_CONFIG = loggerConfig {
    loggers {
        default {
            level { INFO }
            targets += console()
            formatter = format { "[$datetime-$level]$log$endln" }
        }
    }
}

open class Logger(val name: String, internal var formatter: LogFormatter = LogFormatter.Companion.FakeLogFormatter, internal var targets: MutableSet<LogTarget> = mutableSetOf(), var level: Int = LogLevel.INFO.level, var baseOn: Logger? = null) {

    companion object {
        @JvmStatic
        fun getLogger(name: String) = DEFAULT_CONFIG.getLogger(name)

        @JvmStatic
        fun getLogger() = DEFAULT_CONFIG.getLogger()
    }

    fun log(level: LogLevel, vararg logs: String) {
        if (level.level < this.level) return
        for (log in logs) {
            log(LogContext(this, level, log))
        }
    }

    fun debug(vararg logs: String) {
        log(LogLevel.DEBUG, *logs)
    }

    fun info(vararg logs: String) {
        log(LogLevel.INFO, *logs)
    }

    fun warn(vararg logs: String) {
        log(LogLevel.WARN, *logs)
    }

    fun error(vararg logs: String) {
        log(LogLevel.ERROR, *logs)
    }

    fun fatal(vararg logs: String) {
        log(LogLevel.FATAL, *logs)
    }

    open fun log(logContext: LogContext) {
        val level = logContext.logLevel
        if (level.level < this.level) return
        if (formatter == LogFormatter.Companion.FakeLogFormatter && baseOn != null) try {
            formatter = baseOn!!.formatter
        } finally {

        }
        val ts = if (baseOn != null && (targets.size == 0 || (targets.size == 1 && targets.first() == LogTarget.Companion.FakeLogTarget))) baseOn!!.targets else targets
        formatter.format(logContext).let {
            for (target in ts) {
                target.log(it, logContext)
            }
        }
    }
}

class GroupLogger(name: String) : Logger(name) {

    val subLoggers = mutableSetOf<Logger>()

    fun addSubLogger(subLogger: Logger) {
        subLoggers.add(subLogger)
    }

    override fun log(logContext: LogContext) {
        subLoggers.forEach { it.log(logContext) }
    }
}

data class LogLevel(val name: String, val level: Int) {
    companion object {
        @JvmStatic
        val ALL = LogLevel("ALL", Int.MIN_VALUE)
        @JvmStatic
        val DEBUG = LogLevel("DEBUG", -100)
        @JvmStatic
        val INFO = LogLevel("INFO", 0)
        @JvmStatic
        val WARN = LogLevel("WARN", 100)
        @JvmStatic
        val ERROR = LogLevel("ERROR", 200)
        @JvmStatic
        val FATAL = LogLevel("FATAL", 300)
        @JvmStatic
        val OFF = LogLevel("OFF", Int.MIN_VALUE)
    }
}

interface LogFormatter {
    companion object {
        object FakeLogFormatter : LogFormatter {
            override fun format(logContext: LogContext): String = logContext.log
        }
    }

    fun format(logContext: LogContext): String
}

class StringBeanLogFormatter(val handler: LoggerStringBean.() -> String) : LogFormatter {
    override fun format(logContext: LogContext): String = LoggerStringBean(logContext.logLevel.name, logContext.log, logContext.logger.name).handler()

}

class LogFormatterWrapper(internal var subFormatter: LogFormatter) : LogFormatter {
    override fun format(logContext: LogContext): String = subFormatter.format(logContext)
}

interface LogTarget {
    companion object {
        object FakeLogTarget : LogTarget {
            override fun log(str: String, logContext: LogContext) {}
        }
    }

    fun log(str: String, logContext: LogContext)
}

open class WriterLogTarget(val writer: Writer) : LogTarget {
    override fun log(str: String, logContext: LogContext) {
        try {
            writer.write(str)
        } finally {
        }
    }
}

open class SimpleFileTarget(val file: File, val charset: Charset = Charsets.UTF_8) : LogTarget {
    init {
        if (!file.exists()) file.createNewFile()
    }

    override fun log(str: String, logContext: LogContext) {
        try {
            file.appendText(str, charset)
        } finally {
        }
    }
}

object ConsoleTarget : LogTarget {
    override fun log(str: String, logContext: LogContext) {
        if (logContext.logLevel.level >= LogLevel.ERROR.level) {
            System.err.print(str)
        } else {
            System.out.print(str)
        }
    }

}

class LogTargetWrapper(var subTarget: LogTarget) : LogTarget {
    override fun log(str: String, logContext: LogContext) {
        subTarget.log(str, logContext)
    }
}

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class LoggerConfigMarker

fun consoleTarget(): LogTarget = ConsoleTarget
fun fileTarget(file: String, charset: Charset = Charsets.UTF_8): LogTarget = SimpleFileTarget(File(file), charset)

fun LoggerConfig.LoggerBuilder.file(file: String) = fileTarget(file)
fun LoggerConfig.LoggerBuilder.console() = consoleTarget()

fun LoggerConfig.LogTargetsBuilder.file(file: String) = fileTarget(file)
fun LoggerConfig.LogTargetsBuilder.console() = consoleTarget()

fun stringBeanFormatter(block: LoggerStringBean.() -> String): LogFormatter = StringBeanLogFormatter(block)

fun LoggerConfig.LogFormattersBuilder.format(block: LoggerStringBean.() -> String) = stringBeanFormatter(block)
fun LoggerConfig.LoggerBuilder.format(block: LoggerStringBean.() -> String) = stringBeanFormatter(block)

data class LogContext(val logger: Logger, val logLevel: LogLevel, val log: String)

data class LoggerStringBean(val level: String, val log: String, val loggerName: String)

val LoggerStringBean.datetime: String get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis()))
val LoggerStringBean.date: String get() = SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))
val LoggerStringBean.time: String get() = SimpleDateFormat("HH:mm:ss").format(Date(System.currentTimeMillis()))
val LoggerStringBean.endln: String get() = System.lineSeparator()
