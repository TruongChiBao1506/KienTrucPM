package iuh.fit.se.productservice.Services;

import java.util.List;

import iuh.fit.se.productservice.dtos.FilterRequest;
import iuh.fit.se.productservice.dtos.GlassDTO;
import iuh.fit.se.productservice.dtos.GlassStatistic;
import iuh.fit.se.productservice.entities.Glass;

public interface GlassService {
    public List<Glass> findAll();

    public List<Glass> findByCategory(Long categoryId);

    public List<Glass> findByCategoryAndGender(Long categoryId, boolean gender);

    public List<GlassDTO> findByCategoryAndGenderAndFilter(Long categoryId, boolean gender, FilterRequest filter);

    public Glass findById(Long id);

    public Glass save(Glass glass);

    public List<String> getAllBrand();

    public List<String> getAllShape();

    public List<String> getAllMaterial();

    public List<String> getAllColor();

    public List<Glass> search(String keyword);

    public List<GlassStatistic> getTop5Glasses();

    public Glass update(Long id, Glass glass);

    public boolean delete(Long id);

    public List<Glass> searchGlasses(String keyword);
}