package ru.practicum.main.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.model.Category;

import java.util.List;

@Component
public class CategoryMapper {

    public Category mapToCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public CategoryDto mapToResponseDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public List<CategoryDto> mapToListResponseDto(List<Category> categories) {
        return categories.stream()
                .map(this::mapToResponseDto)
                .toList();
    }
}
