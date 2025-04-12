package iuh.fit.se.productservice.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import iuh.fit.se.productservice.entities.Specifications;

@RepositoryRestResource(collectionResourceRel = "specifications", path = "specifications")
public interface SpecificationRepository extends JpaRepository<Specifications, Long>{

}
