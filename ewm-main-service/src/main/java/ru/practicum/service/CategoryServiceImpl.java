package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryCreateDto;
import ru.practicum.dto.CategoryResponseDto;
import ru.practicum.dto.CategoryUpdateDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CategoryCreateDto categoryCreateDto) {
        validateCategoryNameExists(categoryCreateDto.getName());
        Category categoryToCreate = categoryMapper.mapToCategory(categoryCreateDto);
        Category createdCategory = categoryRepository.save(categoryToCreate);
        return categoryMapper.mapToResponseDto(createdCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category existingCategory = getCategoryOrThrow(id);
        return categoryMapper.mapToResponseDto(existingCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getCategories(Integer from, Integer size) {
        List<Category> existingCategories = categoryRepository.findAllWithOffset(from, size);
        return categoryMapper.mapToListResponseDto(existingCategories);
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryUpdateDto categoryUpdateDto) {
        Category existingCategory = getCategoryOrThrow(id);
        String oldName = existingCategory.getName();
        String newName = categoryUpdateDto.getName();

        if (!newName.equals(oldName)) {
            validateCategoryNameExists(categoryUpdateDto.getName());
            existingCategory.setName(categoryUpdateDto.getName());
        }

        return categoryMapper.mapToResponseDto(existingCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    private void validateCategoryNameExists(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException("Category with name=" + name + " already exists");
        }
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Category with id=" + id + " not found"));
    }
}
