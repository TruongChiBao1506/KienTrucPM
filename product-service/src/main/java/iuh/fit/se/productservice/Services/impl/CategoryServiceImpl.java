package iuh.fit.se.productservice.Services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.productservice.entities.Category;
import iuh.fit.se.productservice.Repositories.CategoryRepository;
import iuh.fit.se.productservice.Services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService{
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Override
	public Category findById(Long id) {
		return categoryRepository.findById(id).orElse(null);
	}

	@Override
	public List<Category> findAll() {
		return categoryRepository.findAll();
	}

}
