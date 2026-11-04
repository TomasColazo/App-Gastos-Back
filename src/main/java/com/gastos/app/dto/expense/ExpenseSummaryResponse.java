package com.gastos.app.dto.expense;

import com.gastos.app.model.Currency;
import com.gastos.app.model.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryResponse {
    private Map<Currency, BigDecimal> totalsByCurrency;
    private Map<ExpenseType, BigDecimal> totalsByType;
    private long totalExpensesCount;
    private int month;
    private int year;
}
