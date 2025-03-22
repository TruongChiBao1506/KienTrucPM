package iuh.fit.se.productservice.Services;

import java.util.List;

import iuh.fit.se.productservice.entities.Specifications;

public interface SpecificationService {
    List<Specifications> findAll();
    Specifications findById(Long id);
    Specifications save(Specifications specifications);
    Specifications update(Long id, Specifications specifications);
    boolean delete(Long id);
}