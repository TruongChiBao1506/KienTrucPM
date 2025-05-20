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


-- Dumping database structure for glasses_db_user
CREATE DATABASE IF NOT EXISTS `glasses_db_user` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `glasses_db_user`;

-- Dumping structure for table glasses_db_user.users
CREATE TABLE IF NOT EXISTS `users` (
  `gender` bit(1) NOT NULL,
  `dob` datetime(6) DEFAULT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `fullname` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_user.users: ~8 rows (approximately)
INSERT INTO `users` (`gender`, `dob`, `id`, `user_id`, `address`, `fullname`, `phone`, `username`) VALUES
	(b'1', '2025-03-17 00:00:00.000000', 1, 1, '123HCM, VN', 'Truong Chi Bao', '0774025712', 'user1'),
	(b'0', '2003-02-07 07:00:00.000000', 9, 9, '123ABC', 'Nguyen D', '0908885661', 'userD3'),
	(b'0', '2025-03-22 07:00:00.000000', 16, 16, 'No address provided', 'Nguyen Van Chin', '0909887456', 'user9'),
	(b'1', '2025-04-17 00:00:00.000000', 17, 17, '123 ABC, VV', 'Truong Chi Bao', '0774025712', 'admin'),
	(b'1', '2003-06-14 00:00:00.000000', 18, 18, '1235HCM, VN', 'Truong Chi Bao', '0774025712', 'user2'),
	(b'1', '2025-04-26 00:00:00.000000', 19, 19, '123HCM, VN', 'Van Chi Hieu', '0774025712', 'chihieu4'),
	(b'0', '2000-09-14 00:00:00.000000', 27, 27, '123 ABC, VN', 'Truong Chi Bao', '0774025712', 'userB'),
	(b'1', '2003-06-14 07:00:00.000000', 28, 28, '123 ABC, VN', 'Nguyen Van D', '0774025712', 'userD');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
