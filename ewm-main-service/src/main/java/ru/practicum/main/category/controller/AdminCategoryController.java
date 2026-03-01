package ru.practicum.main.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.category.dto.CategoryCreateDto;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.CategoryUpdateDto;
import ru.practicum.main.category.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@Validated

@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid CategoryCreateDto categoryCreateDto) {
        log.info("Creating category: name={}", categoryCreateDto.getName());
        return categoryService.createCategory(categoryCreateDto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable @Positive Long catId,
                                      @RequestBody @Valid CategoryUpdateDto categoryUpdateDto) {
        log.info("Creating category: new name={}", categoryUpdateDto.getName());
        return categoryService.updateCategory(catId, categoryUpdateDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategories(@PathVariable @Positive Long catId) {
        log.info("Deleting category: id={}", catId);
        categoryService.deleteCategory(catId);
    }
}
