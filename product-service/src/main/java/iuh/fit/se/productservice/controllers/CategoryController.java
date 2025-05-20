package iuh.fit.se.productservice.controllers;

import iuh.fit.se.productservice.Services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories(){
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", categoryService.findAll());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", categoryService.findById(id));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
