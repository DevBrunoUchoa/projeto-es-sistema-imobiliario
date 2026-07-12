ALTER TABLE ad_images ADD COLUMN storage_path VARCHAR(500);
UPDATE ad_images SET storage_path = url WHERE storage_path IS NULL;
ALTER TABLE ad_images ALTER COLUMN storage_path SET NOT NULL;
CREATE UNIQUE INDEX uq_ad_images_storage_path ON ad_images (storage_path);
