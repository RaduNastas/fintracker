package com.fintracker.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    public record QueryParams(String sql, List<Object> params) {
    }

    public static QueryParams buildTransactionFilterQuery(String type, String category, LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder sql = new StringBuilder("SELECT id, type, amount, category, date FROM transactions WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (category != null) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (startDate != null) {
            sql.append(" AND date >= ?");
            params.add(Timestamp.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND date <= ?");
            params.add(Timestamp.valueOf(endDate));
        }

        return new QueryParams(sql.toString(), params);
    }

    public static QueryParams buildSelectQuery(String tableName, List<FilterCondition> conditions) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        for (FilterCondition condition : conditions) {
            sql.append(" AND ").append(condition.column()).append(" ").append(condition.operator()).append(" ?");
            params.add(condition.value());
        }

        return new QueryParams(sql.toString(), params);
    }

    public record FilterCondition(String column, String operator, Object value) {
    }

}