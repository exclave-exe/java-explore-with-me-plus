package ru.practicum.main.category.service;

import ru.practicum.main.category.dto.CategoryCreateDto;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(CategoryCreateDto categoryCreateDto);

    CategoryDto getCategoryById(Long id);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto updateCategory(Long id, CategoryUpdateDto categoryUpdateDto);

    void deleteCategory(Long id);
}
