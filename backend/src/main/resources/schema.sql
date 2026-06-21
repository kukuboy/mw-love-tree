-- LoveTree Database Schema

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(128) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(64),
    `avatar` VARCHAR(512),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `couple` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user1_id` BIGINT NOT NULL,
    `user2_id` BIGINT,
    `invite_code` VARCHAR(8) NOT NULL UNIQUE,
    `anniversary` DATE,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tree` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `couple_id` BIGINT NOT NULL UNIQUE,
    `stage` VARCHAR(20) DEFAULT 'SEED',
    `leaf_count` INT DEFAULT 0,
    `flower_count` INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `love_event` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `couple_id` BIGINT NOT NULL,
    `author_id` BIGINT NOT NULL,
    `title` VARCHAR(128) NOT NULL,
    `content` TEXT,
    `event_type` VARCHAR(32) NOT NULL,
    `event_date` DATE NOT NULL,
    `images` JSON,
    `mood` VARCHAR(16),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `couple_id` BIGINT NOT NULL,
    `from_id` BIGINT NOT NULL,
    `to_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `is_read` TINYINT(1) DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `photo` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `couple_id` BIGINT NOT NULL,
    `uploader_id` BIGINT NOT NULL,
    `url` VARCHAR(512) NOT NULL,
    `caption` VARCHAR(256),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Foreign keys
ALTER TABLE `couple` ADD CONSTRAINT `fk_couple_user1` FOREIGN KEY (`user1_id`) REFERENCES `user`(`id`);
ALTER TABLE `couple` ADD CONSTRAINT `fk_couple_user2` FOREIGN KEY (`user2_id`) REFERENCES `user`(`id`);
ALTER TABLE `tree` ADD CONSTRAINT `fk_tree_couple` FOREIGN KEY (`couple_id`) REFERENCES `couple`(`id`);
ALTER TABLE `love_event` ADD CONSTRAINT `fk_love_event_couple` FOREIGN KEY (`couple_id`) REFERENCES `couple`(`id`);
ALTER TABLE `love_event` ADD CONSTRAINT `fk_love_event_author` FOREIGN KEY (`author_id`) REFERENCES `user`(`id`);
ALTER TABLE `message` ADD CONSTRAINT `fk_message_couple` FOREIGN KEY (`couple_id`) REFERENCES `couple`(`id`);
ALTER TABLE `message` ADD CONSTRAINT `fk_message_from` FOREIGN KEY (`from_id`) REFERENCES `user`(`id`);
ALTER TABLE `message` ADD CONSTRAINT `fk_message_to` FOREIGN KEY (`to_id`) REFERENCES `user`(`id`);
ALTER TABLE `photo` ADD CONSTRAINT `fk_photo_couple` FOREIGN KEY (`couple_id`) REFERENCES `couple`(`id`);
ALTER TABLE `photo` ADD CONSTRAINT `fk_photo_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user`(`id`);

-- Indexes
CREATE INDEX `idx_couple_user1` ON `couple`(`user1_id`);
CREATE INDEX `idx_couple_user2` ON `couple`(`user2_id`);
CREATE INDEX `idx_couple_invite_code` ON `couple`(`invite_code`);
CREATE INDEX `idx_love_event_couple` ON `love_event`(`couple_id`);
CREATE INDEX `idx_love_event_date` ON `love_event`(`event_date`);
CREATE INDEX `idx_message_couple` ON `message`(`couple_id`);
CREATE INDEX `idx_message_from_to` ON `message`(`from_id`, `to_id`);
CREATE INDEX `idx_photo_couple` ON `photo`(`couple_id`);
CREATE INDEX `idx_photo_uploader` ON `photo`(`uploader_id`);
