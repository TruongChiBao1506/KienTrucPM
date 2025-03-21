package iuh.fit.se.productservice.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iuh.fit.se.productservice.entities.Specifications;

@Repository
public interface SpecificationsRepository extends JpaRepository<Specifications, Long> {
}