package iuh.fit.se.chatbotservice.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

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
        private boolean wantsMostExpensive = false;
        private boolean wantsCheapest = false;
        private boolean wantsBoth = false;
    }

    public ProductIntent analyzeIntent(String message) {
        message = message.toLowerCase();
        ProductIntent intent = new ProductIntent();

        // Detect product type with enhanced phrases
        List<String> eyeglassPhrases = Arrays.asList(
                "kính cận", "kính đọc", "eyeglasses", "kính mắt", "gọng kính",
                "kính cận thị", "kính viễn", "kính cận thị", "prescription",
                "glasses for reading", "optical", "eye glasses", "kính mắt thường"
        );

        List<String> sunglassPhrases = Arrays.asList(
                "kính râm", "kính mát", "sunglasses", "chống nắng", "chống tia uv",
                "kính đen", "kính che nắng", "sun glasses", "kính đi biển",
                "kính chống nắng", "kính đi ngoài trời", "kính uv"
        );

        for (String phrase : eyeglassPhrases) {
            if (message.contains(phrase)) {
                intent.setWantsEyeglasses(true);
                break;
            }
        }

        for (String phrase : sunglassPhrases) {
            if (message.contains(phrase)) {
                intent.setWantsSunglasses(true);
                break;
            }
        }

        if (intent.isWantsEyeglasses() && intent.isWantsSunglasses()) {
            intent.setWantsBoth(true);
            log.info("User wants both eyeglasses and sunglasses");
        }

        // Detect most expensive/cheapest intent with enhanced phrases
        List<String> expensivePhrases = Arrays.asList(
                "đắt nhất", "mắc nhất", "cao nhất", "giá cao nhất", "giá đắt nhất",
                "most expensive", "highest price", "luxury", "đắt tiền nhất", "giá cao",
                "cao cấp nhất", "sang trọng nhất", "premium", "top tier", "high-end",
                "cao cấp", "đắt", "đắt đỏ", "giá cao"
        );

        List<String> cheapestPhrases = Arrays.asList(
                "rẻ nhất", "giá rẻ nhất", "thấp nhất", "giá thấp nhất", "giá mềm nhất",
                "cheapest", "lowest price", "best price", "giá tốt nhất", "giá tốt",
                "phải chăng nhất", "affordable", "budget", "tiết kiệm nhất"
        );

        for (String phrase : expensivePhrases) {
            if (message.contains(phrase)) {
                intent.setWantsMostExpensive(true);
                log.info("User wants the most expensive glasses: '{}'", message);
                break;
            }
        }

        for (String phrase : cheapestPhrases) {
            if (message.contains(phrase)) {
                intent.setWantsCheapest(true);
                log.info("User wants the cheapest glasses");
                break;
            }
        }

        // Detect gender with enhanced phrases
        List<String> malePhrases = Arrays.asList(
                "nam", "đàn ông", "men", "man", "male", "boy", "cho nam", "của nam",
                "kính nam", "dành cho nam", "nam giới", "phái nam", "con trai"
        );

        List<String> femalePhrases = Arrays.asList(
                "nữ", "phụ nữ", "women", "woman", "female", "girl", "cho nữ", "của nữ",
                "kính nữ", "dành cho nữ", "nữ giới", "phái nữ", "con gái"
        );

        for (String phrase : malePhrases) {
            if (message.contains(phrase)) {
                intent.setForMen(true);
                break;
            }
        }

        for (String phrase : femalePhrases) {
            if (message.contains(phrase)) {
                intent.setForWomen(true);
                break;
            }
        }

        // Detect brand - enhanced list
        String[] brands = {
                "rayban", "ray-ban", "ray ban", "gucci", "prada", "oakley", "dior",
                "chanel", "versace", "armani", "dolce", "gabbana", "burberry",
                "tom ford", "coach", "fendi", "persol", "miu miu", "giorgio armani",
                "michael kors", "polaroid", "carrera", "police"
        };

        for (String brand : brands) {
            if (message.contains(brand)) {
                intent.setBrand(brand);
                log.info("Detected brand: {}", brand);
                break;
            }
        }

        // Detect shape - enhanced list
        String[] shapes = {
                "vuông", "tròn", "mèo", "cat-eye", "cat eye", "oval", "chữ nhật",
                "rectangle", "square", "round", "aviator", "pilot", "butterfly",
                "hexagon", "shield", "clubmaster", "browline", "wayfarer", "geometric",
                "oversized", "hình thang", "pentagon", "bầu dục", "hình bầu dục"
        };

        for (String shape : shapes) {
            if (message.contains(shape)) {
                intent.setShape(shape);
                log.info("Detected shape: {}", shape);
                break;
            }
        }

        // Detect material - enhanced list
        String[] materials = {
                "nhựa", "plastic", "kim loại", "metal", "titanium", "composite",
                "gỗ", "wood", "acetate", "carbon fiber", "stainless steel",
                "thép không gỉ", "hợp kim", "alloy", "nylon", "carbon", "fiber",
                "sợi carbon", "thép", "steel", "nhôm", "aluminum", "gọng nhựa",
                "gọng kim loại", "gọng thép"
        };

        for (String material : materials) {
            if (message.contains(material)) {
                intent.setMaterial(material);
                log.info("Detected material: {}", material);
                break;
            }
        }

        // Detect color - enhanced list
        String[] colors = {
                "đen", "trắng", "đỏ", "xanh lá", "xanh dương", "vàng", "nâu", "hồng",
                "tím", "xám", "bạc", "vàng gold", "gold", "black", "white", "red",
                "green", "blue", "yellow", "brown", "pink", "purple", "gray", "silver",
                "navy", "beige", "khaki", "xanh rêu", "transparent", "tortoise",
                "xanh lá cây", "rose gold", "vàng hồng", "cam", "orange", "trong suốt",
                "multicolor", "nhiều màu", "amber", "burgundy", "wine red"
        };

        for (String color : colors) {
            if (message.contains(color)) {
                intent.setColor(color);
                log.info("Detected color: {}", color);
                break;
            }
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
            log.info("Detected price range: {}₫ to {}₫", intent.getMinPrice(), intent.getMaxPrice());
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
                log.info("Detected maximum price: {}₫", intent.getMaxPrice());
            } else if (aboveMatcher.find()) {
                Double amount = Double.parseDouble(aboveMatcher.group(1));
                String unit = aboveMatcher.group(2);
                intent.setMinPrice(convertToVND(amount, unit));
                log.info("Detected minimum price: {}₫", intent.getMinPrice());
            }
        }

        // Set reasonable defaults for price categories
        if (message.contains("giá rẻ") || message.contains("giá thấp") || message.contains("sinh viên")) {
            if (intent.getMinPrice() == null) intent.setMinPrice(0.0);
            if (intent.getMaxPrice() == null) intent.setMaxPrice(1500000.0);
            log.info("Set default budget price range: {}₫ to {}₫", intent.getMinPrice(), intent.getMaxPrice());
        } else if (message.contains("cao cấp") || message.contains("sang trọng") || message.contains("luxury")) {
            if (intent.getMinPrice() == null) intent.setMinPrice(3000000.0);
            log.info("Set default luxury price minimum: {}₫", intent.getMinPrice());
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