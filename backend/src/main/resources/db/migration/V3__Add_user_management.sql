CREATE TABLE role (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL
);

CREATE TABLE application_user (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES application_user(id),
    role_id UUID NOT NULL REFERENCES role(id),
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO role (id, code, label) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN', 'Administrateur'),
    ('00000000-0000-0000-0000-000000000002', 'USER', 'Utilisateur');

INSERT INTO application_user (id, username, password_hash, first_name, last_name, enabled) VALUES
    (
        '00000000-0000-0000-0000-000000000010',
        'admin',
        '$2a$10$l42RCMDwOC1QrHyPwrrBP.OXPiAOF/K3sKxDSlXofue8xl3LlFS8K',
        'Local',
        'Admin',
        TRUE
    );

INSERT INTO user_role (user_id, role_id) VALUES
    ('00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000002');
