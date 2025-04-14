package iuh.fit.se.productservice.Services.impl;

import java.util.List;

import iuh.fit.se.productservice.Repositories.SpecificationRepository;
import iuh.fit.se.productservice.Services.CategoryService;
import iuh.fit.se.productservice.Services.GlassService;

import iuh.fit.se.productservice.dtos.GlassesDTO;
import iuh.fit.se.productservice.dtos.GlassesUpdatedStockResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iuh.fit.se.productservice.dtos.GlassStatistic;
import iuh.fit.se.productservice.entities.Category;
import iuh.fit.se.productservice.entities.Glass;
//import iuh.fit.se.productservice.exceptions.EntityNotFoundException;
import iuh.fit.se.productservice.Repositories.CategoryRepository;
import iuh.fit.se.productservice.Repositories.FrameSizeRepository;
import iuh.fit.se.productservice.Repositories.GlassRepository;
import org.modelmapper.ModelMapper;

@Service
public class GlassServiceImpl implements GlassService {

    @Autowired
    private GlassRepository glassRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpecificationRepository specificationRepository;

    @Autowired
    private FrameSizeRepository frameSizeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CategoryService categoryService ;

    @Override
    public List<Glass> findAll() {
        return glassRepository.findAll();
    }

    @Override
    public List<Glass> findByCategory(Long categoryId) {
        return glassRepository.findByCategory(categoryId);
    }

    @Override
    public List<Glass> findByCategoryAndGender(Long categoryId, boolean gender) {
        return glassRepository.findByCategoryAndGender(categoryId, gender);
    }

//    @Override
//    public List<GlassDTO> findByCategoryAndGenderAndFilter(Long categoryId, boolean gender, FilterRequest filter) {
//        return glassRepository.findAll((root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Always apply category and gender filters
//            predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
//            predicates.add(criteriaBuilder.equal(root.get("gender"), gender));
//
//            if (filter != null) {
//                // Price range filter
//                if (filter.getMinPrice() != null && !filter.getMinPrice().isEmpty()) {
//                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"),
//                            Double.parseDouble(filter.getMinPrice())));
//                }
//                if (filter.getMaxPrice() != null && !filter.getMaxPrice().isEmpty()) {
//                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"),
//                            Double.parseDouble(filter.getMaxPrice())));
//                }
//
//                // Brand filter
//                if (filter.getBrands() != null && !filter.getBrands().isEmpty()) {
//                    List<String> brands = List.of(filter.getBrands().split(","));
//                    predicates.add(root.get("brand").in(brands));
//                }
//
//                // Shape filter
//                if (filter.getShapes() != null && !filter.getShapes().isEmpty()) {
//                    List<String> shapes = List.of(filter.getShapes().split(","));
//                    predicates.add(root.get("specifications").get("shape").in(shapes));
//                }
//
//                // Material filter
//                if (filter.getMaterials() != null && !filter.getMaterials().isEmpty()) {
//                    List<String> materials = List.of(filter.getMaterials().split(","));
//                    predicates.add(root.get("specifications").get("material").in(materials));
//                }
//
//                // Color filter
//                if (filter.getColors() != null && !filter.getColors().isEmpty()) {
//                    List<String> colors = List.of(filter.getColors().split(","));
//                    predicates.add(root.get("colorName").in(colors));
//                }
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        }).stream().map(this::convertToGlassDTO).toList();
//    }

    @Override
    public Glass findById(Long id) {
        return glassRepository.findById(id)
                .orElse(null);
    }

    @Override
    @Transactional
    public Glass save(@Valid Glass glass) {
        return glassRepository.save(glass);
    }

    @Override
    public List<String> getAllBrand() {
        return glassRepository.getAllBrand();
    }

    @Override
    public List<String> getAllShape() {
        return glassRepository.getAllShape();
    }

    @Override
    public List<String> getAllMaterial() {
        return glassRepository.getAllMaterial();
    }

    @Override
    public List<String> getAllColor() {
        return glassRepository.getAllColor();
    }

    @Override
    public List<Glass> search(String keyword) {
        return glassRepository.searchGlasses(keyword);
    }

    @Override
    public List<GlassStatistic> getTop5Glasses() {
//        return glassRepository.getTop5Glasses();
        return null;
    }

    @Transactional
    @Override
    public GlassesDTO update(Long id, GlassesDTO glass) {
        if (this.findById(id) == null) {
            return null;
        }
        // Update
        System.out.println("glassDTO: " + glass);
        Glass glassConvert = this.convertToEntity(glass);
        System.out.println(glassConvert);
        Category category = categoryService.findById(glass.getCategory().getId());
        glass.setCategory(category);

        glassRepository.save(glassConvert);
        return this.convertToDTO(glassConvert);
    }

    private Glass convertToEntity(GlassesDTO glassDTO) {
        Glass glass = modelMapper.map(glassDTO, Glass.class);
        return glass;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!glassRepository.existsById(id)) {

        }

        glassRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Glass> searchGlasses(String keyword) {
        return glassRepository.searchGlasses(keyword);
    }

    @Override
    public GlassesUpdatedStockResponse updateStock(Long id, int quantity) {
        Glass glass = glassRepository.findById(id).get();
        if (glass == null) {
            throw new EntityNotFoundException("Glass not found with id: " + id);
        }
        glass.setStock(glass.getStock() - quantity);
        glassRepository.save(glass);
        GlassesUpdatedStockResponse response = new GlassesUpdatedStockResponse();
        response.setName(glass.getName());
        response.setColorName(glass.getColorName());
        response.setPrice(glass.getPrice());

        return response;
    }

    private GlassesDTO convertToDTO(Glass glass) {
        GlassesDTO glassDTO= modelMapper.map(glass, GlassesDTO.class);
        return glassDTO;
    }

}