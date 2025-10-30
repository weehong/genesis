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
VALUES ('Google Singapore', '200605367W', decode(
        'PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMjIuNTYgMTIuMjVjMC0uNzgtLjA3LTEuNTMtLjItMi4yNUgxMnY0LjI2aDUuOTJjLS4yNiAxLjM3LTEuMDQgMi41My0yLjIxIDMuMzF2Mi43N2gzLjU3YzIuMDgtMS45MiAzLjI4LTQuNzQgMy4yOC04LjA5eiIgZmlsbD0iIzQyODVGNCIvPjxwYXRoIGQ9Ik0xMiAyM2MyLjk3IDAgNS40Ni0uOTggNy4yOC0yLjY2bC0zLjU3LTIuNzdjLS45OC42Ni0yLjIzIDEuMDYtMy43MSAxLjA2LTIuODYgMC01LjI5LTEuOTMtNi4xNi00LjUzSDIuMTh2Mi44NEMzLjk5IDIwLjUzIDcuNyAyMyAxMiAyM3oiIGZpbGw9IiMzNEE4NTMiLz48cGF0aCBkPSJNNS44NCAxNC4wOWMtLjIyLS42Ni0uMzUtMS4zNi0uMzUtMi4wOXMuMTMtMS40My4zNS0yLjA5VjcuMDdIMi4xOEMxLjQzIDguNTUgMSAxMC4yMiAxIDEycy40MyAzLjQ1IDEuMTggNC45M2wyLjg1LTIuMjIuODEtLjYyeiIgZmlsbD0iI0ZCQkMwNSIvPjxwYXRoIGQ9Ik0xMiA1LjM4YzEuNjIgMCAzLjA2LjU2IDQuMjEgMS42NGwzLjE1LTMuMTVDMTcuNDUgMi4wOSAxNC45NyAxIDEyIDEgNy43IDEgMy45OSAzLjQ3IDIuMTggNy4wN2wzLjY2IDIuODRjLjg3LTIuNiAzLjMtNC41MyA2LjE2LTQuNTN6IiBmaWxsPSIjRUE0MzM1Ii8+PHBhdGggZD0iTTEgMWgyMnYyMkgxeiIgZmlsbD0ibm9uZSIvPjwvc3ZnPg==',
        'base64'), FALSE);

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
VALUES (1, 'Singapore HQ', 'SG-HQ', '70 Pasir Panjang Rd', 'Singapore', 'Singapore', 'Singapore', '117371',
        '+65-6531-5000', 'singapore@google.com');

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
VALUES (1, 'Engineering', 'ENG', 'Software Engineering Department'),
       (1, 'Sales', 'SALES', 'Sales and Business Development'),
       (1, 'HR', 'HR', 'Human Resources Department');

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
