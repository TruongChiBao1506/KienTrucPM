package iuh.fit.se.productservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {
    private String brands;
    private String shapes;
    private String materials;
    private String colors;
    private String minPrice;
    private String maxPrice;

    public String getBrands() {
        return brands;
    }

    public String getShapes() {
        return shapes;
    }

    public String getMaterials() {
        return materials;
    }

    public String getColors() {
        return colors;
    }

    public String getMinPrice() {
        return minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }

    public void setShapes(String shapes) {
        this.shapes = shapes;
    }

    public void setMaterials(String materials) {
        this.materials = materials;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }
}