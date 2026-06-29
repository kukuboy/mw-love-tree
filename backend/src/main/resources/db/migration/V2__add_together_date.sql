-- V2: Add together_date column to couple table
-- Separate from anniversary (binding date) to represent the actual "together" date

SET @dbname = DATABASE();
SET @tablename = 'couple';
SET @columnname = 'together_date';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE `couple` ADD COLUMN `together_date` DATE NULL AFTER `anniversary`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
