  -- Add active status tracking to app_user
   ALTER TABLE app_user ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
   ALTER TABLE app_user ADD COLUMN deactivated_at TIMESTAMP;

   -- Add active status tracking to movie
   ALTER TABLE movie ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
   ALTER TABLE movie ADD COLUMN deactivated_at TIMESTAMP;

   -- Add active status tracking to show
   ALTER TABLE show ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
   ALTER TABLE show ADD COLUMN deactivated_at TIMESTAMP;