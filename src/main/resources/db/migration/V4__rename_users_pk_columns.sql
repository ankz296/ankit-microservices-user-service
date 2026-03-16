-- Keep old bigint id for reference (optional)
ALTER TABLE users RENAME COLUMN id TO id_long;

-- Rename UUID pk column to id
ALTER TABLE users RENAME COLUMN id_uuid TO id;