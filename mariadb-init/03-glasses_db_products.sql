-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               11.3.2-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             12.6.0.6765
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for glasses_db_products
CREATE DATABASE IF NOT EXISTS `glasses_db_products` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `glasses_db_products`;

-- Dumping structure for table glasses_db_products.categories
CREATE TABLE IF NOT EXISTS `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_products.categories: ~2 rows (approximately)
INSERT INTO `categories` (`id`, `name`) VALUES
	(1, 'Sunglasses'),
	(2, 'EyeGlasses');

-- Dumping structure for table glasses_db_products.frame_size
CREATE TABLE IF NOT EXISTS `frame_size` (
  `bridge` double NOT NULL,
  `frame_weight` double NOT NULL,
  `frame_width` double NOT NULL,
  `lens_height` double NOT NULL,
  `lens_width` double NOT NULL,
  `temple_length` double NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_products.frame_size: ~15 rows (approximately)
INSERT INTO `frame_size` (`bridge`, `frame_weight`, `frame_width`, `lens_height`, `lens_width`, `temple_length`, `id`) VALUES
	(18, 25, 140, 40, 54, 145, 3),
	(16, 20, 145, 42, 58, 140, 5),
	(16, 30, 140, 45, 58, 140, 7),
	(14, 22, 145, 50, 58, 135, 12),
	(17, 12, 135, 44, 53, 149, 13),
	(18, 13, 131, 39, 53, 147, 14),
	(16, 20, 136, 40, 54, 145, 15),
	(18, 20, 133, 40, 52, 145, 16),
	(16, 20, 136, 40, 54, 145, 17),
	(18, 25, 140, 40, 54, 145, 18),
	(18, 25, 140, 40, 54, 145, 19),
	(14, 30, 138, 50, 58, 135, 20),
	(21, 27, 139, 42, 51, 145, 21),
	(18, 28, 137, 43, 55, 137, 22),
	(21, 27, 139, 42, 51, 145, 23);

-- Dumping structure for table glasses_db_products.glasses
CREATE TABLE IF NOT EXISTS `glasses` (
  `gender` bit(1) NOT NULL,
  `price` double DEFAULT NULL,
  `stock` int(11) NOT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `frame_size_id` bigint(20) DEFAULT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `specifications_id` bigint(20) DEFAULT NULL,
  `brand` varchar(255) DEFAULT NULL,
  `color_code` varchar(255) DEFAULT NULL,
  `color_name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image_front_url` varchar(255) DEFAULT NULL,
  `image_side_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1e7tf5lin8568i378a1pmr2m8` (`frame_size_id`),
  UNIQUE KEY `UKkmmc4ckgg4wt2e3xtoa5pv94m` (`specifications_id`),
  KEY `FK94moswykxx610dst8t0547qqk` (`category_id`),
  CONSTRAINT `FK94moswykxx610dst8t0547qqk` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `FK98oyvgvms7epqllxyl71uacg9` FOREIGN KEY (`specifications_id`) REFERENCES `specifications` (`id`),
  CONSTRAINT `FKjw1hok4kvj2ihxsfeeh4ysnpc` FOREIGN KEY (`frame_size_id`) REFERENCES `frame_size` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_products.glasses: ~15 rows (approximately)
INSERT INTO `glasses` (`gender`, `price`, `stock`, `category_id`, `frame_size_id`, `id`, `specifications_id`, `brand`, `color_code`, `color_name`, `description`, `image_front_url`, `image_side_url`, `name`) VALUES
	(b'1', 350000, 31, 2, 3, 1, 3, 'Ray-Ban', '#000000', 'Black', 'Classic black wayfarer glasses with UV protection.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746085431/images/1746085429545_886895510158-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746085434/images/1746085433143_886895510158-left.jpg', 'Classic Wayfarer'),
	(b'1', 200000, 40, 2, 5, 7, 5, 'Oakley', '#C0C0C0', 'Silver', 'Stylish silver aviator glasses with anti-glare coating.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746088197/images/1746088195385_751658244802-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746088200/images/1746088198895_751658244802-left.jpg', 'Modern Aviator'),
	(b'0', 400000, 29, 2, 7, 9, 7, 'Oakley', '#C0C0C0', 'Silver', 'Sleek silver aviator sunglasses with polarized lenses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746088223/images/1746088221630_730638384073-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746088226/images/1746088224863_730638384073-left.jpg', 'Aviator Deluxe'),
	(b'0', 500000, 28, 2, 12, 14, 12, 'XOXO', '#03a506', 'Green', 'Iconic gold aviator glasses with gradient UV lenses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746088636/images/1746088634092_751286331042-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746088640/images/1746088637068_751286331042-left.jpg', ' XOXO Green Vienna'),
	(b'0', 900000, 15, 2, 13, 15, 13, ' Nicole Miller', '#0087db', 'Blue', 'Vintage round tortoise frame with blue light protection.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746088878/images/1746088875750_730638384059-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746088881/images/1746088879695_730638384059-left.jpg', 'Nicole Miller Blue Edenroc'),
	(b'1', 450000, 28, 2, 14, 16, 14, ' Colours', '#464444', 'Gunmetal', 'Small frame suitable for narrow faces', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746089143/images/1746089141051_751658243461-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746089146/images/1746089143999_751658243461-left.jpg', ' Colours Gunmetal Blount'),
	(b'1', 750000, 20, 2, 15, 17, 15, ' Jones New York', '#000000', 'Black', 'Iconic gold aviator glasses with gradient UV lenses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746089409/images/1746089407030_886895449854-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746089412/images/1746089410507_886895449854-left.jpg', 'Jones New York Black Petite VJOP153'),
	(b'0', 550000, 50, 2, 16, 18, 16, ' Jones New York', '#acbcfb', 'Blue', 'Iconic gold aviator glasses with gradient UV lenses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746090210/images/1746090207037_751286331059-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746090215/images/1746090211429_751286331059-left.jpg', 'Jones New York Tortoise J775'),
	(b'1', 600000, 30, 2, 17, 19, 17, ' Nicole Miller', '#bd7800', 'Brown', 'Iconic gold aviator glasses with gradient UV lenses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746090433/images/1746090431571_716736397320-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746090435/images/1746090434182_716736397320-left.jpg', 'Nicole Miller Brown Hewes'),
	(b'0', 450000, 25, 1, 18, 20, 18, ' Coach', '#ffeb66', 'Gold', 'Classic black wayfarer glasses with UV protection.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746263826/images/1746263822947_725125956413-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746263829/images/1746263827025_725125956413-left.jpg', 'COACH 7079 Gold'),
	(b'0', 550000, 30, 1, 19, 21, 19, ' Coach', '#feb67c', 'Gold Rose', 'Classic black wayfarer glasses with UV protection.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746264965/images/1746264963045_725125965413-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746264968/images/1746264966049_725125965413-left.jpg', 'COACH GOLD 7064'),
	(b'0', 550000, 20, 1, 20, 22, 20, ' Coach', '#f5b400', 'Brown', 'Iconic gold aviator sunglasses with polarized lenses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746291377/images/1746291375158_725125108355-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746291380/images/1746291378307_725125108355-left.jpg', 'COACH GOLD 7111'),
	(b'0', 450000, 20, 1, 21, 23, 21, ' Coach', '#6b0df8', 'Purple', 'Retro-inspired half-frame tortoise sunglasses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746291728/images/1746291726366_725125930734-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746291733/images/1746291729761_725125930734-left.jpg', 'COACH PURPLE 8132'),
	(b'0', 600000, 20, 1, 22, 24, 22, ' Coach', '#cfa407', 'Brown', 'Matte black Holbrook sunglasses with impact-resistant lenses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746292819/images/1746292817501_725125956253-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746292822/images/1746292820686_725125956253-left.jpg', ' COACH Tortoise 8168'),
	(b'0', 400000, 20, 1, 23, 25, 23, ' Coach', '#b17306', 'Brown', 'Retro-inspired half-frame tortoise sunglasses.', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746293244/images/1746293242154_603429056964-front.jpg', 'https://res.cloudinary.com/diwy72evq/image/upload/v1746293246/images/1746293245289_603429056964-left-768x768.jpg', 'COACH Tortoise 8232');

-- Dumping structure for table glasses_db_products.specifications
CREATE TABLE IF NOT EXISTS `specifications` (
  `available_as_progressive_bifocal` bit(1) NOT NULL,
  `readers` bit(1) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `material` varchar(50) NOT NULL,
  `rim` varchar(50) NOT NULL,
  `shape` varchar(50) NOT NULL,
  `feature` varchar(100) NOT NULL,
  `frame_size_description` varchar(200) NOT NULL,
  `pd_range` varchar(255) NOT NULL,
  `prescription_range` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_products.specifications: ~15 rows (approximately)
INSERT INTO `specifications` (`available_as_progressive_bifocal`, `readers`, `id`, `material`, `rim`, `shape`, `feature`, `frame_size_description`, `pd_range`, `prescription_range`) VALUES
	(b'1', b'1', 3, 'Plastic', 'Full-rim', 'Wayfarer', 'UV protection, anti-scratch', 'Medium size frame suitable for most face shapes', '60-65', '-20.00 ~ +12.00'),
	(b'0', b'1', 5, 'Metal', 'Half-rim', 'Aviator', 'Anti-glare, lightweight', 'Large frame for wide faces', '58-68', '-15.00 ~ +10.00'),
	(b'1', b'0', 7, 'Metal', 'Full Rim', 'Aviator', 'Polarized lenses, anti-glare', 'Large size frame, ideal for broader faces', '58-68', '-18.00 ~ +8.00'),
	(b'1', b'1', 12, 'Plastic', 'Full-rim', 'Aviator', 'Gradient UV lenses, lightweight frame', 'Large frame, ideal for wide faces', '58-70', '-15.00 ~ +10.00'),
	(b'0', b'1', 13, 'Plastic', 'Full-rim', 'Square', 'Custom engraving,Lightweight,Universal Bridge Fit', 'Small frame suitable for narrow faces', '59-68', '-10.00 ~ +6.00'),
	(b'1', b'0', 14, 'Carbon Fiber', 'Full Rim', 'Browline', 'Nose Pads,Custom engraving,High Rx,Lightweight', 'Wrap design for active use and full coverage', '61-72', '-8.00 ~ +4.00'),
	(b'1', b'0', 15, 'Mixed', 'Full Rim', 'Square', 'Custom engraving,Universal Bridge Fit', 'Large frame, ideal for wide faces', '61-79', '-20.00 ~ +12.00'),
	(b'1', b'0', 16, 'Plastic', 'Full Rim', 'Square', 'Custom engraving,Universal Bridge Fit', 'Large frame, ideal for wide faces', '59-79', '-20.00 ~ +12.00'),
	(b'1', b'1', 17, 'Mixed', 'Full Rim', 'Square', 'Polarized lenses, anti-glare', 'Large frame, ideal for wide faces', '60-65', '-18.00 ~ +8.00'),
	(b'0', b'0', 18, 'Metal', 'Full Rim', 'Square', 'Polarized lenses, anti-glare', 'Medium size frame suitable for most face shapes', '60-70', '-20.00 ~ +12.00'),
	(b'0', b'0', 19, 'Mixed', 'Full Rim', 'Square', 'Anti-glare, lightweight', 'Medium size frame suitable for most face shapes', '60-70', '-20.00 ~ +12.00'),
	(b'1', b'0', 20, 'Stainless Steel', 'Full Rim', 'Square', 'Custom engraving,Universal Bridge Fit', 'Large size frame with a sleek look', '58-70', '-15.00 ~ +10.00'),
	(b'1', b'1', 21, 'Mixed', 'Half-rim', 'Browline', 'Anti-glare, UV protection', 'Medium to large frame with classic design', '60-72', '-18.00 ~ +10.00'),
	(b'0', b'0', 22, 'Plastic', 'Full Rim', 'Square', 'UV protection, anti-scratch', 'Medium fit frame with sporty design', '59-68', '-12.00 ~ +8.00'),
	(b'1', b'1', 23, 'Plastic', 'Full Rim', 'Rectangle', 'UV protection, anti-scratch', 'Medium to large frame with classic design', '60-72', '-18.00 ~ +10.00');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
