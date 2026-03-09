package ru.practicum.main.category.service;

import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto getCategoryById(Long id);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto updateCategory(Long id, NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);
}
