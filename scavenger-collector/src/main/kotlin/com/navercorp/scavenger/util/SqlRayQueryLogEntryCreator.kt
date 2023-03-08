package com.navercorp.scavenger.util

import com.github.vertical_blank.sqlformatter.SqlFormatter
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.logging.QueryLogEntryCreator
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.Date
import java.util.UUID
import kotlin.math.roundToInt

class SqlRayQueryLogEntryCreator : QueryLogEntryCreator {
    private var dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    private var dateFormatter = DateTimeFormatter.ISO_DATE
    private var timeFormatter = DateTimeFormatter.ISO_TIME

    override fun getLogEntry(
        execInfo: ExecutionInfo,
        queryInfoList: List<QueryInfo>,
        writeDataSourceName: Boolean,
        writeConnectionId: Boolean
    ): String {
        val result = StringBuilder()
        for (queryInfo in queryInfoList) {
            val query = queryInfo.query
            val params = queryInfo.parametersList.flatten()
                .map { if (it.method.name == "setNull") null else it.args[1] }

            val sql = fillParameters(query, params)
            result.append(SqlFormatter.format(sql)).append("\n")
        }
        return result.toString()
    }

    private fun fillParameters(statement: String, params: List<Any?>): String {
        val completedSqlBuilder = StringBuilder((statement.length * 1.2f).roundToInt())
        var index: Int
        var prevIndex = 0

        for (arg in params) {
            index = statement.indexOf("?", prevIndex)
            if (index == -1) {
                break
            }
            try {
                completedSqlBuilder.append(statement, prevIndex, index)
                when (arg) {
                    null -> {
                        completedSqlBuilder.append("[NULL]")
                    }

                    is String -> {
                        completedSqlBuilder.append('\'')
                            .append(arg.toString().replace("'", "''"))
                            .append('\'')
                    }

                    is Timestamp -> {
                        completedSqlBuilder.append("[TIMESTAMP] '")
                            .append(dateTimeFormatter.format(arg.toInstant().atZone(ZoneId.systemDefault())))
                            .append('\'')
                    }

                    is Date -> {
                        completedSqlBuilder.append("[DATE] '")
                            .append(dateTimeFormatter.format(arg.toInstant().atZone(ZoneId.systemDefault())))
                            .append('\'')
                    }

                    is Instant -> {
                        completedSqlBuilder.append("[INSTANT] '")
                            .append(dateTimeFormatter.format(arg.atZone(ZoneId.systemDefault())))
                            .append('\'')
                    }

                    is LocalDateTime -> {
                        completedSqlBuilder.append("[LOCAL_DATE_TIME] '")
                            .append(dateTimeFormatter.format(arg as LocalDateTime?))
                            .append('\'')
                    }

                    is ZonedDateTime -> {
                        completedSqlBuilder.append("[ZONED_DATE_TIME] '")
                            .append(dateTimeFormatter.format(arg as ZonedDateTime?))
                            .append('\'')
                    }

                    is LocalDate -> {
                        completedSqlBuilder.append("[LOCAL_DATE] '")
                            .append(dateFormatter.format(arg as LocalDate?))
                            .append('\'')
                    }

                    is LocalTime -> {
                        completedSqlBuilder.append("[LOCAL_TIME] '")
                            .append(timeFormatter.format(arg as LocalTime?))
                            .append('\'')
                    }

                    is Number -> {
                        completedSqlBuilder.append(arg.toString())
                    }

                    is ByteArray -> {
                        var value = Arrays.toString(arg)
                        if (arg.size == 16) {
                            try {
                                value = UUID.nameUUIDFromBytes(arg).toString()
                            } catch (ignored: Exception) {
                                // ignore
                            }
                        }
                        completedSqlBuilder.append('\'').append(value).append('\'')
                    }

                    else -> {
                        completedSqlBuilder.append('\'').append(arg.toString()).append('\'')
                    }
                }
            } catch (e: Exception) {
                completedSqlBuilder.append("#BINDING-ERROR")
            }
            prevIndex = index + 1
        }

        if (prevIndex != statement.length) {
            completedSqlBuilder.append(statement.substring(prevIndex))
        }
        return completedSqlBuilder.toString()
    }
}
