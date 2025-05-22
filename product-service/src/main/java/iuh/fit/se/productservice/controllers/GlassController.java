package iuh.fit.se.productservice.controllers;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import iuh.fit.se.productservice.Services.CategoryService;
import iuh.fit.se.productservice.Services.FrameSizeService;
import iuh.fit.se.productservice.Services.GlassService;
import iuh.fit.se.productservice.Services.SpecificationService;
import iuh.fit.se.productservice.dtos.*;
import iuh.fit.se.productservice.entities.Glass;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private CategoryService categoryService;    //	@GetMapping("/hello")

    //	public String hello() {
//		return "Hello from Product Service";
//	}
    @GetMapping("/glasses")
    @RateLimiter(name = "listingEndpoints", fallbackMethod = "fallbackForListingEndpoints")
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        // Lấy dữ liệu phân trang
        Page<Glass> glassPage = glassService.findAllPaginated(page, size);

        response.put("status", HttpStatus.OK.value());
        response.put("data", glassPage.getContent());
        response.put("currentPage", glassPage.getNumber());
        response.put("totalItems", glassPage.getTotalElements());
        response.put("totalPages", glassPage.getTotalPages());
        response.put("hasMore", !glassPage.isLast());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/glasses/{id}")
    @RateLimiter(name = "detailEndpoints", fallbackMethod = "fallbackForDetailEndpoints")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", glassService.findById(id));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/glasses-review/{id}")
    @RateLimiter(name = "detailEndpoints", fallbackMethod = "fallbackForDetailEndpoints")
    public ResponseEntity<Map<String, Object>> getGlassesReviewById(@PathVariable Long id) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        Glass glass = glassService.findById(id);
        if (glass == null) {
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Product not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            GlassesDTOForReviewResonponse glassesDTOForReviewResonponse = new GlassesDTOForReviewResonponse();
            glassesDTOForReviewResonponse.setId(glass.getId());
            glassesDTOForReviewResonponse.setName(glass.getName());
            glassesDTOForReviewResonponse.setDescription(glass.getDescription());
            glassesDTOForReviewResonponse.setPrice(glass.getPrice());
            response.put("status", HttpStatus.OK.value());
            response.put("data", glassesDTOForReviewResonponse);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

    }

    @GetMapping("/eyeglasses")
    @RateLimiter(name = "listingEndpoints", fallbackMethod = "fallbackForListingEndpoints")
    public ResponseEntity<Map<String, Object>> getByCategoryEyeGlass(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) Boolean gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        // Nếu có các tham số lọc, sử dụng chúng
        if (brand != null || shape != null || material != null || color != null ||
                minPrice != null || maxPrice != null || gender != null) {

            FilterRequest filter = new FilterRequest();
            filter.setBrands(brand);
            filter.setShapes(shape);
            filter.setMaterials(material);
            filter.setColors(color);
            filter.setMinPrice(minPrice);
            filter.setMaxPrice(maxPrice);

            Page<GlassDTO> glassPage;
            if (gender != null) {
                // Nếu có giới tính, sử dụng endpoint lọc theo giới tính và danh mục
                glassPage = glassService.findByCategoryAndGenderAndFilterPaginated(2L, gender, filter, page, size);
            } else {
                // Nếu không có giới tính, chỉ lọc theo danh mục
                // Điều này yêu cầu thêm phương thức mới vào service
                glassPage = glassService.findByCategoryAndFilterPaginated(2L, filter, page, size);
            }

            response.put("status", HttpStatus.OK.value());
            response.put("data", glassPage.getContent());
            response.put("currentPage", glassPage.getNumber());
            response.put("totalItems", glassPage.getTotalElements());
            response.put("totalPages", glassPage.getTotalPages());
            response.put("hasMore", !glassPage.isLast());
        } else {
            // Nếu không có tham số lọc, sử dụng endpoint hiện tại
            Page<Glass> glassPage = glassService.findByCategoryPaginated(2L, page, size);

            response.put("status", HttpStatus.OK.value());
            response.put("data", glassPage.getContent());
            response.put("currentPage", glassPage.getNumber());
            response.put("totalItems", glassPage.getTotalElements());
            response.put("totalPages", glassPage.getTotalPages());
            response.put("hasMore", !glassPage.isLast());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sunglasses")
    @RateLimiter(name = "listingEndpoints", fallbackMethod = "fallbackForListingEndpoints")
    public ResponseEntity<Map<String, Object>> getByCategorySunGlass(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) Boolean gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        // Nếu có các tham số lọc, sử dụng chúng
        if (brand != null || shape != null || material != null || color != null ||
                minPrice != null || maxPrice != null || gender != null) {

            FilterRequest filter = new FilterRequest();
            filter.setBrands(brand);
            filter.setShapes(shape);
            filter.setMaterials(material);
            filter.setColors(color);
            filter.setMinPrice(minPrice);
            filter.setMaxPrice(maxPrice);

            Page<GlassDTO> glassPage;
            if (gender != null) {
                // Nếu có giới tính, sử dụng endpoint lọc theo giới tính và danh mục
                glassPage = glassService.findByCategoryAndGenderAndFilterPaginated(1L, gender, filter, page, size);
            } else {
                // Nếu không có giới tính, chỉ lọc theo danh mục
                glassPage = glassService.findByCategoryAndFilterPaginated(1L, filter, page, size);
            }

            response.put("status", HttpStatus.OK.value());
            response.put("data", glassPage.getContent());
            response.put("currentPage", glassPage.getNumber());
            response.put("totalItems", glassPage.getTotalElements());
            response.put("totalPages", glassPage.getTotalPages());
            response.put("hasMore", !glassPage.isLast());
        } else {
            // Nếu không có tham số lọc, sử dụng endpoint hiện tại
            Page<Glass> glassPage = glassService.findByCategoryPaginated(1L, page, size);

            response.put("status", HttpStatus.OK.value());
            response.put("data", glassPage.getContent());
            response.put("currentPage", glassPage.getNumber());
            response.put("totalItems", glassPage.getTotalElements());
            response.put("totalPages", glassPage.getTotalPages());
            response.put("hasMore", !glassPage.isLast());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/eyeglasses/men")
    @RateLimiter(name = "listingEndpoints", fallbackMethod = "fallbackForListingEndpoints")
    public ResponseEntity<Map<String, Object>> getByCategoryEyeGlassMen(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        FilterRequest filter = new FilterRequest();
        filter.setBrands(brand);
        filter.setShapes(shape);
        filter.setMaterials(material);
        filter.setColors(color);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        System.out.println(filter);

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        Page<GlassDTO> glassPage = glassService.findByCategoryAndGenderAndFilterPaginated(2L, true, filter, page, size);

        response.put("status", HttpStatus.OK.value());
        response.put("data", glassPage.getContent());
        response.put("currentPage", glassPage.getNumber());
        response.put("totalItems", glassPage.getTotalElements());
        response.put("totalPages", glassPage.getTotalPages());
        response.put("hasMore", !glassPage.isLast());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/eyeglasses/women")
    @RateLimiter(name = "listingEndpoints", fallbackMethod = "fallbackForListingEndpoints")
    public ResponseEntity<Map<String, Object>> getByCategoryEyeGlassWomen(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        FilterRequest filter = new FilterRequest();
        filter.setBrands(brand);
        filter.setShapes(shape);
        filter.setMaterials(material);
        filter.setColors(color);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        System.out.println(filter);

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        Page<GlassDTO> glassPage = glassService.findByCategoryAndGenderAndFilterPaginated(2L, false, filter, page, size);

        response.put("status", HttpStatus.OK.value());
        response.put("data", glassPage.getContent());
        response.put("currentPage", glassPage.getNumber());
        response.put("totalItems", glassPage.getTotalElements());
        response.put("totalPages", glassPage.getTotalPages());
        response.put("hasMore", !glassPage.isLast());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sunglasses/men")
    @RateLimiter(name = "listingEndpoints", fallbackMethod = "fallbackForListingEndpoints")
    public ResponseEntity<Map<String, Object>> getByCategorySunGlassMen(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        FilterRequest filter = new FilterRequest();
        filter.setBrands(brand);
        filter.setShapes(shape);
        filter.setMaterials(material);
        filter.setColors(color);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        System.out.println(filter);

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        Page<GlassDTO> glassPage = glassService.findByCategoryAndGenderAndFilterPaginated(1L, true, filter, page, size);

        response.put("status", HttpStatus.OK.value());
        response.put("data", glassPage.getContent());
        response.put("currentPage", glassPage.getNumber());
        response.put("totalItems", glassPage.getTotalElements());
        response.put("totalPages", glassPage.getTotalPages());
        response.put("hasMore", !glassPage.isLast());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sunglasses/women")
    @RateLimiter(name = "listingEndpoints", fallbackMethod = "fallbackForListingEndpoints")
    public ResponseEntity<Map<String, Object>> getByCategorySunGlassWomen(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        FilterRequest filter = new FilterRequest();
        filter.setBrands(brand);
        filter.setShapes(shape);
        filter.setMaterials(material);
        filter.setColors(color);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        System.out.println(filter);

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        Page<GlassDTO> glassPage = glassService.findByCategoryAndGenderAndFilterPaginated(1L, false, filter, page, size);

        response.put("status", HttpStatus.OK.value());
        response.put("data", glassPage.getContent());
        response.put("currentPage", glassPage.getNumber());
        response.put("totalItems", glassPage.getTotalElements());
        response.put("totalPages", glassPage.getTotalPages());
        response.put("hasMore", !glassPage.isLast());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/brands")
    @RateLimiter(name = "supportingEndpoints", fallbackMethod = "fallbackForSupportingEndpoints")
    public ResponseEntity<Map<String, Object>> getAllBrand() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", glassService.getAllBrand());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/shapes")
    @RateLimiter(name = "supportingEndpoints", fallbackMethod = "fallbackForSupportingEndpoints")
    public ResponseEntity<Map<String, Object>> getAllShape() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", glassService.getAllShape());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/materials")
    @RateLimiter(name = "supportingEndpoints", fallbackMethod = "fallbackForSupportingEndpoints")
    public ResponseEntity<Map<String, Object>> getAllMaterial() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", glassService.getAllMaterial());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/colors")
    @RateLimiter(name = "supportingEndpoints", fallbackMethod = "fallbackForSupportingEndpoints")
    public ResponseEntity<Map<String, Object>> getAllColor() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", glassService.getAllColor());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/search")
    @RateLimiter(name = "searchEndpoints", fallbackMethod = "fallbackForSearchEndpoints")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        Page<Glass> searchResults = glassService.searchPaginated(keyword, page, size);
        List<GlassDTO> glasses = searchResults.getContent().stream().map(glass -> new GlassDTO(
                glass.getId(),
                glass.getImageSideUrl(),
                glass.getImageFrontUrl(),
                glass.getColorCode(),
                glass.getName(),
                glass.getBrand(),
                glass.getPrice())).collect(Collectors.toList());

        response.put("status", HttpStatus.OK.value());
        response.put("data", glasses);
        response.put("currentPage", searchResults.getNumber());
        response.put("totalItems", searchResults.getTotalElements());
        response.put("totalPages", searchResults.getTotalPages());
        response.put("hasMore", !searchResults.isLast());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top5-glasses")
    @RateLimiter(name = "listingEndpoints", fallbackMethod = "fallbackForListingEndpoints")
    public ResponseEntity<Map<String, Object>> getTop5Glasses() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", glassService.getTop5Glasses());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }    //Thêm sản phẩm

    @PostMapping("/glasses")
    @RateLimiter(name = "writeEndpoints", fallbackMethod = "fallbackForWriteEndpoints")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody GlassesDTO glassDTO, BindingResult bindingResult) {
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
        } else {
            Glass glass = new Glass();
            glass.setName(glassDTO.getName());
            glass.setBrand(glassDTO.getBrand());
            glass.setPrice(glassDTO.getPrice());
            glass.setColorCode(glassDTO.getColorCode());
            glass.setColorName(glassDTO.getColorName());
            glass.setDescription(glassDTO.getDescription());
            glass.setGender(glassDTO.isGender());
            glass.setStock(glassDTO.getStock());
            glass.setImageFrontUrl(glassDTO.getImageFrontUrl());
            glass.setImageSideUrl(glassDTO.getImageSideUrl());
            glass.setCategory(glassDTO.getCategory());
            glass.setFrameSize(glassDTO.getFrameSize());
            glass.setSpecifications(glassDTO.getSpecifications());

            response.put("status", HttpStatus.OK.value());
            response.put("data", glassService.save(glass));

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    // Cập nhật sản phẩm
    @PutMapping("/glasses/{id}")
    @RateLimiter(name = "writeEndpoints", fallbackMethod = "fallbackForWriteEndpoints")
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
    @RateLimiter(name = "writeEndpoints", fallbackMethod = "fallbackForWriteEndpoints")
    public ResponseEntity<Map<String, Object>> deleteGlass(@PathVariable long id) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        if (glassService.delete(id)) {
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Delete success");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Delete fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }        
    
    // Tìm kiếm sản phẩm theo từ khóa    
    @GetMapping("/glasses/search")
    @RateLimiter(name = "searchEndpoints", fallbackMethod = "fallbackForSearchEndpoints")
    public ResponseEntity<Map<String, Object>> searchGlasses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Map<String, Object> response = new LinkedHashMap<>();

        Page<Glass> searchResults = glassService.searchGlassesPaginated(keyword, page, size);

        response.put("status", HttpStatus.OK.value());
        response.put("data", searchResults.getContent());
        response.put("currentPage", searchResults.getNumber());
        response.put("totalItems", searchResults.getTotalElements());
        response.put("totalPages", searchResults.getTotalPages());
        response.put("hasMore", !searchResults.isLast());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/glasses/{id}/update-stock")
    @RateLimiter(name = "writeEndpoints", fallbackMethod = "fallbackForWriteEndpoints")
    public ResponseEntity<Map<String, Object>> updateStockProduct(
            @PathVariable Long id,
            @RequestParam int quantity) {
        System.out.println("Update stock for glass with ID: " + id + ", quantity: " + quantity);
        Map<String, Object> response = new LinkedHashMap<>();
        GlassesUpdatedStockResponse GlassesUpdated = glassService.updateStock(id, quantity);
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Update stock success");
        response.put("data", GlassesUpdated);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/glassesDTO/{id}")
    @RateLimiter(name = "detailEndpoints", fallbackMethod = "fallbackForDetailEndpoints")
    public GlassesToOrderItemDTO findById(@PathVariable Long id) {
        Glass glass = glassService.findById(id);
        GlassesToOrderItemDTO glassesToOrderItemDTO = new GlassesToOrderItemDTO();
        glassesToOrderItemDTO.setName(glass.getName());
        glassesToOrderItemDTO.setBrand(glass.getBrand());
        glassesToOrderItemDTO.setImage_side_url(glass.getImageSideUrl());
        glassesToOrderItemDTO.setColor_name(glass.getColorName());
        glassesToOrderItemDTO.setColor_code(glass.getColorCode());
        return glassesToOrderItemDTO;
    }

    // Fallback methods for rate limited endpoints

    public ResponseEntity<Map<String, Object>> fallbackForSearchEndpoints(Exception ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "Search rate limit exceeded. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    public ResponseEntity<Map<String, Object>> fallbackForListingEndpoints(Exception ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "Product listing rate limit exceeded. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    public ResponseEntity<Map<String, Object>> fallbackForWriteEndpoints(Exception ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "Write operation rate limit exceeded. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    public ResponseEntity<Map<String, Object>> fallbackForDetailEndpoints(Exception ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "Product detail request rate limit exceeded. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    public ResponseEntity<Map<String, Object>> fallbackForSupportingEndpoints(Exception ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "Supporting data request rate limit exceeded. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    // Additional fallback method for GlassesToOrderItemDTO
    public GlassesToOrderItemDTO fallbackForDetailEndpoints(Long id, Exception ex) {
        // Return a simplified or empty DTO when rate limited
        GlassesToOrderItemDTO fallbackDTO = new GlassesToOrderItemDTO();
        fallbackDTO.setName("Rate limit exceeded");
        return fallbackDTO;
    }
}
