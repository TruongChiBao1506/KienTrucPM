package iuh.fit.se.productservice.Services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import iuh.fit.se.productservice.Services.GlassService;
//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iuh.fit.se.productservice.dtos.FilterRequest;
import iuh.fit.se.productservice.dtos.GlassDTO;
import iuh.fit.se.productservice.dtos.GlassStatistic;
import iuh.fit.se.productservice.entities.Category;
import iuh.fit.se.productservice.entities.FrameSize;
import iuh.fit.se.productservice.entities.Glass;
import iuh.fit.se.productservice.entities.Specifications;
//import iuh.fit.se.productservice.exceptions.EntityNotFoundException;
import iuh.fit.se.productservice.Repositories.CategoryRepository;
import iuh.fit.se.productservice.Repositories.FrameSizeRepository;
import iuh.fit.se.productservice.Repositories.GlassRepository;
import iuh.fit.se.productservice.Repositories.SpecificationsRepository;
import iuh.fit.se.productservice.Services.GlassService;
import jakarta.persistence.criteria.Predicate;

@Service
public class GlassServiceImpl implements GlassService {

    @Autowired
    private GlassRepository glassRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpecificationsRepository specificationsRepository;

    @Autowired
    private FrameSizeRepository frameSizeRepository;

//    @Autowired
//    private ModelMapper modelMapper;

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

    @Override
    public List<GlassDTO> findByCategoryAndGenderAndFilter(Long categoryId, boolean gender, FilterRequest filter) {
        return glassRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always apply category and gender filters
            predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            predicates.add(criteriaBuilder.equal(root.get("gender"), gender));

            if (filter != null) {
                // Price range filter
                if (filter.getMinPrice() != null && !filter.getMinPrice().isEmpty()) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"),
                            Double.parseDouble(filter.getMinPrice())));
                }
                if (filter.getMaxPrice() != null && !filter.getMaxPrice().isEmpty()) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"),
                            Double.parseDouble(filter.getMaxPrice())));
                }

                // Brand filter
                if (filter.getBrands() != null && !filter.getBrands().isEmpty()) {
                    List<String> brands = List.of(filter.getBrands().split(","));
                    predicates.add(root.get("brand").in(brands));
                }

                // Shape filter
                if (filter.getShapes() != null && !filter.getShapes().isEmpty()) {
                    List<String> shapes = List.of(filter.getShapes().split(","));
                    predicates.add(root.get("specifications").get("shape").in(shapes));
                }

                // Material filter
                if (filter.getMaterials() != null && !filter.getMaterials().isEmpty()) {
                    List<String> materials = List.of(filter.getMaterials().split(","));
                    predicates.add(root.get("specifications").get("material").in(materials));
                }

                // Color filter
                if (filter.getColors() != null && !filter.getColors().isEmpty()) {
                    List<String> colors = List.of(filter.getColors().split(","));
                    predicates.add(root.get("colorName").in(colors));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }).stream().map(this::convertToGlassDTO).toList();
    }

    @Override
    public Glass findById(Long id) {
        return glassRepository.findById(id)
                .orElseThrow();
    }

    @Override
    @Transactional
    public Glass save(Glass glass) {
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

    @Override
    @Transactional
    public Glass update(Long id, Glass updatedGlass) {
        Glass existingGlass = findById(id);

        // Update basic fields
        existingGlass.setName(updatedGlass.getName());
        existingGlass.setBrand(updatedGlass.getBrand());
        existingGlass.setPrice(updatedGlass.getPrice());
        existingGlass.setColorName(updatedGlass.getColorName());
        existingGlass.setColorCode(updatedGlass.getColorCode());
        existingGlass.setImageFrontUrl(updatedGlass.getImageFrontUrl());
        existingGlass.setImageSideUrl(updatedGlass.getImageSideUrl());
        existingGlass.setGender(updatedGlass.isGender());
        existingGlass.setStock(updatedGlass.getStock());
        existingGlass.setDescription(updatedGlass.getDescription());

        // Update category if provided
        if (updatedGlass.getCategory() != null && updatedGlass.getCategory().getId() != null) {
            Category category = categoryRepository.findById(updatedGlass.getCategory().getId())
                    .orElseThrow();
            existingGlass.setCategory(category);
        }

        // Update specifications if provided
        if (updatedGlass.getSpecifications() != null) {
            Specifications specs = existingGlass.getSpecifications();
            if (specs == null) {
                specs = new Specifications();
            }
            specs.setMaterial(updatedGlass.getSpecifications().getMaterial());
            specs.setShape(updatedGlass.getSpecifications().getShape());
            existingGlass.setSpecifications(specs);
        }

        // Update frameSize if provided
        if (updatedGlass.getFrameSize() != null) {
            FrameSize frameSize = existingGlass.getFrameSize();
            if (frameSize == null) {
                frameSize = new FrameSize();
            }
            frameSize.setLensWidth(updatedGlass.getFrameSize().getLensWidth());
            frameSize.setLensHeight(updatedGlass.getFrameSize().getLensHeight());
//            frameSize.setBridgeWidth(updatedGlass.getFrameSize().getBridgeWidth());
            frameSize.setTempleLength(updatedGlass.getFrameSize().getTempleLength());
            existingGlass.setFrameSize(frameSize);
        }

        return glassRepository.save(existingGlass);
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

    private GlassDTO convertToGlassDTO(Glass glass) {
        GlassDTO dto = new GlassDTO();

        dto.setId(glass.getId());
        dto.setName(glass.getName());
        dto.setBrand(glass.getBrand());
        dto.setPrice(glass.getPrice());
//        dto.setColorName(glass.getColorName());
        dto.setColorCode(glass.getColorCode());
        dto.setImageFrontUrl(glass.getImageFrontUrl());
        dto.setImageSideUrl(glass.getImageSideUrl());
//        dto.setGender(glass.isGender());
//        dto.setStock(glass.getStock());
//        dto.setDescription(glass.getDescription());

        if (glass.getCategory() != null) {
            dto.setCategoryId(glass.getCategory().getId());
            dto.setCategoryName(glass.getCategory().getName());
        }

        if (glass.getSpecifications() != null) {
            dto.setMaterial(glass.getSpecifications().getMaterial());
            dto.setShape(glass.getSpecifications().getShape());
        }

        if (glass.getFrameSize() != null) {
            dto.setLensWidth(glass.getFrameSize().getLensWidth());
            dto.setLensHeight(glass.getFrameSize().getLensHeight());
            dto.setBridgeWidth(glass.getFrameSize().getBridgeWidth());
            dto.setTempleLength(glass.getFrameSize().getTempleLength());
        }

        return dto;
    }
}