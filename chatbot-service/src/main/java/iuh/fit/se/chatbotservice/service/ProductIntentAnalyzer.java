package iuh.fit.se.chatbotservice.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ProductIntentAnalyzer {

    @Data
    @NoArgsConstructor
    public static class ProductIntent {
        private boolean wantsEyeglasses = false;
        private boolean wantsSunglasses = false;
        private boolean forMen = false;
        private boolean forWomen = false;
        private String brand;
        private String shape;
        private String material;
        private String color;
        private Double minPrice;
        private Double maxPrice;
        private String searchKeyword;
        private boolean wantsMostExpensive = false;  // Muốn tìm kính đắt nhất
        private boolean wantsCheapest = false;       // Muốn tìm kính rẻ nhất
        private boolean wantsBoth = false;           // Muốn tìm cả kính mắt và kính râm
    }

    public ProductIntent analyzeIntent(String message) {
        message = message.toLowerCase();
        ProductIntent intent = new ProductIntent();

        // Detect product type
        if (containsAnyOf(message, "kính cận", "kính đọc", "eyeglasses", "kính mắt", "gọng kính")) {
            intent.setWantsEyeglasses(true);
        }

        if (containsAnyOf(message, "kính râm", "kính mát", "sunglasses", "chống nắng", "chống tia uv")) {
            intent.setWantsSunglasses(true);
        }

        if (intent.isWantsEyeglasses() && intent.isWantsSunglasses()) {
            intent.setWantsBoth(true);
        }

        // Detect most expensive/cheapest intent - cải thiện phát hiện "mắc nhất"
        if (containsAnyOf(message, "đắt nhất", "mắc nhất", "cao nhất", "giá cao nhất", "giá đắt nhất", "most expensive", "highest price", "luxury", "đắt tiền nhất", "giá cao")) {
            intent.setWantsMostExpensive(true);
            log.info("Phát hiện người dùng muốn tìm kính đắt nhất: '{}'", message);
        }

        if (containsAnyOf(message, "rẻ nhất", "giá rẻ nhất", "thấp nhất", "giá thấp nhất", "giá mềm nhất", "cheapest", "lowest price", "best price")) {
            intent.setWantsCheapest(true);
            log.info("Phát hiện người dùng muốn tìm kính rẻ nhất");
        }

        // Detect gender
        if (containsAnyOf(message, "nam", "đàn ông", "men", "man", "male", "boy", "cho nam", "của nam")) {
            intent.setForMen(true);
        }

        if (containsAnyOf(message, "nữ", "phụ nữ", "women", "woman", "female", "girl", "cho nữ", "của nữ")) {
            intent.setForWomen(true);
        }

        // Detect brand - mở rộng dựa trên thương hiệu thực tế
        for (String brand : new String[]{"rayban", "ray-ban", "gucci", "prada", "oakley", "dior", "chanel", "versace", "armani", "dolce", "gabbana", "burberry", "tom ford"}) {
            if (message.contains(brand)) {
                intent.setBrand(brand);
                break;
            }
        }

        // Detect shape - mở rộng dựa trên hình dạng gọng thực tế
        for (String shape : new String[]{"vuông", "tròn", "mèo", "cat-eye", "oval", "chữ nhật", "rectangle", "square", "round", "aviator", "pilot", "butterfly", "hexagon"}) {
            if (message.contains(shape)) {
                intent.setShape(shape);
                break;
            }
        }

        // Detect material - mở rộng dựa trên chất liệu thực tế
        for (String material : new String[]{"nhựa", "plastic", "kim loại", "metal", "titanium", "composite", "gỗ", "wood", "acetate"}) {
            if (message.contains(material)) {
                intent.setMaterial(material);
                break;
            }
        }

        // Detect color - mở rộng dựa trên màu sắc thực tế
        for (String color : new String[]{"đen", "trắng", "đỏ", "xanh", "vàng", "nâu", "hồng", "tím", "xám", "bạc", "vàng gold", "black", "white", "red", "blue", "yellow", "brown", "pink", "purple", "gray", "silver", "gold"}) {
            if (message.contains(color)) {
                intent.setColor(color);
                break;
            }
        }

        // Detect price range
        if (message.contains("giá rẻ") || message.contains("giá thấp") || message.contains("sinh viên")) {
            intent.setMinPrice(500000.0);
            intent.setMaxPrice(1500000.0);
        } else if (message.contains("cao cấp") || message.contains("sang trọng") || message.contains("luxury")) {
            intent.setMinPrice(3000000.0);
            intent.setMaxPrice(null);
        }

        // Extract specific price ranges if mentioned
        Pattern pricePattern = Pattern.compile("(\\d+)\\s*(triệu|tr|nghìn|k|ngàn)\\s*(?:đến|tới|-)\\s*(\\d+)\\s*(triệu|tr|nghìn|k|ngàn)");
        Matcher matcher = pricePattern.matcher(message);

        if (matcher.find()) {
            Double minAmount = Double.parseDouble(matcher.group(1));
            String minUnit = matcher.group(2);
            Double maxAmount = Double.parseDouble(matcher.group(3));
            String maxUnit = matcher.group(4);

            intent.setMinPrice(convertToVND(minAmount, minUnit));
            intent.setMaxPrice(convertToVND(maxAmount, maxUnit));
        } else {
            // Try single price patterns like "dưới 2 triệu" or "trên 500k"
            Pattern belowPattern = Pattern.compile("(?:dưới|không quá|less than|under|below)\\s*(\\d+)\\s*(triệu|tr|nghìn|k|ngàn)");
            Pattern abovePattern = Pattern.compile("(?:trên|hơn|more than|over|above)\\s*(\\d+)\\s*(triệu|tr|nghìn|k|ngàn)");

            Matcher belowMatcher = belowPattern.matcher(message);
            Matcher aboveMatcher = abovePattern.matcher(message);

            if (belowMatcher.find()) {
                Double amount = Double.parseDouble(belowMatcher.group(1));
                String unit = belowMatcher.group(2);
                intent.setMaxPrice(convertToVND(amount, unit));
            } else if (aboveMatcher.find()) {
                Double amount = Double.parseDouble(aboveMatcher.group(1));
                String unit = aboveMatcher.group(2);
                intent.setMinPrice(convertToVND(amount, unit));
            }
        }

        // Set search keyword if no specific filters are detected
        if (!intent.isWantsEyeglasses() && !intent.isWantsSunglasses() &&
                !intent.isForMen() && !intent.isForWomen() &&
                intent.getBrand() == null && intent.getShape() == null &&
                intent.getMaterial() == null && intent.getColor() == null &&
                intent.getMinPrice() == null && intent.getMaxPrice() == null &&
                !intent.isWantsMostExpensive() && !intent.isWantsCheapest()) {

            intent.setSearchKeyword(message);
        }

        return intent;
    }

    private boolean containsAnyOf(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Double convertToVND(Double amount, String unit) {
        switch (unit.toLowerCase()) {
            case "triệu":
            case "tr":
                return amount * 1000000;
            case "nghìn":
            case "ngàn":
            case "k":
                return amount * 1000;
            default:
                return amount;
        }
    }
}