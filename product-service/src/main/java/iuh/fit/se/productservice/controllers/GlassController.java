package iuh.fit.se.productservice.controllers;

import iuh.fit.se.productservice.Services.CategoryService;
import iuh.fit.se.productservice.Services.FrameSizeService;
import iuh.fit.se.productservice.Services.GlassService;
import iuh.fit.se.productservice.Services.SpecificationService;
import iuh.fit.se.productservice.dtos.FilterRequest;
import iuh.fit.se.productservice.dtos.GlassDTO;
import iuh.fit.se.productservice.dtos.GlassesDTO;
import iuh.fit.se.productservice.entities.Glass;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class GlassController {

	@Autowired
	private GlassService glassService;
	
	@Autowired
	private FrameSizeService frameSizeService;
	
	@Autowired
	private SpecificationService specificationService;
	
	@Autowired
	private CategoryService categoryService;

	@GetMapping("/hello")
	public String hello() {
		return "Hello from Product Service";
	}

	@GetMapping("/glasses")
	public ResponseEntity<Map<String, Object>> getAll(){
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.findAll());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/glasses/{id}")
	public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id){
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.findById(id));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/eyeglasses")
	public ResponseEntity<Map<String, Object>> getByCategoryEyeGlass() {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.findByCategory(1L));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/sunglasses")
	public ResponseEntity<Map<String, Object>> getByCategorySunGlass() {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.findByCategory(2L));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
//	@GetMapping("/eyeglasses/men")
//	public ResponseEntity<Map<String, Object>> getByCategoryEyeGlassMen(
//			@RequestParam(required = false) String brand,
//			@RequestParam(required = false) String shape,
//			@RequestParam(required = false) String material,
//			@RequestParam(required = false) String color,
//			@RequestParam(required = false) String minPrice,
//			@RequestParam(required = false) String maxPrice) {
//		FilterRequest filter = new FilterRequest();
//		filter.setBrands(brand);
//		filter.setShapes(shape);
//		filter.setMaterials(material);
//		filter.setColors(color);
//		filter.setMinPrice(minPrice);
//		filter.setMaxPrice(maxPrice);
//		System.out.println(filter);
//		Map<String, Object> response = new LinkedHashMap<String, Object>();
//		response.put("status", HttpStatus.OK.value());
//		response.put("data", glassService.findByCategoryAndGenderAndFilter(1L, true, filter));
//		return ResponseEntity.status(HttpStatus.OK).body(response);
//	}
//	@GetMapping("/eyeglasses/women")
//	public ResponseEntity<Map<String, Object>> getByCategoryEyeGlassWomen(
//			@RequestParam(required = false) String brand,
//			@RequestParam(required = false) String shape,
//			@RequestParam(required = false) String material,
//			@RequestParam(required = false) String color,
//			@RequestParam(required = false) String minPrice,
//			@RequestParam(required = false) String maxPrice) {
//		FilterRequest filter = new FilterRequest();
//		filter.setBrands(brand);
//		filter.setShapes(shape);
//		filter.setMaterials(material);
//		filter.setColors(color);
//		filter.setMinPrice(minPrice);
//		filter.setMaxPrice(maxPrice);
//		System.out.println(filter);
//		Map<String, Object> response = new LinkedHashMap<String, Object>();
//		response.put("status", HttpStatus.OK.value());
//		response.put("data", glassService.findByCategoryAndGenderAndFilter(1L, false, filter));
//		return ResponseEntity.status(HttpStatus.OK).body(response);
//	}
//	@GetMapping("/sunglasses/men")
//	public ResponseEntity<Map<String, Object>> getByCategorySunGlassMen(
//			@RequestParam(required = false) String brand,
//			@RequestParam(required = false) String shape,
//			@RequestParam(required = false) String material,
//			@RequestParam(required = false) String color,
//			@RequestParam(required = false) String minPrice,
//			@RequestParam(required = false) String maxPrice) {
//		FilterRequest filter = new FilterRequest();
//		filter.setBrands(brand);
//		filter.setShapes(shape);
//		filter.setMaterials(material);
//		filter.setColors(color);
//		filter.setMinPrice(minPrice);
//		filter.setMaxPrice(maxPrice);
//		System.out.println(filter);
//		Map<String, Object> response = new LinkedHashMap<String, Object>();
//		response.put("status", HttpStatus.OK.value());
//		response.put("data", glassService.findByCategoryAndGenderAndFilter(2L, true, filter));
//		return ResponseEntity.status(HttpStatus.OK).body(response);
//	}
	@GetMapping("/sunglasses/women")
	public ResponseEntity<Map<String, Object>> getByCategorySunGlassWomen(
			@RequestParam(required = false) String brand,
			@RequestParam(required = false) String shape,
			@RequestParam(required = false) String material,
			@RequestParam(required = false) String color,
			@RequestParam(required = false) String minPrice,
			@RequestParam(required = false) String maxPrice) {
		FilterRequest filter = new FilterRequest();
		filter.setBrands(brand);
		filter.setShapes(shape);
		filter.setMaterials(material);
		filter.setColors(color);
		filter.setMinPrice(minPrice);
		filter.setMaxPrice(maxPrice);
		System.out.println(filter);
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
//		response.put("data", glassService.findByCategoryAndGenderAndFilter(2L, false, filter));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/brands")
	public ResponseEntity<Map<String, Object>> getAllBrand() {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.getAllBrand());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/shapes")
	public ResponseEntity<Map<String, Object>> getAllShape() {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.getAllShape());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/materials")
	public ResponseEntity<Map<String, Object>> getAllMaterial() {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.getAllMaterial());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/colors")
	public ResponseEntity<Map<String, Object>> getAllColor() {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.getAllColor());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/search")
	public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<GlassDTO> glasses = glassService.search(keyword).stream().map(glass -> new GlassDTO(
				glass.getId(),
				glass.getImageSideUrl(),
				glass.getImageFrontUrl(),
				glass.getColorCode(),
				glass.getName(),
				glass.getBrand(),
				glass.getPrice())).collect(Collectors.toList());
		response.put("status", HttpStatus.OK.value());
		response.put("data", glasses);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/top5-glasses")
	public ResponseEntity<Map<String, Object>> getTop5Glasses(){
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("status", HttpStatus.OK.value());
		response.put("data", glassService.getTop5Glasses());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	//Thêm sản phẩm
		@PostMapping("/glasses")
		public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody Glass glassDTO , BindingResult bindingResult) {
		    Map<String, Object> response = new LinkedHashMap<>();
		    
		    if (bindingResult.hasErrors()) {
				Map<String, Object> errors = new LinkedHashMap<String, Object>();
				
				bindingResult.getFieldErrors().stream().forEach(result -> {
					errors.put(result.getField(), result.getDefaultMessage());
				});
				
				System.out.println(bindingResult);
				response.put("status", HttpStatus.BAD_REQUEST.value());
				response.put("errors", errors);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			else {
//				
				response.put("status", HttpStatus.OK.value());
				response.put("data", glassService.save(glassDTO));
				
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
		}


	    // Cập nhật sản phẩm
		@PutMapping("/glasses/{id}")
		public ResponseEntity<Map<String, Object>> update(
		        @PathVariable Long id, 
		        @Valid @RequestBody GlassesDTO glassDTO, 
		        BindingResult bindingResult) {
		    Map<String, Object> response = new LinkedHashMap<>();

		    // Kiểm tra lỗi ràng buộc (validation)
		    if (bindingResult.hasErrors()) {
		        Map<String, Object> errors = new LinkedHashMap<>();
		        bindingResult.getFieldErrors().forEach(error -> {
		            errors.put(error.getField(), error.getDefaultMessage());
		        });

		        response.put("status", HttpStatus.BAD_REQUEST.value());
		        response.put("errors", errors);
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		    }

		    // Kiểm tra xem sản phẩm có tồn tại hay không
		    Glass existingGlass = glassService.findById(id);
		    if (existingGlass == null) {
		        response.put("status", HttpStatus.NOT_FOUND.value());
		        response.put("error", "Product not found");
		        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		    }

		    // Thực hiện cập nhật
		    try {
		        GlassesDTO updatedGlass = glassService.update(id, glassDTO);
		        response.put("status", HttpStatus.OK.value());
		        response.put("data", updatedGlass);
		        return ResponseEntity.ok(response);
		    } catch (Exception e) {
		        // Xử lý các lỗi khác
		        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		        response.put("error", "An error occurred while updating the product: " + e.getMessage());
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		    }
		}
		
		@DeleteMapping("/glasses/{id}")
		public ResponseEntity<Map<String, Object>> deleteGlass(@PathVariable long id) {
			Map<String, Object> response = new LinkedHashMap<String, Object>();
			if(glassService.delete(id)) {
				response.put("status", HttpStatus.OK.value());
				response.put("message", "Delete success");
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}else {
				response.put("status", HttpStatus.BAD_REQUEST.value());
				response.put("message", "Delete fail");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			
		}


	    // Tìm kiếm sản phẩm theo từ khóa
	    @GetMapping("/glasses/search")
	    public ResponseEntity<Map<String, Object>> searchGlasses(@RequestParam String keyword) {
	        Map<String, Object> response = new LinkedHashMap<>();
	        response.put("status", HttpStatus.OK.value());
	        response.put("data", glassService.search(keyword));
	        return ResponseEntity.ok(response);
	    }
}
