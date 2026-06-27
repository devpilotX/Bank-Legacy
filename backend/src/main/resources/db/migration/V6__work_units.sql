-- V6: the modernization workspace. A work unit is one small piece of old code an
-- engineer rewrites into Java while the old system keeps running. We keep three
-- versions side by side: the original old code, the AI's first-draft Java, and the
-- human's final Java. We never overwrite the AI draft with the human version, so the
-- history of what the AI suggested and what the engineer actually shipped is kept.

CREATE TABLE work_units (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id     BIGINT       NOT NULL,
    source_file_id BIGINT,
    title          VARCHAR(255) NOT NULL,
    original_code  TEXT         NOT NULL,
    ai_draft_java  TEXT,
    human_java     TEXT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'todo',
    owner_id       BIGINT,
    notes          TEXT,
    created_by     BIGINT,
    approved_by    BIGINT,
    approved_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_work_units_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_work_units_source_file FOREIGN KEY (source_file_id) REFERENCES source_files (id) ON DELETE SET NULL,
    CONSTRAINT fk_work_units_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_work_units_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_work_units_approved_by FOREIGN KEY (approved_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_work_units_status CHECK (status IN ('todo', 'in_progress', 'in_review', 'done'))
);

CREATE INDEX idx_work_units_project ON work_units (project_id);
CREATE INDEX idx_work_units_owner ON work_units (owner_id);

COMMENT ON TABLE  work_units               IS 'One piece of old code being rewritten into Java, with the AI draft and human final kept apart.';
COMMENT ON COLUMN work_units.original_code IS 'The old code section we are rewriting, copied in so it stays stable.';
COMMENT ON COLUMN work_units.ai_draft_java IS 'The AI first-draft Java. We keep it as a record, even after a human rewrites it.';
COMMENT ON COLUMN work_units.human_java    IS 'The engineer''s Java. This is what gets approved.';
COMMENT ON COLUMN work_units.status        IS 'todo, in_progress, in_review, or done.';

CREATE TRIGGER trg_work_units_updated_at
    BEFORE UPDATE ON work_units
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
