package iuh.fit.se.productservice.Services;

import java.util.List;

import iuh.fit.se.productservice.dtos.*;
import iuh.fit.se.productservice.entities.Glass;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

public interface GlassService {
    public List<Glass> findAll();
    
    public Page<Glass> findAllPaginated(int page, int size);

    public List<Glass> findByCategory(Long categoryId);
    
    public Page<Glass> findByCategoryPaginated(Long categoryId, int page, int size);

    public List<Glass> findByCategoryAndGender(Long categoryId, boolean gender);
    
    public Page<Glass> findByCategoryAndGenderPaginated(Long categoryId, boolean gender, int page, int size);

    public List<GlassDTO> findByCategoryAndGenderAndFilter(Long categoryId, boolean gender, FilterRequest filter);
    
    public Page<GlassDTO> findByCategoryAndGenderAndFilterPaginated(Long categoryId, boolean gender, FilterRequest filter, int page, int size);
    
    public Page<GlassDTO> findByCategoryAndFilterPaginated(Long categoryId, FilterRequest filter, int page, int size);

    public Glass findById(Long id);

    public Glass save(Glass glass);

    public List<String> getAllBrand();

    public List<String> getAllShape();

    public List<String> getAllMaterial();

    public List<String> getAllColor();

    public List<Glass> search(String keyword);
    
    public Page<Glass> searchPaginated(String keyword, int page, int size);

    public List<GlassStatistic> getTop5Glasses();

    public GlassesDTO update(Long id, GlassesDTO glass);

    public boolean delete(Long id);

    public List<Glass> searchGlasses(String keyword);
    
    public Page<Glass> searchGlassesPaginated(String keyword, int page, int size);

    public GlassesUpdatedStockResponse updateStock(Long id, int quantity);
}