-- V7: verification checks that the new Java behaves like the old code. A test case
-- is an input and the output the old system produced for it. A run records what the
-- new Java produced and whether it matched. The AI can draft cases from the old code,
-- but those start as suggestions a human confirms before we trust them.
--
-- Note: actually compiling and running the rewritten Java in a sandbox is future work.
-- For now a run stores the observed output and the pass or fail, so the data and the
-- API are ready and the deeper engine can slot in later.

CREATE TABLE verification_cases (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    work_unit_id    BIGINT       NOT NULL,
    name            VARCHAR(255) NOT NULL,
    input           TEXT,
    expected_output TEXT         NOT NULL,
    origin          VARCHAR(10)  NOT NULL DEFAULT 'human',
    status          VARCHAR(12)  NOT NULL DEFAULT 'confirmed',
    created_by      BIGINT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_vcases_work_unit FOREIGN KEY (work_unit_id) REFERENCES work_units (id) ON DELETE CASCADE,
    CONSTRAINT fk_vcases_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_vcases_origin CHECK (origin IN ('human', 'ai')),
    CONSTRAINT chk_vcases_status CHECK (status IN ('confirmed', 'suggested'))
);

CREATE TABLE verification_runs (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    work_unit_id  BIGINT       NOT NULL,
    case_id       BIGINT       NOT NULL,
    actual_output TEXT,
    passed        BOOLEAN      NOT NULL,
    detail        TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_vruns_work_unit FOREIGN KEY (work_unit_id) REFERENCES work_units (id) ON DELETE CASCADE,
    CONSTRAINT fk_vruns_case FOREIGN KEY (case_id) REFERENCES verification_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_vcases_work_unit ON verification_cases (work_unit_id);
CREATE INDEX idx_vruns_work_unit ON verification_runs (work_unit_id);
CREATE INDEX idx_vruns_case ON verification_runs (case_id);

COMMENT ON TABLE  verification_cases        IS 'Input and expected output pairs that define how a piece of code should behave.';
COMMENT ON COLUMN verification_cases.origin IS 'human or ai. AI-drafted cases wait for a person to confirm them.';
COMMENT ON TABLE  verification_runs         IS 'One run of the new Java against one case, with the result. Runs are kept, never changed.';

CREATE TRIGGER trg_vcases_updated_at
    BEFORE UPDATE ON verification_cases
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
