package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.dto.CategoryCreateDto;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.CategoryUpdateDto;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryCreateDto categoryCreateDto) {
        validateCategoryNameExists(categoryCreateDto.getName());
        Category categoryToCreate = categoryMapper.mapToCategory(categoryCreateDto);
        Category createdCategory = categoryRepository.save(categoryToCreate);
        return categoryMapper.mapToResponseDto(createdCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category existingCategory = getCategoryOrThrow(id);
        return categoryMapper.mapToResponseDto(existingCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        List<Category> existingCategories = categoryRepository.findAllWithOffset(from, size);
        return categoryMapper.mapToListResponseDto(existingCategories);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryUpdateDto categoryUpdateDto) {
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
    @Transactional
    public void deleteCategory(Long id) {
        if (eventRepository.existsByCategory_Id(id))
            throw new ConflictException("Category " + id + " is attached to existing events and cannot be removed");

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