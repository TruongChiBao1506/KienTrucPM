package iuh.fit.se.productservice.Services;

import java.util.List;

import iuh.fit.se.productservice.entities.Category;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Long id);
    Category save(Category category);
    Category update(Long id, Category category);
    boolean delete(Long id);
}