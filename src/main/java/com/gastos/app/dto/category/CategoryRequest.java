package com.gastos.app.dto.category;

import com.gastos.app.model.ExpenseType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String name;

    private String icon;

    private String color;

    private ExpenseType suggestedType;
}
