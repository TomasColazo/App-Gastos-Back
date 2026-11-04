package com.gastos.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String icon;

    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_type")
    private ExpenseType suggestedType;

    @Column(name = "is_system_default", nullable = false)
    @Builder.Default
    private boolean isSystemDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null if isSystemDefault == true
}
