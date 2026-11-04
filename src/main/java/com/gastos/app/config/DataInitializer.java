package com.gastos.app.config;

import com.gastos.app.model.Category;
import com.gastos.app.model.ExpenseType;
import com.gastos.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("Inicializando categorías predeterminadas del sistema...");

            List<Category> defaultCategories = List.of(
                    Category.builder()
                            .name("Supermercado y Alimentos")
                            .icon("shopping-cart")
                            .color("#10B981") // Green
                            .suggestedType(ExpenseType.GROCERY)
                            .isSystemDefault(true)
                            .build(),
                    Category.builder()
                            .name("Servicios del Hogar (Luz, Agua, Gas, Internet)")
                            .icon("zap")
                            .color("#F59E0B") // Amber
                            .suggestedType(ExpenseType.SERVICE)
                            .isSystemDefault(true)
                            .build(),
                    Category.builder()
                            .name("Suscripciones Digitales")
                            .icon("tv")
                            .color("#8B5CF6") // Purple
                            .suggestedType(ExpenseType.SUBSCRIPTION)
                            .isSystemDefault(true)
                            .build(),
                    Category.builder()
                            .name("Cuotas de Tarjeta y Préstamos")
                            .icon("credit-card")
                            .color("#EF4444") // Red
                            .suggestedType(ExpenseType.INSTALLMENT)
                            .isSystemDefault(true)
                            .build(),
                    Category.builder()
                            .name("Salidas, Bares y Restaurantes")
                            .icon("utensils")
                            .color("#EC4899") // Pink
                            .suggestedType(ExpenseType.LEISURE)
                            .isSystemDefault(true)
                            .build(),
                    Category.builder()
                            .name("Transporte y Combustible")
                            .icon("car")
                            .color("#3B82F6") // Blue
                            .suggestedType(ExpenseType.TRANSPORT)
                            .isSystemDefault(true)
                            .build(),
                    Category.builder()
                            .name("Salud y Farmacia")
                            .icon("heart-pulse")
                            .color("#06B6D4") // Cyan
                            .suggestedType(ExpenseType.HEALTH)
                            .isSystemDefault(true)
                            .build(),
                    Category.builder()
                            .name("Gastos Diarios / Varios")
                            .icon("coffee")
                            .color("#64748B") // Slate
                            .suggestedType(ExpenseType.DAILY)
                            .isSystemDefault(true)
                            .build()
            );

            categoryRepository.saveAll(defaultCategories);
            log.info("{} categorías inicializadas correctamente.", defaultCategories.size());
        }
    }
}
