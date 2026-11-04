package com.gastos.app.dto.expense;

import com.gastos.app.dto.category.CategoryResponse;
import com.gastos.app.model.Currency;
import com.gastos.app.model.ExpenseType;
import com.gastos.app.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private Currency currency;
    private LocalDate date;
    private ExpenseType expenseType;
    private PaymentMethod paymentMethod;
    private CategoryResponse category;
    private Integer installmentNumber;
    private Integer totalInstallments;
    private boolean isRecurring;
    private Integer recurrenceDay;
    private LocalDateTime createdAt;
}
