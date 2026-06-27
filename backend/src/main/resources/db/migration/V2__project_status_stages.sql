-- V2: projects now move through real modernization stages instead of the generic
-- statuses we started with. A project begins at intake and ends at done.

ALTER TABLE projects DROP CONSTRAINT chk_projects_status;

-- Map any old values into the new set. There is no real data yet, but this keeps
-- the migration safe if there ever is.
UPDATE projects SET status = 'intake'
 WHERE status NOT IN ('intake', 'mapping', 'modernizing', 'verifying', 'done');

ALTER TABLE projects ALTER COLUMN status SET DEFAULT 'intake';

ALTER TABLE projects
    ADD CONSTRAINT chk_projects_status
    CHECK (status IN ('intake', 'mapping', 'modernizing', 'verifying', 'done'));

COMMENT ON COLUMN projects.status IS 'Stage: intake, mapping, modernizing, verifying, or done.';
