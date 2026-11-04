package com.gastos.app.dto.expense;

import com.gastos.app.model.Currency;
import com.gastos.app.model.ExpenseStatus;
import com.gastos.app.model.ExpenseType;
import com.gastos.app.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotBlank(message = "El concepto o título es obligatorio")
    private String title;

    private String description;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    private Currency currency = Currency.ARS;

    private LocalDate date;

    private ExpenseType expenseType = ExpenseType.DAILY;

    private PaymentMethod paymentMethod = PaymentMethod.DEBIT_CARD;

    private ExpenseStatus status = ExpenseStatus.PAID;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    // Cuotas
    private Integer installmentNumber;
    private Integer totalInstallments;

    // Recurrente / Servicios / Suscripciones
    private Boolean isRecurring = false;
    private Integer recurrenceDay;

    // Comprobante y cuenta de servicio
    private String receiptUrl;
    private String referenceCode;
    private String period;
}
