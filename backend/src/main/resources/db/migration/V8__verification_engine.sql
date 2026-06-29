-- V8: the real verification engine. We now run the original COBOL to get the true
-- expected output, run the new Java the same way, and compare. So two things change.
--
-- First, a test case no longer needs a hand-typed expected output. A case is now just
-- the inputs, and the golden answer comes from running the old COBOL.
--
-- Second, a run now records the whole picture so an engineer can see exactly what
-- happened: both outputs, a readable diff, the comparison settings that were used,
-- whether the only difference was formatting, who ran it, and when.

-- A case is now just inputs, so the expected output is optional.
ALTER TABLE verification_cases ALTER COLUMN expected_output DROP NOT NULL;

-- Some programs read from files instead of standard input. This holds those input
-- files as a small JSON object of file name to file content. The engine writes them
-- into the sandbox before the program runs. Null means the case only uses stdin.
ALTER TABLE verification_cases ADD COLUMN input_files TEXT;

COMMENT ON COLUMN verification_cases.expected_output IS
    'Optional. The engine now gets the expected output by running the original COBOL, so a person no longer types it.';
COMMENT ON COLUMN verification_cases.input_files IS
    'Optional JSON of file name to content, for programs that read input files instead of standard input.';

-- A run now stores the full picture.
ALTER TABLE verification_runs ADD COLUMN cobol_output              TEXT;
ALTER TABLE verification_runs ADD COLUMN java_output               TEXT;
ALTER TABLE verification_runs ADD COLUMN diff                      TEXT;
ALTER TABLE verification_runs ADD COLUMN outcome                   VARCHAR(24);
ALTER TABLE verification_runs ADD COLUMN difference_kind           VARCHAR(12);
ALTER TABLE verification_runs ADD COLUMN normalized_trailing_space BOOLEAN;
ALTER TABLE verification_runs ADD COLUMN numeric_tolerance         DOUBLE PRECISION;
ALTER TABLE verification_runs ADD COLUMN run_by                    BIGINT;

ALTER TABLE verification_runs
    ADD CONSTRAINT fk_vruns_run_by FOREIGN KEY (run_by) REFERENCES users (id) ON DELETE SET NULL;

-- outcome is the plain result of one run. The older "passed" boolean stays as the
-- simple yes or no headline; outcome adds why, including the error paths.
ALTER TABLE verification_runs ADD CONSTRAINT chk_vruns_outcome CHECK (
    outcome IS NULL OR outcome IN (
        'passed', 'failed',
        'cobol_compile_error', 'cobol_run_error',
        'java_compile_error', 'java_run_error',
        'timeout', 'engine_error'));

-- difference_kind tells us which kind of difference we saw, so a formatting-only gap is
-- never confused with a real behavior gap.
ALTER TABLE verification_runs ADD CONSTRAINT chk_vruns_diff_kind CHECK (
    difference_kind IS NULL OR difference_kind IN ('none', 'formatting', 'behavior'));

COMMENT ON COLUMN verification_runs.cobol_output IS
    'The golden output captured by running the original COBOL on this case input.';
COMMENT ON COLUMN verification_runs.java_output IS
    'What the new Java produced on the same input. This replaces the older actual_output column.';
COMMENT ON COLUMN verification_runs.diff IS
    'A readable, line by line diff shown when the outputs do not match.';
COMMENT ON COLUMN verification_runs.outcome IS
    'Plain result: passed, failed, a compile or run error on either side, a timeout, or an engine error.';
COMMENT ON COLUMN verification_runs.difference_kind IS
    'none when they match as is, formatting when they only match after trimming spaces or allowing a numeric tolerance, behavior for a real difference.';
COMMENT ON COLUMN verification_runs.normalized_trailing_space IS
    'Whether trailing spaces were trimmed before comparing. Always recorded so the result is never a silent guess.';
COMMENT ON COLUMN verification_runs.numeric_tolerance IS
    'The allowed numeric difference when comparing numbers. Null or zero means an exact match was required.';
COMMENT ON COLUMN verification_runs.run_by IS
    'The user who pressed run.';
