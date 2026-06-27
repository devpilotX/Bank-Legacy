-- V5: the dependency map shows how the old system fits together: which program
-- calls which, which copybooks are pulled in, and which data files are touched.
-- Nodes are the parts. Edges are the links between them. Each edge records where it
-- came from: parsing the code (obvious calls and includes) or the AI (trickier
-- links). AI links start as suggestions, because a person must confirm them.

CREATE TABLE dependency_nodes (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id     BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    kind           VARCHAR(30)  NOT NULL DEFAULT 'unknown',
    source_file_id BIGINT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_dep_nodes_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_dep_nodes_source_file FOREIGN KEY (source_file_id) REFERENCES source_files (id) ON DELETE SET NULL,
    CONSTRAINT uq_dep_nodes UNIQUE (project_id, name, kind),
    CONSTRAINT chk_dep_nodes_kind CHECK (kind IN ('program', 'copybook', 'file', 'paragraph', 'unknown'))
);

CREATE TABLE dependency_edges (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id   BIGINT       NOT NULL,
    from_node_id BIGINT       NOT NULL,
    to_node_id   BIGINT       NOT NULL,
    kind         VARCHAR(30)  NOT NULL,
    origin       VARCHAR(10)  NOT NULL,
    status       VARCHAR(12)  NOT NULL DEFAULT 'confirmed',
    detail       VARCHAR(500),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_dep_edges_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_dep_edges_from FOREIGN KEY (from_node_id) REFERENCES dependency_nodes (id) ON DELETE CASCADE,
    CONSTRAINT fk_dep_edges_to FOREIGN KEY (to_node_id) REFERENCES dependency_nodes (id) ON DELETE CASCADE,
    CONSTRAINT chk_dep_edges_kind CHECK (kind IN ('calls', 'includes', 'reads', 'writes', 'relates')),
    CONSTRAINT chk_dep_edges_origin CHECK (origin IN ('parsed', 'ai')),
    CONSTRAINT chk_dep_edges_status CHECK (status IN ('confirmed', 'suggested', 'rejected'))
);

CREATE INDEX idx_dep_nodes_project ON dependency_nodes (project_id);
CREATE INDEX idx_dep_edges_project ON dependency_edges (project_id);
CREATE INDEX idx_dep_edges_from ON dependency_edges (from_node_id);
CREATE INDEX idx_dep_edges_to ON dependency_edges (to_node_id);

COMMENT ON TABLE  dependency_nodes        IS 'The parts of the old system: programs, copybooks, and data files.';
COMMENT ON TABLE  dependency_edges        IS 'Links between parts. origin says parsed or ai; ai links wait for a human to confirm.';
COMMENT ON COLUMN dependency_edges.origin IS 'parsed (from reading the code) or ai (suggested by the AI).';
COMMENT ON COLUMN dependency_edges.status IS 'confirmed, suggested (waiting on a human), or rejected.';

CREATE TRIGGER trg_dep_nodes_updated_at
    BEFORE UPDATE ON dependency_nodes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_dep_edges_updated_at
    BEFORE UPDATE ON dependency_edges
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
