-- Ensure any existing rows have a valid inviter before enforcing NOT NULL
UPDATE user_invitations 
SET invited_by = (SELECT id FROM users WHERE role = 'PLATFORM_ADMIN' ORDER BY id ASC LIMIT 1)
WHERE invited_by IS NULL;

-- Enforce NOT NULL on invited_by
ALTER TABLE user_invitations ALTER COLUMN invited_by SET NOT NULL;
