-- V1: the core tables our platform is built on.
--
-- This is the first migration, so it sets up the four things we always deal with:
-- the people on our team, the banks we work for, the projects we run for them, and
-- the old source files we take in. Flyway runs this once and writes down that it
-- did, so the schema comes out the same on every machine.
--
-- A few conventions we follow here and in later migrations:
--   * ids are bigint identity columns, so the database hands out the numbers.
--   * every table keeps created_at and updated_at as timestamptz, in UTC.
--   * updated_at is kept current by a trigger, so no one has to remember to set it.
--   * status columns are plain text with a check constraint listing the values we
--     allow today. Adding a new value later is a deliberate migration, which is
--     what we want for a system that touches real banking work.


-- Keeps updated_at honest. Runs before every UPDATE and stamps the current time.
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ---------------------------------------------------------------------------
-- users: our own team members who sign in to the tool.
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    -- How we check a sign-in. Left null for now because the login flow is not built
    -- yet. It will hold a hashed password, or stay null if we move to single sign-on.
    password_hash VARCHAR(255),
    role          VARCHAR(20)  NOT NULL DEFAULT 'engineer',
    status        VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email   UNIQUE (email),
    CONSTRAINT chk_users_role   CHECK (role   IN ('engineer', 'admin')),
    CONSTRAINT chk_users_status CHECK (status IN ('active', 'disabled'))
);

COMMENT ON TABLE  users        IS 'Our own team members who sign in to the internal tool.';
COMMENT ON COLUMN users.email  IS 'Used to sign in. Must be unique.';
COMMENT ON COLUMN users.role   IS 'What the person can do: engineer or admin.';
COMMENT ON COLUMN users.status IS 'active means they can sign in; disabled locks them out.';

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- clients: the banks and credit unions we do the work for.
-- ---------------------------------------------------------------------------
CREATE TABLE clients (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'active',
    notes      TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_clients_status CHECK (status IN ('prospect', 'active', 'archived'))
);

COMMENT ON TABLE  clients        IS 'The banks and credit unions we do modernization work for.';
COMMENT ON COLUMN clients.name   IS 'The bank''s name, the way we refer to them.';
COMMENT ON COLUMN clients.status IS 'prospect, active, or archived.';

CREATE TRIGGER trg_clients_updated_at
    BEFORE UPDATE ON clients
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- projects: one body of work for a client, like mapping a single core system.
-- ---------------------------------------------------------------------------
CREATE TABLE projects (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- A project always belongs to a client. We block deleting a client that still
    -- has projects, so work never gets orphaned or quietly wiped.
    CONSTRAINT fk_projects_client
        FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE RESTRICT,
    CONSTRAINT chk_projects_status CHECK (status IN ('active', 'on_hold', 'done', 'archived'))
);

COMMENT ON TABLE  projects           IS 'One body of work for a client, for example mapping one core system.';
COMMENT ON COLUMN projects.client_id IS 'The bank this project is for.';
COMMENT ON COLUMN projects.status    IS 'active, on_hold, done, or archived.';

CREATE INDEX idx_projects_client_id ON projects (client_id);

CREATE TRIGGER trg_projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- source_files: the old code files we take in for a project, one row per file.
-- ---------------------------------------------------------------------------
CREATE TABLE source_files (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id BIGINT       NOT NULL,
    filename   VARCHAR(500) NOT NULL,
    -- What kind of file it is: cobol, copybook, jcl, and so on.
    language   VARCHAR(30)  NOT NULL DEFAULT 'cobol',
    byte_size  BIGINT,
    -- A sha-256 hex digest so we can spot duplicates and notice if a file changed.
    checksum   VARCHAR(64),
    -- The actual source text. It can be large, so later code should read it only
    -- when it is really needed, not on every list query.
    content    TEXT,
    status     VARCHAR(20)  NOT NULL DEFAULT 'received',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- A file belongs to one project. If a project is deleted, its files go with it.
    CONSTRAINT fk_source_files_project
        FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT chk_source_files_status
        CHECK (status IN ('received', 'analyzing', 'analyzed', 'rewritten', 'verified'))
);

COMMENT ON TABLE  source_files            IS 'The old source files we take in for a project, one row per file.';
COMMENT ON COLUMN source_files.project_id IS 'The project this file belongs to.';
COMMENT ON COLUMN source_files.language   IS 'cobol, copybook, jcl, and so on.';
COMMENT ON COLUMN source_files.status     IS 'Where the file is in our process: received through verified.';

CREATE INDEX idx_source_files_project_id ON source_files (project_id);

CREATE TRIGGER trg_source_files_updated_at
    BEFORE UPDATE ON source_files
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
