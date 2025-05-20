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


-- Dumping database structure for glasses_db_account
CREATE DATABASE IF NOT EXISTS `glasses_db_account` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `glasses_db_account`;

-- Dumping structure for table glasses_db_account.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','SUPER','USER') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping structure for table glasses_db_account.refresh_token
CREATE TABLE IF NOT EXISTS `refresh_token` (
  `expiry_date` datetime(6) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `token` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr4k4edos30bx9neoq81mdvwph` (`token`),
  UNIQUE KEY `UKf95ixxe7pa48ryn1awmh2evt7` (`user_id`),
  CONSTRAINT `FKjtx87i0jvq2svedphegvdwcuy` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=223 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table glasses_db_account.users: ~8 rows (approximately)
INSERT INTO `users` (`id`, `email`, `password`, `username`, `role`) VALUES
	(1, 'chibaotruongds@gmail.com', '$2a$10$/QMbunf6z0bhfLTl.GFLbeDpkG6bzCL/UeogcCBOu.xeAhWcAUQrm', 'user1', 'USER'),
	(9, 'NguyenD@gmail.com', '$2a$10$jvfxZ1E6stKJxCZkVAATU.gMejWSqNxE83v.dxzRwqHeLtpkPFI8a', 'userD3', 'USER'),
	(16, 'vanChin@gmail.com', '$2a$10$y5gRAGM0XRl2hyIvxwWVHeqMbtzXgp.K75gyWYddfLTTCIAwLjNc2', 'user9', 'USER'),
	(17, 'chibaotruong1506@gmail.com', '$2a$10$6F4SNZ9idfnf148zEpu9ZO0G.A2cIwJYvUrAZvmuzXdS9OESyXCcy', 'admin', 'SUPER'),
	(18, 'windows012b@gmail.com', '$2a$10$Mujx/BtYOD2Lr6WvyV6Awe9wFi2GEKc3xUE1rq/bi0yyfP22u6oIO', 'user2', 'USER'),
	(19, 'vanchihieu5@gmail.com', '$2a$10$dWGHcmSl34dzXsjPY/XD9uobWbZTzpHj/G1ewsTDPvvmmMrAEiPGq', 'chihieu4', 'USER'),
	(27, 'sendingemaileventhub@gmail.com', '$2a$10$lOQNQ6CeFJtCqbhfay0ncukrGdKfH9rUaw61jBnriYJ.vd.uF/C.q', 'userB', 'USER'),
	(28, 'testemail123vn@gmail.com', '$2a$10$2srTkFsU3uc2BiSW1mOJmO4fs6a3uV9braYD/u1aQBi3D81i1gB4y', 'userD', 'USER');

-- Dumping data for table glasses_db_account.refresh_token: ~2 rows (approximately)
INSERT INTO `refresh_token` (`expiry_date`, `id`, `user_id`, `token`) VALUES
	('2025-04-19 18:06:18.553078', 13, 9, 'a71c5bc3-3ed5-4b64-89fe-8ba4e84f5eae'),
	('2025-04-25 15:31:01.516481', 15, 18, '5dd47879-ca24-4996-a002-a161137161b9');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
