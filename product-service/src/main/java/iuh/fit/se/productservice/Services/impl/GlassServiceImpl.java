package iuh.fit.se.productservice.Services.impl;

import java.util.ArrayList;
import java.util.List;

import iuh.fit.se.productservice.Repositories.SpecificationRepository;
import iuh.fit.se.productservice.Services.CategoryService;
import iuh.fit.se.productservice.Services.GlassService;

import iuh.fit.se.productservice.dtos.FilterRequest;
import iuh.fit.se.productservice.dtos.GlassDTO;
import iuh.fit.se.productservice.dtos.GlassesDTO;
import jakarta.persistence.criteria.Predicate;
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
    public GlassesDTO saveDTO(@Valid GlassesDTO glassDTO) {
        // Convert DTO to entity
        Glass glass = modelMapper.map(glassDTO, Glass.class);

        // Set ID to null to ensure we create a new entity
        glass.setId(null);

        // Save related entities if they're new
        if (glass.getSpecifications() != null) {
            if (glass.getSpecifications().getId() != null) {
                glass.setSpecifications(specificationRepository.findById(glass.getSpecifications().getId()).orElse(null));
            } else {
                glass.setSpecifications(specificationRepository.save(glass.getSpecifications()));
            }
        }

        if (glass.getFrameSize() != null) {
            if (glass.getFrameSize().getId() != null) {
                glass.setFrameSize(frameSizeRepository.findById(glass.getFrameSize().getId()).orElse(null));
            } else {
                glass.setFrameSize(frameSizeRepository.save(glass.getFrameSize()));
            }
        }

        if (glass.getCategory() != null) {
            if (glass.getCategory().getId() != null) {
                glass.setCategory(categoryRepository.findById(glass.getCategory().getId()).orElse(null));
            } else {
                glass.setCategory(categoryRepository.save(glass.getCategory()));
            }
        }

        // Save the main entity
        Glass savedGlass = glassRepository.save(glass);

        // Convert back to DTO and return
        return modelMapper.map(savedGlass, GlassesDTO.class);
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
    public GlassesDTO update(Long id, GlassesDTO glassDTO) {
        // Check if glass exists
        Glass existingGlass = glassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Glass not found with id: " + id));

        // Update basic properties
        existingGlass.setName(glassDTO.getName());
        existingGlass.setBrand(glassDTO.getBrand());
        existingGlass.setPrice(glassDTO.getPrice());
        existingGlass.setColorName(glassDTO.getColorName());
        existingGlass.setColorCode(glassDTO.getColorCode());
        existingGlass.setImageFrontUrl(glassDTO.getImageFrontUrl());
        existingGlass.setImageSideUrl(glassDTO.getImageSideUrl());
        existingGlass.setGender(glassDTO.isGender());
        existingGlass.setStock(glassDTO.getStock());
        existingGlass.setDescription(glassDTO.getDescription());

        // Handle Specifications
        if (glassDTO.getSpecifications() != null) {
            if (glassDTO.getSpecifications().getId() != null) {
                existingGlass.setSpecifications(specificationRepository.findById(glassDTO.getSpecifications().getId())
                        .orElse(null));
            } else {
                if (existingGlass.getSpecifications() != null) {
                    // Update existing specifications
                    existingGlass.getSpecifications().setPdRange(glassDTO.getSpecifications().getPdRange());
                    existingGlass.getSpecifications().setPrescriptionRange(glassDTO.getSpecifications().getPrescriptionRange());
                    existingGlass.getSpecifications().setAvailableAsProgressiveBifocal(glassDTO.getSpecifications().getAvailableAsProgressiveBifocal());
                    existingGlass.getSpecifications().setReaders(glassDTO.getSpecifications().getReaders());
                    existingGlass.getSpecifications().setFrameSizeDescription(glassDTO.getSpecifications().getFrameSizeDescription());
                    existingGlass.getSpecifications().setRim(glassDTO.getSpecifications().getRim());
                    existingGlass.getSpecifications().setShape(glassDTO.getSpecifications().getShape());
                    existingGlass.getSpecifications().setMaterial(glassDTO.getSpecifications().getMaterial());
                    existingGlass.getSpecifications().setFeature(glassDTO.getSpecifications().getFeature());
                } else {
                    // Create new specifications
                    existingGlass.setSpecifications(specificationRepository.save(glassDTO.getSpecifications()));
                }
            }
        }

        // Handle FrameSize
        if (glassDTO.getFrameSize() != null) {
            if (glassDTO.getFrameSize().getId() != null) {
                existingGlass.setFrameSize(frameSizeRepository.findById(glassDTO.getFrameSize().getId())
                        .orElse(null));
            } else {
                if (existingGlass.getFrameSize() != null) {
                    // Update existing frameSize
                    existingGlass.getFrameSize().setFrameWidth(glassDTO.getFrameSize().getFrameWidth());
                    existingGlass.getFrameSize().setLensWidth(glassDTO.getFrameSize().getLensWidth());
                    existingGlass.getFrameSize().setBridge(glassDTO.getFrameSize().getBridge());
                    existingGlass.getFrameSize().setTempleLength(glassDTO.getFrameSize().getTempleLength());
                    existingGlass.getFrameSize().setLensHeight(glassDTO.getFrameSize().getLensHeight());
                    existingGlass.getFrameSize().setFrameWeight(glassDTO.getFrameSize().getFrameWeight());
                } else {
                    // Create new frameSize
                    existingGlass.setFrameSize(frameSizeRepository.save(glassDTO.getFrameSize()));
                }
            }
        }

        // Handle Category
        if (glassDTO.getCategory() != null && glassDTO.getCategory().getId() != null) {
            existingGlass.setCategory(categoryRepository.findById(glassDTO.getCategory().getId())
                    .orElse(null));
        }

        // Save the updated glass
        Glass updatedGlass = glassRepository.save(existingGlass);

        // Convert back to DTO and return
        return modelMapper.map(updatedGlass, GlassesDTO.class);
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


    private GlassDTO convertToGlassDTO(Glass glass) {
        GlassDTO glassDTO = modelMapper.map(glass, GlassDTO.class);
        return glassDTO;
    }

}