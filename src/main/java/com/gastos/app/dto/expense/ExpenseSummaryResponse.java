package com.gastos.app.dto.expense;

import com.gastos.app.model.Currency;
import com.gastos.app.model.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryResponse {
    private Map<Currency, BigDecimal> totalsByCurrency;
    private Map<Currency, BigDecimal> paidByCurrency;
    private Map<Currency, BigDecimal> pendingByCurrency;
    private Map<ExpenseType, BigDecimal> totalsByType;
    private List<CategoryBreakdownItem> categoryBreakdown;
    private long totalExpensesCount;
    private long paidCount;
    private long pendingCount;
    private BigDecimal previousMonthTotalArs;
    private int month;
    private int year;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdownItem {
        private Long categoryId;
        private String categoryName;
        private String color;
        private String icon;
        private BigDecimal totalArs;
        private BigDecimal totalUsd;
        private long count;
        private double percentageArs;
    }
}
