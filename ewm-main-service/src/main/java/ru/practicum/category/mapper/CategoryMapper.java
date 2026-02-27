package ru.practicum.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryResponseDto;
import ru.practicum.category.model.Category;

import java.util.List;

@Component
public class CategoryMapper {

    public Category mapToCategory(CategoryCreateDto categoryCreateDto) {
        return Category.builder()
                .name(categoryCreateDto.getName())
                .build();
    }

    public CategoryResponseDto mapToResponseDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public List<CategoryResponseDto> mapToListResponseDto(List<Category> categories) {
        return categories.stream()
                .map(this::mapToResponseDto)
                .toList();
    }
}
