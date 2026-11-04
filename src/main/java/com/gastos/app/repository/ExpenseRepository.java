package com.gastos.app.repository;

import com.gastos.app.model.Expense;
import com.gastos.app.model.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    List<Expense> findByUserIdAndExpenseTypeOrderByDateDesc(Long userId, ExpenseType expenseType);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.isRecurring = true")
    List<Expense> findRecurringExpensesByUser(@Param("userId") Long userId);
}
