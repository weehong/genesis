CREATE TABLE companies
(
    id                  BIGSERIAL PRIMARY KEY,
    uuid                UUID         NOT NULL DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    registration_number VARCHAR(20)  NOT NULL UNIQUE,
    logo                BYTEA,
    soft_delete         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_companies_registration ON companies (registration_number);
CREATE INDEX idx_companies_uuid ON companies (uuid);
CREATE INDEX idx_companies_soft_delete ON companies (soft_delete);

CREATE TABLE branches
(
    id            BIGSERIAL PRIMARY KEY,
    uuid          UUID         NOT NULL DEFAULT gen_random_uuid(),
    company_id    BIGINT       NOT NULL,
    branch_name   VARCHAR(255) NOT NULL,
    branch_code   VARCHAR(20)  NOT NULL,
    address       VARCHAR(1000),
    city          VARCHAR(100),
    state         VARCHAR(100),
    country       VARCHAR(100),
    postal_code   VARCHAR(20),
    phone_number  VARCHAR(20),
    email_address VARCHAR(255),
    soft_delete   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_company FOREIGN KEY (company_id)
        REFERENCES companies (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_branch_code_company UNIQUE (company_id, branch_code)
);

CREATE INDEX idx_branches_company_id ON branches (company_id);
CREATE INDEX idx_branches_uuid ON branches (uuid);
CREATE INDEX idx_branches_soft_delete ON branches (soft_delete);

CREATE TABLE departments
(
    id              BIGSERIAL PRIMARY KEY,
    uuid            UUID         NOT NULL DEFAULT gen_random_uuid(),
    branch_id       BIGINT       NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    department_code VARCHAR(20)  NOT NULL,
    description     TEXT,
    soft_delete     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_department_branch FOREIGN KEY (branch_id)
        REFERENCES branches (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_department_code_branch UNIQUE (branch_id, department_code)
);

CREATE INDEX idx_departments_branch_id ON departments (branch_id);
CREATE INDEX idx_departments_uuid ON departments (uuid);
CREATE INDEX idx_departments_soft_delete ON departments (soft_delete);

CREATE TABLE constraints
(
    id           BIGSERIAL PRIMARY KEY,
    uuid         UUID          NOT NULL DEFAULT gen_random_uuid(),
    name         VARCHAR(255)  NOT NULL,
    description  TEXT,
    sentence     VARCHAR(1000) NOT NULL,
    schemas      JSONB,
    schemas_hash VARCHAR(64),
    soft_delete  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP              DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_constraints_uuid ON constraints (uuid);
CREATE INDEX idx_constraints_soft_delete ON constraints (soft_delete);
CREATE INDEX idx_constraints_schemas_hash ON constraints (schemas_hash);