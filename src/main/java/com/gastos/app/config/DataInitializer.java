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
import java.util.ArrayList;
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
        getOrCreateCategory("Supermercado y Alimentos", "shopping-cart", "#10B981", ExpenseType.GROCERY);
        getOrCreateCategory("Suscripciones Digitales", "tv", "#8B5CF6", ExpenseType.SUBSCRIPTION);
        getOrCreateCategory("Cuotas de Tarjeta y Préstamos", "credit-card", "#EF4444", ExpenseType.INSTALLMENT);
        getOrCreateCategory("Salidas, Bares y Restaurantes", "utensils", "#EC4899", ExpenseType.LEISURE);
        getOrCreateCategory("Transporte y Combustible", "car", "#0EA5E9", ExpenseType.TRANSPORT);
        getOrCreateCategory("Gastos Diarios / Varios", "coffee", "#64748B", ExpenseType.DAILY);

        // 3. Cargar historial completo 2026 de la planilla de Tomas si aún solo están los 8 iniciales
        if (expenseRepository.count() <= 10) {
            log.info("Cargando el historial completo de impuestos y servicios 2026 de Tomas...");
            expenseRepository.deleteAll();

            List<Expense> all2026 = new ArrayList<>();

            // --- CONTRIBUCIÓN INMUEBLE (Municipalidad de Cba) - Padrón 030900102500015/4 ---
            Object[][] muniData = {
                    {2, "16657.00", "Cuota 1"}, {3, "16657.00", "Cuota 2"}, {4, "16657.00", "Cuota 3"},
                    {5, "17701.36", "Cuota 4"}, {6, "16657.00", "Cuota 5"}, {7, "18931.00", "Cuota 6"},
                    {8, "18931.00", "Cuota 7"}, {9, "18931.00", "Cuota 8"}, {10, "18931.00", "Cuota 9"},
                    {11, "18931.00", "Cuota 10"}
            };
            for (Object[] row : muniData) {
                int m = (int) row[0];
                all2026.add(buildServiceExpense(
                        user, impuestosCat, "Contribución Inmueble (Muni Cba)",
                        "Municipalidad de Córdoba - Padrón 030900102500015/4",
                        (String) row[1], LocalDate.of(2026, m, 10),
                        PaymentMethod.DEBIT_CARD, ExpenseStatus.PAID,
                        "030900102500015/4", (String) row[2], 10
                ));
            }

            // --- RENTAS INMUEBLE (Prov. de Córdoba) - Cuenta 110140272540 ---
            Object[][] rentasData = {
                    {2, "4583.49", "Cuota 1"}, {3, "4583.49", "Cuota 2"}, {4, "4583.49", "Cuota 3"},
                    {5, "4583.49", "Cuota 4"}, {6, "4583.19", "Cuota 5"}, {7, "4583.50", "Cuota 6"},
                    {9, "4580.00", "Cuota 8"}, {10, "4583.49", "Cuota 9"}, {11, "4580.00", "Cuota 10"}
            };
            for (Object[] row : rentasData) {
                int m = (int) row[0];
                all2026.add(buildServiceExpense(
                        user, impuestosCat, "Rentas Inmueble (Prov. Córdoba)",
                        "Rentas Provincia de Córdoba - Cuenta 110140272540",
                        (String) row[1], LocalDate.of(2026, m, 10),
                        PaymentMethod.DEBIT_CARD, ExpenseStatus.PAID,
                        "110140272540", (String) row[2], 10
                ));
            }

            // --- ECOGAS - Cuenta 21289350 ---
            Object[][] ecogasData = {
                    {3, "9603.91", "01-1/2"}, {4, "9568.05", "01-2/2"},
                    {5, "9000.22", "02-1/2"}, {6, "8880.43", "02-2/2"},
                    {7, "25269.50", "03-1/2"}, {8, "25154.29", "03-2/2"},
                    {9, "27497.73", "04-1/2"}, {10, "27282.15", "04-2/2"},
                    {11, "15159.65", "05-1/2"}
            };
            for (Object[] row : ecogasData) {
                int m = (int) row[0];
                all2026.add(buildServiceExpense(
                        user, serviciosCat, "ECOGAS - Gas Natural",
                        "Cuenta 21289350",
                        (String) row[1], LocalDate.of(2026, m, 8),
                        PaymentMethod.DEBIT_CARD, ExpenseStatus.PAID,
                        "21289350", (String) row[2], 8
                ));
            }

            // --- EPEC - Contrato 0285055301 ---
            Object[][] epecData = {
                    {3, "30847.80", "1.1-2"}, {4, "56413.60", "1.2-2"},
                    {5, "61241.20", "2.1-2"}, {6, "27194.00", "2.2-2"},
                    {7, "59480.00", "3.1-2"}, {8, "62109.50", "3.2-2"},
                    {9, "38315.50", "4.1-2"}, {10, "51478.90", "4.2-2"},
                    {11, "26880.70", "5.1-2"}
            };
            for (Object[] row : epecData) {
                int m = (int) row[0];
                all2026.add(buildServiceExpense(
                        user, serviciosCat, "EPEC - Energía Eléctrica",
                        "Contrato 0285055301 / Cliente 4941318",
                        (String) row[1], LocalDate.of(2026, m, 5),
                        PaymentMethod.BANK_TRANSFER, ExpenseStatus.PAID,
                        "0285055301", (String) row[2], 5
                ));
            }

            // --- AGUAS CORDOBESAS - N° Facturación 729093 ---
            Object[][] aguasData = {
                    {2, "19322.08", "Cuota 1"}, {3, "19322.08", "Cuota 2"}, {4, "19322.08", "Cuota 3"},
                    {5, "20712.41", "Cuota 4"}, {6, "20898.18", "Cuota 5"}, {7, "20898.18", "Cuota 6"},
                    {8, "22526.18", "Cuota 7"}, {9, "22526.18", "Cuota 8"}, {10, "22526.18", "Cuota 9"}
            };
            for (Object[] row : aguasData) {
                int m = (int) row[0];
                all2026.add(buildServiceExpense(
                        user, serviciosCat, "Aguas Cordobesas",
                        "N° Facturación: 729093 - DA: Banco Nación",
                        (String) row[1], LocalDate.of(2026, m, 21),
                        PaymentMethod.BANK_TRANSFER, ExpenseStatus.PAID,
                        "729093", (String) row[2], 21
                ));
            }
            // Noviembre Aguas Cordobesas (Pendiente de cobro en el mes actual)
            all2026.add(buildServiceExpense(
                    user, serviciosCat, "Aguas Cordobesas",
                    "N° Facturación: 729093 - DA: Banco Nación",
                    "22526.18", LocalDate.of(2026, 11, 21),
                    PaymentMethod.BANK_TRANSFER, ExpenseStatus.PENDING,
                    "729093", "Cuota 10", 21
            ));

            // --- CLARO INTERNET + TV (Hogar) - Cuenta 21438376749 ---
            Object[][] claroHogarData = {
                    {1, "38177.11", "2026/01"}, {2, "39985.07", "2026/02"}, {3, "41664.46", "2026/03"},
                    {4, "43367.35", "2026/04"}, {5, "45408.82", "2026/05"}, {6, "51793.67", "2026/06"},
                    {7, "54483.33", "2026/07"}, {8, "57251.00", "2026/08"}, {9, "37238.66", "2026/09"},
                    {10, "19514.16", "2026/10"}
            };
            for (Object[] row : claroHogarData) {
                int m = (int) row[0];
                all2026.add(buildServiceExpense(
                        user, telefoniaCat, "Claro Internet + TV (Hogar)",
                        "N° cuenta: 21438376749 - Cuenta de la casa",
                        (String) row[1], LocalDate.of(2026, m, 15),
                        PaymentMethod.DEBIT_CARD, ExpenseStatus.PAID,
                        "21438376749", (String) row[2], 15
                ));
            }
            // Noviembre Claro Hogar (Pendiente)
            all2026.add(buildServiceExpense(
                    user, telefoniaCat, "Claro Internet + TV (Hogar)",
                    "N° cuenta: 21438376749 - Cuenta de la casa",
                    "19514.16", LocalDate.of(2026, 11, 15),
                    PaymentMethod.DEBIT_CARD, ExpenseStatus.PENDING,
                    "21438376749", "2026/11", 15
            ));

            // --- EXPENSAS ---
            Object[][] expensasData = {
                    {1, "68083.00", "2026/01"}, {2, "54863.00", "2026/02"}, {3, "54863.00", "2026/03"},
                    {4, "54863.00", "2026/04"}, {5, "60349.30", "2026/05"}, {6, "60349.30", "2026/06"},
                    {7, "75089.60", "2026/07"}, {8, "75089.60", "2026/08"}, {9, "75089.50", "2026/09"},
                    {10, "79333.22", "2026/10"}
            };
            for (Object[] row : expensasData) {
                int m = (int) row[0];
                all2026.add(buildServiceExpense(
                        user, impuestosCat, "Expensas",
                        "Liquidación mensual de expensas",
                        (String) row[1], LocalDate.of(2026, m, 10),
                        PaymentMethod.BANK_TRANSFER, ExpenseStatus.PAID,
                        null, (String) row[2], 10
                ));
            }
            // Noviembre Expensas (Pendiente)
            all2026.add(buildServiceExpense(
                    user, impuestosCat, "Expensas",
                    "Liquidación mensual de expensas",
                    "79333.22", LocalDate.of(2026, 11, 10),
                    PaymentMethod.BANK_TRANSFER, ExpenseStatus.PENDING,
                    null, "2026/11", 10
            ));

            // --- CLARO TOMÁS ---
            Object[][] claroTomasData = {
                    {1, "25164.00", "2026/01"}, {2, "29850.02", "2026/02"}, {3, "27345.01", "2026/03"},
                    {4, "28407.00", "2026/04"}, {5, "29685.00", "2026/05"}, {6, "31167.00", "2026/06"},
                    {7, "32724.00", "2026/07"}, {8, "34301.99", "2026/08"}, {9, "36942.01", "2026/09"},
                    {10, "35993.99", "2026/10"}
            };
            for (Object[] row : claroTomasData) {
                int m = (int) row[0];
                all2026.add(buildServiceExpense(
                        user, telefoniaCat, "Claro Tomás (Línea Móvil)",
                        "Servicio móvil personal (desde 02/07/2021)",
                        (String) row[1], LocalDate.of(2026, m, 20),
                        PaymentMethod.DEBIT_CARD, ExpenseStatus.PAID,
                        null, (String) row[2], 20
                ));
            }
            // Noviembre Claro Tomás (Pendiente)
            all2026.add(buildServiceExpense(
                    user, telefoniaCat, "Claro Tomás (Línea Móvil)",
                    "Servicio móvil personal (desde 02/07/2021)",
                    "35993.99", LocalDate.of(2026, 11, 20),
                    PaymentMethod.DEBIT_CARD, ExpenseStatus.PENDING,
                    null, "2026/11", 20
            ));

            expenseRepository.saveAll(all2026);
            log.info("{} registros históricos de 2026 cargados exitosamente.", all2026.size());
        }
    }

    private Expense buildServiceExpense(
            User user, Category category, String title, String description,
            String amountStr, LocalDate date, PaymentMethod pm, ExpenseStatus status,
            String refCode, String period, int recurrenceDay
    ) {
        return Expense.builder()
                .title(title)
                .description(description)
                .amount(new BigDecimal(amountStr))
                .currency(Currency.ARS)
                .date(date)
                .expenseType(ExpenseType.SERVICE)
                .paymentMethod(pm)
                .status(status)
                .category(category)
                .isRecurring(true)
                .recurrenceDay(recurrenceDay)
                .referenceCode(refCode)
                .period(period)
                .user(user)
                .build();
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
