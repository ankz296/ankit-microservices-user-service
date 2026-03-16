-- Enable UUID generation helper
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Add active column (default true)
ALTER TABLE users
ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- Add new UUID column that will become the PK
ALTER TABLE users
ADD COLUMN IF NOT EXISTS id_uuid UUID;

-- Backfill UUID values for existing rows
UPDATE users
SET id_uuid = gen_random_uuid()
WHERE id_uuid IS NULL;

-- Make it not null
ALTER TABLE users
ALTER COLUMN id_uuid SET NOT NULL;

-- Ensure uniqueness (helps before making it PK)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_id_uuid'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT uk_users_id_uuid UNIQUE (id_uuid);
    END IF;
END $$;