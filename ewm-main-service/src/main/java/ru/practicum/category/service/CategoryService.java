package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryResponseDto;
import ru.practicum.category.dto.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto createCategory(CategoryCreateDto categoryCreateDto);

    CategoryResponseDto getCategoryById(Long id);

    List<CategoryResponseDto> getCategories(Integer from, Integer size);

    CategoryResponseDto updateCategory(Long id, CategoryUpdateDto categoryUpdateDto);

    void deleteCategory(Long id);
}
