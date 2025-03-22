package iuh.fit.se.productservice.Services;

import java.util.List;

import iuh.fit.se.productservice.entities.Category;

public interface CategoryService {
	public Category findById(Long id);
	public List<Category> findAll();
}
