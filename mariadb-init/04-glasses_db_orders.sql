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


-- Dumping database structure for glasses_db_orders
CREATE DATABASE IF NOT EXISTS `glasses_db_orders` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `glasses_db_orders`;

-- Dumping structure for table glasses_db_orders.orders
CREATE TABLE IF NOT EXISTS `orders` (
  `total_amount` double DEFAULT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_date` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `order_number` varchar(255) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `shipping_address` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_orders.orders: ~38 rows (approximately)
INSERT INTO `orders` (`total_amount`, `id`, `order_date`, `user_id`, `order_number`, `payment_method`, `shipping_address`, `status`) VALUES
	(804.46, 8, '2025-04-13 01:15:00.000000', 1, '20254132739', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PROCESSING'),
	(903.48, 9, '2025-04-13 01:15:00.000000', 1, '2025413215238', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(853.97, 10, '2025-04-13 01:15:00.000000', 1, '202541323840', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(853.97, 11, '2025-04-13 01:15:00.000000', 1, '2025413231311', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(351.49, 12, '2025-04-13 01:15:00.000000', 1, '2025413234828', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(351.49, 13, '2025-04-13 01:15:00.000000', 1, '2025413235547', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(502.48, 14, '2025-04-13 01:15:00.000000', 1, '202541413211', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(1606.46, 15, '2025-04-13 01:15:00.000000', 1, '20254141412', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(1806.96, 16, '2025-04-13 01:15:00.000000', 1, '202541414416', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(1757.45, 17, '2025-04-13 01:15:00.000000', 1, '202541415457', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(1757.45, 18, '2025-04-13 01:15:00.000000', 1, '202541415710', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(1757.45, 19, '2025-04-13 01:15:00.000000', 1, '20254142138', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(1757.45, 20, '2025-04-13 01:15:00.000000', 1, '2025414243', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(1757.45, 21, '2025-04-13 01:15:00.000000', 9, '2025414151811', 'VNPAY', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(2750000, 22, '2025-04-13 01:15:00.000000', 9, '2025414152044', 'VNPAY', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(550000, 23, '2025-04-13 01:15:00.000000', 9, '2025414152849', 'VNPAY', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(550000, 24, '2025-04-13 01:15:00.000000', 1, '2025414153027', 'COD', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PENDING'),
	(550000, 27, '2025-04-13 01:15:00.000000', 1, '202541416134', 'VNPAY', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PAID'),
	(750000, 28, '2025-04-13 01:15:00.000000', 1, '202541416281', 'VNPAY', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PAID'),
	(550000, 29, '2025-04-13 01:15:00.000000', 1, '2025414165035', 'VNPAY', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PAID'),
	(550000, 30, '2025-04-13 01:15:00.000000', 1, '202541416590', 'VNPAY', '123 Nguyễn Văn A, Quận 1, TP. Hồ Chí Minh', 'PAID'),
	(350000, 33, '2025-04-26 07:13:44.471000', 1, '2025426141344', 'COD', 'No address provided', 'PENDING'),
	(400000, 34, '2025-04-26 07:14:53.803000', 1, '2025426141453', 'VNPAY', 'No address provided', 'PAID'),
	(900000, 35, '2025-04-26 11:33:25.314000', 19, '2025426183325', 'VNPAY', 'No address provided', 'PAID'),
	(700000, 36, '2025-04-26 13:03:33.681000', 1, '202542620333', 'COD', 'No address provided', 'PENDING'),
	(1050000, 37, '2025-04-26 13:04:16.245000', 1, '202542620416', 'VNPAY', 'No address provided', 'PAID'),
	(350000, 38, '2025-04-26 13:09:32.355000', 1, '202542620932', 'VNPAY', 'No address provided', 'PAID'),
	(350000, 39, '2025-04-26 13:11:03.203000', 1, '202542620113', 'VNPAY', 'No address provided', 'PAID'),
	(400000, 40, '2025-04-26 13:13:52.188000', 1, '2025426201352', 'VNPAY', 'No address provided', 'PAID'),
	(1400000, 41, '2025-04-26 13:30:01.209000', 1, '202542620301', 'COD', 'No address provided', 'PENDING'),
	(700000, 42, '2025-05-19 17:19:16.260000', 1, '202552001916', 'COD', '123HCM, VN', 'PENDING'),
	(450000, 43, '2025-05-19 17:23:46.692000', 1, '202552002346', 'VNPAY', '123HCM, VN', 'PAID'),
	(450000, 44, '2025-05-19 17:50:27.722000', 1, '202552005027', 'COD', '123HCM, VN', 'PENDING'),
	(350000, 45, '2025-05-19 17:58:58.561000', 1, '202552005858', 'COD', '123HCM, VN', 'PENDING'),
	(200000, 46, '2025-05-19 17:59:13.593000', 1, '202552005913', 'COD', '123HCM, VN', 'PENDING'),
	(500000, 47, '2025-05-19 18:03:55.892000', 1, '20255201355', 'VNPAY', '123HCM, VN', 'PAID'),
	(500000, 48, '2025-05-19 18:09:22.261000', 1, '20255201922', 'VNPAY', '123HCM, VN', 'PAID'),
	(200000, 49, '2025-05-19 18:19:52.684000', 1, '202552011952', 'COD', '123HCM, VN', 'PENDING');

-- Dumping structure for table glasses_db_orders.order_items
CREATE TABLE IF NOT EXISTS `order_items` (
  `quantity` int(11) NOT NULL,
  `total_price` double DEFAULT NULL,
  `unit_price` double DEFAULT NULL,
  `order_id` bigint(20) NOT NULL,
  `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_orders.order_items: ~60 rows (approximately)
INSERT INTO `order_items` (`quantity`, `total_price`, `unit_price`, `order_id`, `order_item_id`, `product_id`) VALUES
	(4, 603.96, 150.99, 8, 5, 1),
	(1, 200.5, 200.5, 8, 6, 7),
	(2, 301.98, 150.99, 9, 7, 1),
	(3, 601.5, 200.5, 9, 8, 7),
	(3, 452.97, 150.99, 10, 9, 1),
	(2, 401, 200.5, 10, 10, 7),
	(3, 452.97, 150.99, 11, 11, 1),
	(2, 401, 200.5, 11, 12, 7),
	(1, 150.99, 150.99, 12, 13, 1),
	(1, 200.5, 200.5, 12, 14, 7),
	(1, 150.99, 150.99, 13, 15, 1),
	(1, 200.5, 200.5, 13, 16, 7),
	(2, 301.98, 150.99, 14, 17, 1),
	(1, 200.5, 200.5, 14, 18, 7),
	(4, 603.96, 150.99, 15, 19, 1),
	(5, 1002.5, 200.5, 15, 20, 7),
	(4, 603.96, 150.99, 16, 21, 1),
	(6, 1203, 200.5, 16, 22, 7),
	(5, 754.95, 150.99, 17, 23, 1),
	(5, 1002.5, 200.5, 17, 24, 7),
	(5, 754.95, 150.99, 18, 25, 1),
	(5, 1002.5, 200.5, 18, 26, 7),
	(5, 754.95, 150.99, 19, 27, 1),
	(5, 1002.5, 200.5, 19, 28, 7),
	(5, 754.95, 150.99, 20, 29, 1),
	(5, 1002.5, 200.5, 20, 30, 7),
	(5, 754.95, 150.99, 21, 31, 1),
	(5, 1002.5, 200.5, 21, 32, 7),
	(5, 1750000, 350000, 22, 33, 1),
	(5, 1000000, 200000, 22, 34, 7),
	(1, 350000, 350000, 23, 35, 1),
	(1, 200000, 200000, 23, 36, 7),
	(1, 350000, 350000, 24, 37, 1),
	(1, 200000, 200000, 24, 38, 7),
	(1, 350000, 350000, 27, 43, 1),
	(1, 200000, 200000, 27, 44, 7),
	(1, 350000, 350000, 28, 45, 1),
	(2, 400000, 200000, 28, 46, 7),
	(1, 350000, 350000, 29, 47, 1),
	(1, 200000, 200000, 29, 48, 7),
	(1, 350000, 350000, 30, 49, 1),
	(1, 200000, 200000, 30, 50, 7),
	(1, 350000, 350000, 33, 53, 1),
	(1, 400000, 400000, 34, 54, 9),
	(2, 700000, 350000, 35, 55, 1),
	(1, 200000, 200000, 35, 56, 7),
	(2, 700000, 350000, 36, 57, 1),
	(3, 1050000, 350000, 37, 58, 1),
	(1, 350000, 350000, 38, 59, 1),
	(1, 350000, 350000, 39, 60, 1),
	(2, 400000, 200000, 40, 61, 7),
	(4, 1400000, 350000, 41, 62, 1),
	(2, 700000, 350000, 42, 63, 1),
	(1, 450000, 450000, 43, 64, 16),
	(1, 450000, 450000, 44, 65, 16),
	(1, 350000, 350000, 45, 66, 1),
	(1, 200000, 200000, 46, 67, 7),
	(1, 500000, 500000, 47, 68, 14),
	(1, 500000, 500000, 48, 69, 14),
	(1, 200000, 200000, 49, 70, 7);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
