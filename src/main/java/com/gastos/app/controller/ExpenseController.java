package com.gastos.app.controller;

import com.gastos.app.dto.expense.ExpenseRequest;
import com.gastos.app.dto.expense.ExpenseResponse;
import com.gastos.app.dto.expense.ExpenseSummaryResponse;
import com.gastos.app.model.ExpenseType;
import com.gastos.app.model.User;
import com.gastos.app.service.AuthService;
import com.gastos.app.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) ExpenseType type,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(expenseService.getExpenses(user, year, month, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(expenseService.getExpenseById(id, user));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(expenseService.updateExpense(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        expenseService.deleteExpense(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponse> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        int currentYear = year != null ? year : LocalDate.now().getYear();
        int currentMonth = month != null ? month : LocalDate.now().getMonthValue();
        return ResponseEntity.ok(expenseService.getMonthlySummary(user, currentYear, currentMonth));
    }
}
