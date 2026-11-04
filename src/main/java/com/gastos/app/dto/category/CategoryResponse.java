package com.gastos.app.dto.category;

import com.gastos.app.model.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private ExpenseType suggestedType;
    private boolean isSystemDefault;
}
