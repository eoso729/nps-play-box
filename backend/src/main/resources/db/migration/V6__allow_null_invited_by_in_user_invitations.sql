-- Allow invited_by to be null for platform/system generated invitations
ALTER TABLE user_invitations ALTER COLUMN invited_by DROP NOT NULL;
