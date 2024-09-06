package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.util.CheckExistence;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CheckExistence checkExistence;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Start creating new category");
        if (categoryRepository.findByName(newCategoryDto.getName()).isPresent()) {
            log.info("Category with name {} already exists", newCategoryDto.getName());
            throw new ConflictException("Category with name " + newCategoryDto.getName() + " already exists");
        } else {
            Category category = categoryRepository.save(CategoryMapper.toCategoryFromNewCategoryDto(newCategoryDto));
            log.info("Category {} created", CategoryMapper.toCategoryDto(category));
            return CategoryMapper.toCategoryDto(category);
        }
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConflictException("There is an event with category id " + catId);
        }
        checkExistence.getCategory(catId);
        log.info("Start deleting category with id {}", catId);
        categoryRepository.deleteById(catId);
        log.info("Category deleted");
    }

    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        checkExistence.getCategory(catId);
        checkExistence.checkCategoryExistence(newCategoryDto);
        log.info("Start updating category with id {}", catId);
        Category category = checkExistence.getCategory(catId);
        category.setName(newCategoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Get all categories from [{}], size [{}]", from, size);
        PageRequest pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        log.info("Get category with id {}", catId);
        return CategoryMapper.toCategoryDto(checkExistence.getCategory(catId));
    }
}
