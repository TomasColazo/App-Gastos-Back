package com.gastos.app.service;

import com.gastos.app.dto.expense.ExpenseRequest;
import com.gastos.app.dto.expense.ExpenseResponse;
import com.gastos.app.dto.expense.ExpenseSummaryResponse;
import com.gastos.app.exception.ResourceNotFoundException;
import com.gastos.app.model.*;
import com.gastos.app.repository.ExpenseRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;

    public List<ExpenseResponse> getExpenses(
            User user,
            Integer year,
            Integer month,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            ExpenseType type,
            ExpenseStatus status,
            Currency currency,
            PaymentMethod paymentMethod,
            String search
    ) {
        Specification<Expense> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), user.getId()));

            if (year != null && month != null) {
                YearMonth ym = YearMonth.of(year, month);
                predicates.add(cb.between(root.get("date"), ym.atDay(1), ym.atEndOfMonth()));
            } else if (year != null) {
                LocalDate start = LocalDate.of(year, 1, 1);
                LocalDate end = LocalDate.of(year, 12, 31);
                predicates.add(cb.between(root.get("date"), start, end));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("expenseType"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (currency != null) {
                predicates.add(cb.equal(root.get("currency"), currency));
            }
            if (paymentMethod != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), paymentMethod));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern);
                Predicate refMatch = cb.like(cb.lower(cb.coalesce(root.get("referenceCode"), "")), pattern);
                Predicate periodMatch = cb.like(cb.lower(cb.coalesce(root.get("period"), "")), pattern);
                predicates.add(cb.or(titleMatch, descMatch, refMatch, periodMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id"));
        return expenseRepository.findAll(spec, sort)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
                .status(request.getStatus() != null ? request.getStatus() : ExpenseStatus.PAID)
                .category(category)
                .installmentNumber(request.getInstallmentNumber())
                .totalInstallments(request.getTotalInstallments())
                .isRecurring(Boolean.TRUE.equals(request.getIsRecurring()))
                .recurrenceDay(request.getRecurrenceDay())
                .receiptUrl(request.getReceiptUrl())
                .referenceCode(request.getReferenceCode())
                .period(request.getPeriod())
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
        if (request.getStatus() != null) {
            expense.setStatus(request.getStatus());
        }
        expense.setCategory(category);
        expense.setInstallmentNumber(request.getInstallmentNumber());
        expense.setTotalInstallments(request.getTotalInstallments());
        expense.setRecurring(Boolean.TRUE.equals(request.getIsRecurring()));
        expense.setRecurrenceDay(request.getRecurrenceDay());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense.setReferenceCode(request.getReferenceCode());
        expense.setPeriod(request.getPeriod());

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
        Map<Currency, BigDecimal> paidByCurrency = new HashMap<>();
        Map<Currency, BigDecimal> pendingByCurrency = new HashMap<>();
        Map<ExpenseType, BigDecimal> totalsByType = new HashMap<>();
        Map<Long, ExpenseSummaryResponse.CategoryBreakdownItem> categoryMap = new HashMap<>();

        long paidCount = 0;
        long pendingCount = 0;

        for (Expense exp : expenses) {
            Currency curr = exp.getCurrency();
            BigDecimal amt = exp.getAmount();

            totalsByCurrency.merge(curr, amt, BigDecimal::add);

            if (exp.getStatus() == ExpenseStatus.PENDING) {
                pendingByCurrency.merge(curr, amt, BigDecimal::add);
                pendingCount++;
            } else {
                paidByCurrency.merge(curr, amt, BigDecimal::add);
                paidCount++;
            }

            if (curr == Currency.ARS) {
                totalsByType.merge(exp.getExpenseType(), amt, BigDecimal::add);
            }

            Category cat = exp.getCategory();
            ExpenseSummaryResponse.CategoryBreakdownItem item = categoryMap.computeIfAbsent(
                    cat.getId(),
                    id -> ExpenseSummaryResponse.CategoryBreakdownItem.builder()
                            .categoryId(cat.getId())
                            .categoryName(cat.getName())
                            .color(cat.getColor())
                            .icon(cat.getIcon())
                            .totalArs(BigDecimal.ZERO)
                            .totalUsd(BigDecimal.ZERO)
                            .count(0)
                            .percentageArs(0.0)
                            .build()
            );

            item.setCount(item.getCount() + 1);
            if (curr == Currency.USD) {
                item.setTotalUsd(item.getTotalUsd().add(amt));
            } else {
                item.setTotalArs(item.getTotalArs().add(amt));
            }
        }

        BigDecimal totalArs = totalsByCurrency.getOrDefault(Currency.ARS, BigDecimal.ZERO);
        List<ExpenseSummaryResponse.CategoryBreakdownItem> breakdown = categoryMap.values().stream()
                .peek(item -> {
                    if (totalArs.compareTo(BigDecimal.ZERO) > 0 && item.getTotalArs().compareTo(BigDecimal.ZERO) > 0) {
                        double pct = item.getTotalArs()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalArs, 1, RoundingMode.HALF_UP)
                                .doubleValue();
                        item.setPercentageArs(pct);
                    }
                })
                .sorted(Comparator.comparing(ExpenseSummaryResponse.CategoryBreakdownItem::getTotalArs).reversed())
                .collect(Collectors.toList());

        // Mes anterior para comparativa
        YearMonth prevYm = ym.minusMonths(1);
        List<Expense> prevExpenses = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                user.getId(), prevYm.atDay(1), prevYm.atEndOfMonth()
        );
        BigDecimal prevTotalArs = prevExpenses.stream()
                .filter(e -> e.getCurrency() == Currency.ARS)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ExpenseSummaryResponse.builder()
                .totalsByCurrency(totalsByCurrency)
                .paidByCurrency(paidByCurrency)
                .pendingByCurrency(pendingByCurrency)
                .totalsByType(totalsByType)
                .categoryBreakdown(breakdown)
                .totalExpensesCount(expenses.size())
                .paidCount(paidCount)
                .pendingCount(pendingCount)
                .previousMonthTotalArs(prevTotalArs)
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

    public ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .date(expense.getDate())
                .expenseType(expense.getExpenseType())
                .paymentMethod(expense.getPaymentMethod())
                .status(expense.getStatus())
                .category(categoryService.mapToResponse(expense.getCategory()))
                .installmentNumber(expense.getInstallmentNumber())
                .totalInstallments(expense.getTotalInstallments())
                .isRecurring(expense.isRecurring())
                .recurrenceDay(expense.getRecurrenceDay())
                .receiptUrl(expense.getReceiptUrl())
                .referenceCode(expense.getReferenceCode())
                .period(expense.getPeriod())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
