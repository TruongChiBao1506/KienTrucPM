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


-- Dumping database structure for glasses_db_notifications
CREATE DATABASE IF NOT EXISTS `glasses_db_notifications` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `glasses_db_notifications`;

-- Dumping structure for table glasses_db_notifications.notifications
CREATE TABLE IF NOT EXISTS `notifications` (
  `is_read` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_notifications.notifications: ~35 rows (approximately)
INSERT INTO `notifications` (`is_read`, `created_at`, `id`, `order_id`, `message`) VALUES
	(b'1', '2025-04-13 23:08:40.626484', 1, 10, 'New Order: #202541323840'),
	(b'1', '2025-04-13 23:13:11.925768', 3, 11, 'New Order: #2025413231311'),
	(b'1', '2025-04-13 23:55:48.568457', 4, 13, 'New Order: #2025413235547'),
	(b'1', '2025-04-14 01:32:12.256803', 5, 14, 'New Order: #202541413211'),
	(b'1', '2025-04-14 01:41:02.580561', 6, 15, 'New Order: #20254141412'),
	(b'1', '2025-04-14 01:44:16.835525', 7, 16, 'New Order: #202541414416'),
	(b'1', '2025-04-14 01:54:58.084825', 8, 17, 'New Order: #202541415457'),
	(b'1', '2025-04-14 01:57:10.866452', 9, 18, 'New Order: #202541415710'),
	(b'1', '2025-04-14 02:01:38.246850', 10, 19, 'New Order: #20254142138'),
	(b'1', '2025-04-14 02:04:03.149405', 11, 20, 'New Order: #2025414243'),
	(b'1', '2025-04-14 15:30:29.048086', 12, 24, 'New Order: #2025414153027'),
	(b'1', '2025-04-14 16:44:20.051672', 13, 28, 'New Order: #202541416281'),
	(b'1', '2025-04-14 16:44:20.094917', 14, 28, 'New Order: #202541416281'),
	(b'1', '2025-04-14 16:50:56.752712', 15, 29, 'New Order: #2025414165035'),
	(b'1', '2025-04-14 16:50:56.802080', 16, 29, 'New Order: #2025414165035'),
	(b'1', '2025-04-14 16:59:30.704140', 17, 30, 'New Order: #202541416590'),
	(b'1', '2025-04-26 14:08:42.666405', 18, 31, 'New Order: #202542614841'),
	(b'1', '2025-04-26 14:09:15.334061', 19, 32, 'New Order: #202542614915'),
	(b'1', '2025-04-26 14:13:44.501745', 20, 33, 'New Order: #2025426141344'),
	(b'1', '2025-04-26 14:15:22.489587', 21, 34, 'New Order: #2025426141453'),
	(b'1', '2025-04-26 18:33:44.769485', 22, 35, 'New Order: #2025426183325'),
	(b'1', '2025-04-26 20:03:33.744892', 23, 36, 'New Order: #202542620333'),
	(b'1', '2025-04-26 20:04:31.700758', 24, 37, 'New Order: #202542620416'),
	(b'1', '2025-04-26 20:09:56.765722', 25, 38, 'New Order: #202542620932'),
	(b'1', '2025-04-26 20:11:27.455398', 26, 39, 'New Order: #202542620113'),
	(b'1', '2025-04-26 20:14:10.349140', 27, 40, 'New Order: #2025426201352'),
	(b'1', '2025-04-26 20:30:01.324548', 28, 41, 'New Order: #202542620301'),
	(b'1', '2025-05-20 00:19:17.159604', 29, 42, 'New Order: #202552001916'),
	(b'1', '2025-05-20 00:24:12.758898', 30, 43, 'New Order: #202552002346'),
	(b'1', '2025-05-20 00:50:28.570027', 31, 44, 'New Order: #202552005027'),
	(b'1', '2025-05-20 00:59:00.149955', 32, 45, 'New Order: #202552005858'),
	(b'1', '2025-05-20 00:59:13.671017', 33, 46, 'New Order: #202552005913'),
	(b'1', '2025-05-20 01:04:17.593833', 34, 47, 'New Order: #20255201355'),
	(b'1', '2025-05-20 01:09:41.088598', 35, 48, 'New Order: #20255201922'),
	(b'1', '2025-05-20 01:19:53.931220', 36, 49, 'New Order: #202552011952');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
