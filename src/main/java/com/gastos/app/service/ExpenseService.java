package com.gastos.app.service;

import com.gastos.app.dto.expense.ExpenseRequest;
import com.gastos.app.dto.expense.ExpenseResponse;
import com.gastos.app.dto.expense.ExpenseSummaryResponse;
import com.gastos.app.exception.ResourceNotFoundException;
import com.gastos.app.model.*;
import com.gastos.app.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;

    public List<ExpenseResponse> getExpenses(User user, Integer year, Integer month, ExpenseType type) {
        List<Expense> list;

        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            list = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(user.getId(), start, end);
        } else if (type != null) {
            list = expenseRepository.findByUserIdAndExpenseTypeOrderByDateDesc(user.getId(), type);
        } else {
            list = expenseRepository.findByUserIdOrderByDateDesc(user.getId());
        }

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ExpenseResponse getExpenseById(Long id, User user) {
        Expense expense = findUserExpense(id, user);
        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, User user) {
        Category category = categoryService.getCategoryEntity(request.getCategoryId(), user);

        Expense expense = Expense.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : Currency.valueOf(user.getDefaultCurrency()))
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .expenseType(request.getExpenseType() != null ? request.getExpenseType() : ExpenseType.DAILY)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.DEBIT_CARD)
                .category(category)
                .installmentNumber(request.getInstallmentNumber())
                .totalInstallments(request.getTotalInstallments())
                .isRecurring(Boolean.TRUE.equals(request.getIsRecurring()))
                .recurrenceDay(request.getRecurrenceDay())
                .user(user)
                .build();

        Expense saved = expenseRepository.save(expense);
        return mapToResponse(saved);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request, User user) {
        Expense expense = findUserExpense(id, user);
        Category category = categoryService.getCategoryEntity(request.getCategoryId(), user);

        expense.setTitle(request.getTitle().trim());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        if (request.getCurrency() != null) {
            expense.setCurrency(request.getCurrency());
        }
        if (request.getDate() != null) {
            expense.setDate(request.getDate());
        }
        if (request.getExpenseType() != null) {
            expense.setExpenseType(request.getExpenseType());
        }
        if (request.getPaymentMethod() != null) {
            expense.setPaymentMethod(request.getPaymentMethod());
        }
        expense.setCategory(category);
        expense.setInstallmentNumber(request.getInstallmentNumber());
        expense.setTotalInstallments(request.getTotalInstallments());
        expense.setRecurring(Boolean.TRUE.equals(request.getIsRecurring()));
        expense.setRecurrenceDay(request.getRecurrenceDay());

        Expense updated = expenseRepository.save(expense);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteExpense(Long id, User user) {
        Expense expense = findUserExpense(id, user);
        expenseRepository.delete(expense);
    }

    public ExpenseSummaryResponse getMonthlySummary(User user, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(user.getId(), start, end);

        Map<Currency, BigDecimal> totalsByCurrency = new HashMap<>();
        Map<ExpenseType, BigDecimal> totalsByType = new HashMap<>();

        for (Expense exp : expenses) {
            // Totales por divisa
            totalsByCurrency.merge(exp.getCurrency(), exp.getAmount(), BigDecimal::add);

            // Totales por tipo de gasto
            totalsByType.merge(exp.getExpenseType(), exp.getAmount(), BigDecimal::add);
        }

        return ExpenseSummaryResponse.builder()
                .totalsByCurrency(totalsByCurrency)
                .totalsByType(totalsByType)
                .totalExpensesCount(expenses.size())
                .month(month)
                .year(year)
                .build();
    }

    private Expense findUserExpense(Long id, User user) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado con id: " + id));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Gasto no encontrado o acceso denegado");
        }
        return expense;
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .date(expense.getDate())
                .expenseType(expense.getExpenseType())
                .paymentMethod(expense.getPaymentMethod())
                .category(categoryService.mapToResponse(expense.getCategory()))
                .installmentNumber(expense.getInstallmentNumber())
                .totalInstallments(expense.getTotalInstallments())
                .isRecurring(expense.isRecurring())
                .recurrenceDay(expense.getRecurrenceDay())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
