-- Companies table
CREATE TABLE companies
(
    id                  bigserial PRIMARY KEY,
    uuid                uuid         NOT NULL DEFAULT gen_random_uuid(),
    name                varchar(255) NOT NULL,
    registration_number varchar(20)  NOT NULL UNIQUE,
    logo                bytea,
    soft_delete         boolean      NOT NULL DEFAULT FALSE,
    created_at          timestamp             DEFAULT CURRENT_TIMESTAMP,
    updated_at          timestamp             DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_companies_registration ON companies (registration_number);
CREATE INDEX idx_companies_uuid ON companies (uuid);
CREATE INDEX idx_companies_soft_delete ON companies (soft_delete);

INSERT INTO companies (name, registration_number, logo, soft_delete)
SELECT 'Google Singapore', '200605367W', NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE registration_number = '200605367W');

-- Branches table
CREATE TABLE branches
(
    id            bigserial PRIMARY KEY,
    uuid          uuid         NOT NULL DEFAULT gen_random_uuid(),
    company_id    bigint       NOT NULL,
    branch_name   varchar(255) NOT NULL,
    branch_code   varchar(20)  NOT NULL,
    address       varchar(1000),
    city          varchar(100),
    state         varchar(100),
    country       varchar(100),
    postal_code   varchar(20),
    phone_number  varchar(20),
    email_address varchar(255),
    soft_delete   boolean      NOT NULL DEFAULT FALSE,
    created_at    timestamp             DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_branch_code_company UNIQUE (company_id, branch_code)
);

CREATE INDEX idx_branches_company_id ON branches (company_id);
CREATE INDEX idx_branches_uuid ON branches (uuid);
CREATE INDEX idx_branches_soft_delete ON branches (soft_delete);

INSERT INTO branches (company_id, branch_name, branch_code, address, city, state, country, postal_code, phone_number,
                      email_address)
SELECT c.id, 'Singapore HQ', 'SG-HQ', '70 Pasir Panjang Rd', 'Singapore', 'Singapore', 'Singapore', '117371',
       '+65-6531-5000', 'singapore@google.com'
FROM companies c
WHERE c.registration_number = '200605367W'
  AND NOT EXISTS (SELECT 1 FROM branches b WHERE b.company_id = c.id AND b.branch_code = 'SG-HQ');

-- Departments table
CREATE TABLE departments
(
    id              bigserial PRIMARY KEY,
    uuid            uuid         NOT NULL DEFAULT gen_random_uuid(),
    branch_id       bigint       NOT NULL,
    department_name varchar(255) NOT NULL,
    department_code varchar(20)  NOT NULL,
    description     text,
    soft_delete     boolean      NOT NULL DEFAULT FALSE,
    created_at      timestamp             DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_department_branch FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_department_code_branch UNIQUE (branch_id, department_code)
);

CREATE INDEX idx_departments_branch_id ON departments (branch_id);
CREATE INDEX idx_departments_uuid ON departments (uuid);
CREATE INDEX idx_departments_soft_delete ON departments (soft_delete);

INSERT INTO departments (branch_id, department_name, department_code, description)
SELECT b.id, 'Engineering', 'ENG', 'Software Engineering Department'
FROM branches b
JOIN companies c ON b.company_id = c.id
WHERE c.registration_number = '200605367W' AND b.branch_code = 'SG-HQ'
  AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.branch_id = b.id AND d.department_code = 'ENG')
UNION ALL
SELECT b.id, 'Sales', 'SALES', 'Sales and Business Development'
FROM branches b
JOIN companies c ON b.company_id = c.id
WHERE c.registration_number = '200605367W' AND b.branch_code = 'SG-HQ'
  AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.branch_id = b.id AND d.department_code = 'SALES')
UNION ALL
SELECT b.id, 'HR', 'HR', 'Human Resources Department'
FROM branches b
JOIN companies c ON b.company_id = c.id
WHERE c.registration_number = '200605367W' AND b.branch_code = 'SG-HQ'
  AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.branch_id = b.id AND d.department_code = 'HR');

-- Constraints table
CREATE TABLE constraints
(
    id           bigserial PRIMARY KEY,
    uuid         uuid          NOT NULL DEFAULT gen_random_uuid(),
    name         varchar(255)  NOT NULL,
    description  text,
    sentence     varchar(1000) NOT NULL,
    schemas      jsonb,
    schemas_hash varchar(64),
    soft_delete  boolean       NOT NULL DEFAULT FALSE,
    created_at   timestamp              DEFAULT CURRENT_TIMESTAMP,
    updated_at   timestamp              DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_constraints_uuid ON constraints (uuid);
CREATE INDEX idx_constraints_soft_delete ON constraints (soft_delete);
CREATE INDEX idx_constraints_schemas_hash ON constraints (schemas_hash);

-- Add column comments to clarify purpose
COMMENT ON COLUMN constraints.description IS 'Long-form human-readable explanation of the constraint rule and its purpose';
COMMENT ON COLUMN constraints.sentence IS 'Short canonical example sentence demonstrating how the constraint is used or applied';
