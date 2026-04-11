CREATE TABLE journalist (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    global_email VARCHAR(255),
    global_phone VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE media (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    url VARCHAR(255)
);

CREATE TABLE theme (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE activity (
    id UUID PRIMARY KEY,
    journalist_id UUID NOT NULL REFERENCES journalist(id),
    media_id UUID NOT NULL REFERENCES media(id),
    role VARCHAR(255),
    specific_email VARCHAR(255),
    specific_phone VARCHAR(50)
);

CREATE TABLE activity_themes (
    activity_id UUID NOT NULL REFERENCES activity(id),
    theme_id UUID NOT NULL REFERENCES theme(id),
    PRIMARY KEY (activity_id, theme_id)
);

CREATE TABLE interaction_log (
    id UUID PRIMARY KEY,
    journalist_id UUID NOT NULL REFERENCES journalist(id),
    activity_id UUID REFERENCES activity(id),
    date DATE NOT NULL,
    description TEXT NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
