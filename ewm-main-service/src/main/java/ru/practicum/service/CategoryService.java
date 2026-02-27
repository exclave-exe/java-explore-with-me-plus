package ru.practicum.service;

import ru.practicum.dto.CategoryCreateDto;
import ru.practicum.dto.CategoryResponseDto;
import ru.practicum.dto.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto createCategory(CategoryCreateDto categoryCreateDto);

    CategoryResponseDto getCategoryById(Long id);

    List<CategoryResponseDto> getCategories(Integer from, Integer size);

    CategoryResponseDto updateCategory(Long id, CategoryUpdateDto categoryUpdateDto);

    void deleteCategory(Long id);
}
