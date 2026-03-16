-- Drop existing PK on bigint id
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;

-- Make UUID column the new primary key
ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id_uuid);