package iuh.fit.se.productservice.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import iuh.fit.se.productservice.entities.Glass;

@Repository
public interface GlassRepository extends JpaRepository<Glass, Long>, JpaSpecificationExecutor<Glass> {

    @Query("SELECT g FROM Glass g WHERE g.category.id = :categoryId")
    List<Glass> findByCategory(Long categoryId);

    @Query("SELECT g FROM Glass g WHERE g.category.id = :categoryId AND g.gender = :gender")
    List<Glass> findByCategoryAndGender(Long categoryId, boolean gender);

    @Query("SELECT DISTINCT g.brand FROM Glass g")
    List<String> getAllBrand();

    @Query("SELECT DISTINCT s.shape FROM Specifications s")
    List<String> getAllShape();

    @Query("SELECT DISTINCT s.material FROM Specifications s")
    List<String> getAllMaterial();

    @Query("SELECT DISTINCT g.colorName FROM Glass g")
    List<String> getAllColor();

    @Query("SELECT g FROM Glass g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(g.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Glass> searchGlasses(String keyword);

//    @Query(value = "SELECT new iuh.fit.se.productservice.dtos.GlassStatistic(g.id, g.name, g.imageSideUrl, g.price, COUNT(oi.orderItemId.productId) as totalSold) " +
//            "FROM Glass g JOIN OrderItem oi ON g.id = oi.orderItemId.productId " +
//            "GROUP BY g.id, g.name, g.imageSideUrl, g.price " +
//            "ORDER BY totalSold DESC")
//    List<GlassStatistic> getTop5Glasses();
}