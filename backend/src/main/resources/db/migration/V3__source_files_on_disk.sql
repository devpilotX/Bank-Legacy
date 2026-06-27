-- V3: source files now live on disk, with their bytes stored under a storage key.
-- The database keeps the facts about each file: who uploaded it, how big it is,
-- how many lines it has, and where to find it. We drop the old inline content
-- column, because large COBOL files and zips are streamed to disk instead of being
-- held inside a database row.

ALTER TABLE source_files DROP COLUMN content;

ALTER TABLE source_files ADD COLUMN storage_key  VARCHAR(500);
ALTER TABLE source_files ADD COLUMN line_count   INTEGER;
ALTER TABLE source_files ADD COLUMN content_type VARCHAR(150);
ALTER TABLE source_files ADD COLUMN uploaded_by  BIGINT;

-- Tie a file to the user who uploaded it, but keep the file if that user is ever
-- removed, so we never lose a bank's code just because someone left the team.
ALTER TABLE source_files
    ADD CONSTRAINT fk_source_files_uploaded_by
    FOREIGN KEY (uploaded_by) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_source_files_uploaded_by ON source_files (uploaded_by);

COMMENT ON COLUMN source_files.storage_key  IS 'Where the bytes live on disk, relative to the storage root.';
COMMENT ON COLUMN source_files.line_count   IS 'Number of lines for text files; null for binary files.';
COMMENT ON COLUMN source_files.content_type IS 'The type the browser reported at upload, if any.';
COMMENT ON COLUMN source_files.uploaded_by  IS 'The team member who uploaded the file.';
