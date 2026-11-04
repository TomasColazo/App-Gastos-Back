package com.gastos.app.controller;

import com.gastos.app.dto.category.CategoryRequest;
import com.gastos.app.dto.category.CategoryResponse;
import com.gastos.app.model.User;
import com.gastos.app.service.AuthService;
import com.gastos.app.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(categoryService.getCategoriesForUser(user));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCustomCategory(request, user));
    }
}
