package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;


    @PostMapping
    public CategoryDto createCategory(@RequestBody NewCategoryDto newCategoryDto) {
        log.info("CREATING NEW CATEGORY");
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETING NEW CATEGORY");
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody NewCategoryDto newCategoryDto) {
        log.info("UPDATING NEW CATEGORY");
        return categoryService.updateCategory(catId, newCategoryDto);
    }

}
