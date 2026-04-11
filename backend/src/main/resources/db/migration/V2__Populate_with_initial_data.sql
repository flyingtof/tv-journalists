-- V2__Populate_with_initial_data.sql

-- Insert Themes related to environment
INSERT INTO theme (id, name) VALUES
(gen_random_uuid(), 'Changement climatique'),
(gen_random_uuid(), 'Biodiversité'),
(gen_random_uuid(), 'Énergies renouvelables'),
(gen_random_uuid(), 'Pollution de l''air'),
(gen_random_uuid(), 'Pollution de l''eau'),
(gen_random_uuid(), 'Déforestation'),
(gen_random_uuid(), 'Agriculture durable'),
(gen_random_uuid(), 'Transition énergétique'),
(gen_random_uuid(), 'Économie circulaire'),
(gen_random_uuid(), 'Protection des océans'),
(gen_random_uuid(), 'Plastiques'),
(gen_random_uuid(), 'Ressources en eau'),
(gen_random_uuid(), 'Mobilité durable'),
(gen_random_uuid(), 'Conservation des espèces'),
(gen_random_uuid(), 'Politiques environnementales'),
(gen_random_uuid(), 'Finance verte'),
(gen_random_uuid(), 'Justice climatique'),
(gen_random_uuid(), 'Adaptation au changement climatique'),
(gen_random_uuid(), 'Technologie et environnement'),
(gen_random_uuid(), 'Santé et environnement');

-- Insert Media (50 total: 70% French, 15% Belgian, 15% Swiss)
-- French Media (35)
INSERT INTO media (id, name, type, url) VALUES
-- Télévision (10)
(gen_random_uuid(), 'TF1', 'TV', 'https://www.tf1.fr'),
(gen_random_uuid(), 'France 2', 'TV', 'https://www.france.tv/france-2/'),
(gen_random_uuid(), 'France 3', 'TV', 'https://www.france.tv/france-3/'),
(gen_random_uuid(), 'Canal+', 'TV', 'https://www.canalplus.com'),
(gen_random_uuid(), 'France 5', 'TV', 'https://www.france.tv/france-5/'),
(gen_random_uuid(), 'M6', 'TV', 'https://www.6play.fr'),
(gen_random_uuid(), 'Arte France', 'TV', 'https://www.arte.tv/fr/'),
(gen_random_uuid(), 'BFM TV', 'TV', 'https://www.bfmtv.com'),
(gen_random_uuid(), 'CNews', 'TV', 'https://www.cnews.fr'),
(gen_random_uuid(), 'LCI', 'TV', 'https://www.lci.fr'),
-- Radio (10)
(gen_random_uuid(), 'France Inter', 'RADIO', 'https://www.franceinter.fr'),
(gen_random_uuid(), 'France Info', 'RADIO', 'https://www.francetvinfo.fr'),
(gen_random_uuid(), 'France Culture', 'RADIO', 'https://www.franceculture.fr'),
(gen_random_uuid(), 'RTL', 'RADIO', 'https://www.rtl.fr'),
(gen_random_uuid(), 'Europe 1', 'RADIO', 'https://www.europe1.fr'),
(gen_random_uuid(), 'RMC', 'RADIO', 'https://rmc.bfmtv.com'),
(gen_random_uuid(), 'NRJ', 'RADIO', 'https://www.nrj.fr'),
(gen_random_uuid(), 'Fun Radio', 'RADIO', 'https://www.funradio.fr'),
(gen_random_uuid(), 'Radio Classique', 'RADIO', 'https://www.radioclassique.fr'),
(gen_random_uuid(), 'FIP', 'RADIO', 'https://www.fip.fr'),
-- Journaux (15)
(gen_random_uuid(), 'Le Monde', 'PRESS', 'https://www.lemonde.fr'),
(gen_random_uuid(), 'Le Figaro', 'PRESS', 'https://www.lefigaro.fr'),
(gen_random_uuid(), 'Libération', 'PRESS', 'https://www.liberation.fr'),
(gen_random_uuid(), 'Les Échos', 'PRESS', 'https://www.lesechos.fr'),
(gen_random_uuid(), 'La Croix', 'PRESS', 'https://www.la-croix.com'),
(gen_random_uuid(), 'L''Humanité', 'PRESS', 'https://www.humanite.fr'),
(gen_random_uuid(), 'Mediapart', 'PRESS', 'https://www.mediapart.fr'),
(gen_random_uuid(), 'Le Canard Enchaîné', 'PRESS', 'https://www.lecanardenchaine.fr'),
(gen_random_uuid(), 'L''Express', 'PRESS', 'https://www.lexpress.fr'),
(gen_random_uuid(), 'L''Obs', 'PRESS', 'https://www.nouvelobs.com'),
(gen_random_uuid(), 'Le Point', 'PRESS', 'https://www.lepoint.fr'),
(gen_random_uuid(), 'Marianne', 'PRESS', 'https://www.marianne.net'),
(gen_random_uuid(), 'Courrier International', 'PRESS', 'https://www.courrierinternational.com'),
(gen_random_uuid(), 'Le Parisien', 'PRESS', 'https://www.leparisien.fr'),
(gen_random_uuid(), '20 Minutes', 'PRESS', 'https://www.20minutes.fr');

-- Belgian Media (8)
INSERT INTO media (id, name, type, url) VALUES
-- Télévision (3)
(gen_random_uuid(), 'RTBF La Une', 'TV', 'https://www.rtbf.be/laune'),
(gen_random_uuid(), 'RTL-TVI', 'TV', 'https://www.rtl.be/tv/rtltvi'),
(gen_random_uuid(), 'Club RTL', 'TV', 'https://www.rtl.be/tv/clubrtl'),
-- Radio (2)
(gen_random_uuid(), 'La Première', 'RADIO', 'https://www.rtbf.be/lapremiere'),
(gen_random_uuid(), 'Bel RTL', 'RADIO', 'https://www.belrtl.be'),
-- Journaux (3)
(gen_random_uuid(), 'Le Soir', 'PRESS', 'https://www.lesoir.be'),
(gen_random_uuid(), 'La Libre Belgique', 'PRESS', 'https://www.lalibre.be'),
(gen_random_uuid(), 'L''Echo', 'PRESS', 'https://www.lecho.be');

-- Swiss Media (7)
INSERT INTO media (id, name, type, url) VALUES
-- Télévision (3)
(gen_random_uuid(), 'RTS 1', 'TV', 'https://www.rts.ch/play/tv'),
(gen_random_uuid(), 'RTS 2', 'TV', 'https://www.rts.ch/play/tv'),
(gen_random_uuid(), 'TV5MONDE Suisse', 'TV', 'https://europe.tv5monde.com/suisse'),
-- Radio (2)
(gen_random_uuid(), 'RTS La Première', 'RADIO', 'https://www.rts.ch/play/radio'),
(gen_random_uuid(), 'RTS Espace 2', 'RADIO', 'https://www.rts.ch/play/radio'),
-- Journaux (2)
(gen_random_uuid(), 'Le Temps', 'PRESS', 'https://www.letemps.ch'),
(gen_random_uuid(), 'La Tribune de Genève', 'PRESS', 'https://www.tdg.ch');

DO $$
DECLARE
    -- Arrays for names
    first_names TEXT[] := ARRAY['Jean', 'Pierre', 'Michel', 'André', 'Philippe', 'Louis', 'Alain', 'Jacques', 'Bernard', 'Nicolas', 'Julien', 'Martin', 'Thomas', 'Alexandre', 'Laurent', 'Sébastien', 'Frédéric', 'Guillaume', 'Stéphane', 'Vincent', 'Marie', 'Nathalie', 'Isabelle', 'Sylvie', 'Catherine', 'Françoise', 'Martine', 'Christine', 'Valérie', 'Sophie', 'Anne', 'Chantal', 'Sandrine', 'Véronique', 'Céline', 'Julie', 'Laurence', 'Patricia', 'Monique', 'Élodie'];
    last_names TEXT[] := ARRAY['Martin', 'Bernard', 'Dubois', 'Thomas', 'Robert', 'Richard', 'Petit', 'Durand', 'Leroy', 'Moreau', 'Simon', 'Laurent', 'Lefebvre', 'Michel', 'Garcia', 'David', 'Bertrand', 'Roux', 'Vincent', 'Fournier', 'Morel', 'Girard', 'Andre', 'Lefevre', 'Mercier', 'Dupont', 'Lambert', 'Bonnet', 'Francois', 'Martinez', 'Legrand', 'Garnier', 'Faure', 'Rousseau', 'Blanc', 'Guerin', 'Muller', 'Henry', 'Roussel', 'Nicolas'];

    -- Variables for loops
    i INT;
    j INT;
    k INT;
    l INT;
    journalist_id_var UUID;
    activity_id_var UUID;
    log_activity_id UUID;
    media_id_var UUID;
    theme_id_var UUID;
    num_activities INT;
    num_themes_per_activity INT;
    num_interactions INT;
    journalist_activities UUID[];

    -- Variables for journalist data
    first_name_var TEXT;
    last_name_var TEXT;

BEGIN
    -- Get all media and theme IDs into arrays for random selection
    CREATE TEMP TABLE media_ids AS SELECT id FROM media;
    CREATE TEMP TABLE theme_ids AS SELECT id FROM theme;

    FOR i IN 1..1000 LOOP
        -- Generate a random journalist
        first_name_var := first_names[1 + floor(random() * array_length(first_names, 1))];
        last_name_var := last_names[1 + floor(random() * array_length(last_names, 1))];

        INSERT INTO journalist (id, first_name, last_name, global_email, global_phone)
        VALUES (
            gen_random_uuid(),
            first_name_var,
            last_name_var,
            lower(first_name_var) || '.' || lower(last_name_var) || '@email.com',
            '06' || lpad((floor(random() * 90000000) + 10000000)::text, 8, '0')
        ) RETURNING id INTO journalist_id_var;

        -- Generate a random number of activities (0 to 5)
        num_activities := floor(random() * 6);

        FOR j IN 1..num_activities LOOP
            -- Select a random media
            SELECT id INTO media_id_var FROM media_ids ORDER BY random() LIMIT 1;

            INSERT INTO activity (id, journalist_id, media_id, role, specific_email, specific_phone)
            VALUES (
                gen_random_uuid(),
                journalist_id_var,
                media_id_var,
                'Journaliste',
                lower(first_name_var) || '.' || lower(last_name_var) || (floor(random()*100))::text || '@' || (SELECT name FROM media WHERE id = media_id_var) || '.com',
                '07' || lpad((floor(random() * 90000000) + 10000000)::text, 8, '0')
            ) RETURNING id INTO activity_id_var;

            -- Assign 1 to 3 themes to the activity
            num_themes_per_activity := 1 + floor(random() * 3);

            FOR k IN 1..num_themes_per_activity LOOP
                -- Select a random theme
                SELECT id INTO theme_id_var FROM theme_ids ORDER BY random() LIMIT 1;

                -- Insert into activity_themes, ignoring duplicates
                INSERT INTO activity_themes (activity_id, theme_id)
                VALUES (activity_id_var, theme_id_var)
                ON CONFLICT DO NOTHING;
            END LOOP;
        END LOOP;

        -- Generate a random number of interaction logs (0 to 20)
        num_interactions := floor(random() * 21);

        -- Get all activity IDs for the current journalist into an array
        SELECT array_agg(id) INTO journalist_activities FROM activity WHERE journalist_id = journalist_id_var;

        FOR l IN 1..num_interactions LOOP
            log_activity_id := NULL; -- Default to no activity

            -- If the journalist has activities, 50% chance to link one
            IF array_length(journalist_activities, 1) > 0 AND random() > 0.5 THEN
                log_activity_id := journalist_activities[1 + floor(random() * array_length(journalist_activities, 1))];
            END IF;

            INSERT INTO interaction_log (id, journalist_id, activity_id, date, description)
            VALUES (
                gen_random_uuid(),
                journalist_id_var,
                log_activity_id,
                NOW() - (floor(random() * 1095) * interval '1 day'), -- Random date in the last 3 years
                'Interaction ' || l || ' pour ' || first_name_var || ' ' || last_name_var
            );
        END LOOP;

    END LOOP;

    -- Drop temp tables
    DROP TABLE media_ids;
    DROP TABLE theme_ids;
END $$;
