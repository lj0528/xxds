CREATE TABLE `user_behavior` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `user_id` BIGINT NOT NULL,
                                 `question_id` VARCHAR(36) NOT NULL,
                                 `chapter_id` INT,
                                 `is_correct` TINYINT(1) DEFAULT 0,
                                 `score` DECIMAL(5,2) DEFAULT 0,
                                 `time_spent` INT DEFAULT 0,
                                 `behavior_type` VARCHAR(20) DEFAULT 'TEST',
                                 `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 INDEX `idx_user_chapter` (`user_id`, `chapter_id`)
);