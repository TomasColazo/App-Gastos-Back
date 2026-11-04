package com.gastos.app.service;

import com.gastos.app.dto.category.CategoryRequest;
import com.gastos.app.dto.category.CategoryResponse;
import com.gastos.app.exception.ResourceNotFoundException;
import com.gastos.app.model.Category;
import com.gastos.app.model.User;
import com.gastos.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategoriesForUser(User user) {
        return categoryRepository.findAllAvailableForUser(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCustomCategory(CategoryRequest request, User user) {
        Category category = Category.builder()
                .name(request.getName().trim())
                .icon(request.getIcon() != null ? request.getIcon() : "tag")
                .color(request.getColor() != null ? request.getColor() : "#64748B")
                .suggestedType(request.getSuggestedType())
                .isSystemDefault(false)
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public Category getCategoryEntity(Long id, User user) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        if (!category.isSystemDefault() && (category.getUser() == null || !category.getUser().getId().equals(user.getId()))) {
            throw new ResourceNotFoundException("Categoría no autorizada o no encontrada");
        }
        return category;
    }

    public CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .suggestedType(category.getSuggestedType())
                .isSystemDefault(category.isSystemDefault())
                .build();
    }
}
