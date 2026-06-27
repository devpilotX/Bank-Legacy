-- V4: explanations are the AI's plain-English account of what a piece of old code
-- does. Every explanation starts as a draft. A human reads it, edits it if needed,
-- and approves it. We record who approved it and when, so we always know a person
-- checked the AI's work before anyone relied on it.

CREATE TABLE explanations (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_file_id BIGINT       NOT NULL,
    -- The line range this explains, or null for the whole file.
    start_line     INTEGER,
    end_line       INTEGER,
    content        TEXT         NOT NULL,
    -- Which AI model wrote the first draft, for our records.
    model          VARCHAR(100),
    status         VARCHAR(20)  NOT NULL DEFAULT 'draft',
    created_by     BIGINT,
    approved_by    BIGINT,
    approved_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_explanations_source_file
        FOREIGN KEY (source_file_id) REFERENCES source_files (id) ON DELETE CASCADE,
    CONSTRAINT fk_explanations_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_explanations_approved_by
        FOREIGN KEY (approved_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_explanations_status CHECK (status IN ('draft', 'approved'))
);

CREATE INDEX idx_explanations_source_file ON explanations (source_file_id);

COMMENT ON TABLE  explanations            IS 'AI explanations of old code, each a draft until a person approves it.';
COMMENT ON COLUMN explanations.status     IS 'draft until a human approves it, then approved.';
COMMENT ON COLUMN explanations.approved_by IS 'The person who approved the explanation.';

CREATE TRIGGER trg_explanations_updated_at
    BEFORE UPDATE ON explanations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
