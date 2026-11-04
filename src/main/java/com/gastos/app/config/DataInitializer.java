package com.gastos.app.config;

import com.gastos.app.model.*;
import com.gastos.app.repository.CategoryRepository;
import com.gastos.app.repository.ExpenseRepository;
import com.gastos.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Crear usuario por defecto si no existe
        User user = userRepository.findByEmail("tomeix13@gmail.com")
                .orElseGet(() -> {
                    log.info("Creando usuario inicial: tomeix13@gmail.com");
                    User newUser = User.builder()
                            .name("Tomas Colazo")
                            .email("tomeix13@gmail.com")
                            .password(passwordEncoder.encode("admin123"))
                            .role(Role.ADMIN)
                            .defaultCurrency("ARS")
                            .build();
                    return userRepository.save(newUser);
                });

        // 2. Asegurar que existan todas las categorías necesarias
        Category serviciosCat = getOrCreateCategory("Servicios del Hogar (Luz, Agua, Gas, Internet)", "zap", "#F59E0B", ExpenseType.SERVICE);
        Category impuestosCat = getOrCreateCategory("Impuestos (Muni, Rentas, Expensas)", "landmark", "#3B82F6", ExpenseType.SERVICE);
        Category telefoniaCat = getOrCreateCategory("Telefonía y Comunicaciones", "phone", "#6366F1", ExpenseType.SERVICE);
        Category superCat = getOrCreateCategory("Supermercado y Alimentos", "shopping-cart", "#10B981", ExpenseType.GROCERY);
        Category suscripCat = getOrCreateCategory("Suscripciones Digitales", "tv", "#8B5CF6", ExpenseType.SUBSCRIPTION);
        Category cuotasCat = getOrCreateCategory("Cuotas de Tarjeta y Préstamos", "credit-card", "#EF4444", ExpenseType.INSTALLMENT);
        Category salidasCat = getOrCreateCategory("Salidas, Bares y Restaurantes", "utensils", "#EC4899", ExpenseType.LEISURE);
        Category transCat = getOrCreateCategory("Transporte y Combustible", "car", "#0EA5E9", ExpenseType.TRANSPORT);
        Category variosCat = getOrCreateCategory("Gastos Diarios / Varios", "coffee", "#64748B", ExpenseType.DAILY);

        // 3. Precargar gastos del 2026 de la planilla de Tomas si aún no hay ninguno
        if (expenseRepository.count() == 0) {
            log.info("Precargando gastos y servicios iniciales del 2026 para Tomas...");

            List<Expense> initialExpenses = List.of(
                    // EPEC (Luz)
                    Expense.builder()
                            .title("EPEC - Energía Eléctrica")
                            .description("Contrato 0285055301 / Cliente 4941318")
                            .amount(new BigDecimal("26880.70"))
                            .currency(Currency.ARS)
                            .date(LocalDate.of(2026, 11, 2))
                            .expenseType(ExpenseType.SERVICE)
                            .paymentMethod(PaymentMethod.BANK_TRANSFER)
                            .status(ExpenseStatus.PAID)
                            .category(serviciosCat)
                            .isRecurring(true)
                            .recurrenceDay(5)
                            .referenceCode("0285055301")
                            .period("5.1-2")
                            .user(user)
                            .build(),

                    // ECOGAS
                    Expense.builder()
                            .title("ECOGAS - Gas Natural")
                            .description("Cuenta 21289350 - Factura bimestral cuota 1/2")
                            .amount(new BigDecimal("15159.65"))
                            .currency(Currency.ARS)
                            .date(LocalDate.of(2026, 11, 2))
                            .expenseType(ExpenseType.SERVICE)
                            .paymentMethod(PaymentMethod.DEBIT_CARD)
                            .status(ExpenseStatus.PAID)
                            .category(serviciosCat)
                            .isRecurring(true)
                            .recurrenceDay(8)
                            .referenceCode("21289350")
                            .period("05-1/2")
                            .user(user)
                            .build(),

                    // AGUAS CORDOBESAS
                    Expense.builder()
                            .title("Aguas Cordobesas")
                            .description("N° Facturación: 729093 - Débito Automático Banco Nación")
                            .amount(new BigDecimal("22526.18"))
                            .currency(Currency.ARS)
                            .date(LocalDate.of(2026, 10, 21))
                            .expenseType(ExpenseType.SERVICE)
                            .paymentMethod(PaymentMethod.BANK_TRANSFER)
                            .status(ExpenseStatus.PAID)
                            .category(serviciosCat)
                            .isRecurring(true)
                            .recurrenceDay(21)
                            .referenceCode("729093")
                            .period("2026/10")
                            .user(user)
                            .build(),

                    // CLARO HOGAR
                    Expense.builder()
                            .title("Claro Internet + TV (Hogar)")
                            .description("N° cuenta: 21438376749")
                            .amount(new BigDecimal("19514.16"))
                            .currency(Currency.ARS)
                            .date(LocalDate.of(2026, 10, 15))
                            .expenseType(ExpenseType.SERVICE)
                            .paymentMethod(PaymentMethod.DEBIT_CARD)
                            .status(ExpenseStatus.PAID)
                            .category(telefoniaCat)
                            .isRecurring(true)
                            .recurrenceDay(15)
                            .referenceCode("21438376749")
                            .period("2026/10")
                            .user(user)
                            .build(),

                    // EXPENSAS
                    Expense.builder()
                            .title("Expensas Edificio")
                            .description("Liquidación mensual de expensas")
                            .amount(new BigDecimal("79333.22"))
                            .currency(Currency.ARS)
                            .date(LocalDate.of(2026, 10, 10))
                            .expenseType(ExpenseType.SERVICE)
                            .paymentMethod(PaymentMethod.BANK_TRANSFER)
                            .status(ExpenseStatus.PAID)
                            .category(impuestosCat)
                            .isRecurring(true)
                            .recurrenceDay(10)
                            .period("2026/10")
                            .user(user)
                            .build(),

                    // MUNICIPALIDAD DE CORDOBA
                    Expense.builder()
                            .title("Contribución Inmueble (Muni Cba)")
                            .description("Padrón 030900102500015/4")
                            .amount(new BigDecimal("18931.00"))
                            .currency(Currency.ARS)
                            .date(LocalDate.of(2026, 11, 1))
                            .expenseType(ExpenseType.SERVICE)
                            .paymentMethod(PaymentMethod.DEBIT_CARD)
                            .status(ExpenseStatus.PAID)
                            .category(impuestosCat)
                            .isRecurring(true)
                            .recurrenceDay(10)
                            .referenceCode("030900102500015/4")
                            .period("2026/10")
                            .user(user)
                            .build(),

                    // RENTAS CORDOBA
                    Expense.builder()
                            .title("Rentas Inmueble (Prov. Córdoba)")
                            .description("Cuenta 110140272540")
                            .amount(new BigDecimal("4580.00"))
                            .currency(Currency.ARS)
                            .date(LocalDate.of(2026, 11, 1))
                            .expenseType(ExpenseType.SERVICE)
                            .paymentMethod(PaymentMethod.DEBIT_CARD)
                            .status(ExpenseStatus.PAID)
                            .category(impuestosCat)
                            .isRecurring(true)
                            .recurrenceDay(10)
                            .referenceCode("110140272540")
                            .period("2026/10")
                            .user(user)
                            .build(),

                    // CLARO TOMÁS
                    Expense.builder()
                            .title("Claro Celular (Tomás)")
                            .description("Línea móvil personal")
                            .amount(new BigDecimal("35993.99"))
                            .currency(Currency.ARS)
                            .date(LocalDate.of(2026, 10, 20))
                            .expenseType(ExpenseType.SERVICE)
                            .paymentMethod(PaymentMethod.DEBIT_CARD)
                            .status(ExpenseStatus.PAID)
                            .category(telefoniaCat)
                            .isRecurring(true)
                            .recurrenceDay(20)
                            .period("2026/10")
                            .user(user)
                            .build()
            );

            expenseRepository.saveAll(initialExpenses);
            log.info("{} gastos precargados exitosamente en la base de datos.", initialExpenses.size());
        }
    }

    private Category getOrCreateCategory(String name, String icon, String color, ExpenseType type) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name) || c.getName().startsWith(name))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .icon(icon)
                        .color(color)
                        .suggestedType(type)
                        .isSystemDefault(true)
                        .build()));
    }
}
