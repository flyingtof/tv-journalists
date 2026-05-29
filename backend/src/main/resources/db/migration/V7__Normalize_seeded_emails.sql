UPDATE journalist
SET global_email = regexp_replace(global_email, '\s+', '-', 'g')
WHERE global_email IS NOT NULL
  AND global_email ~ '\s';

UPDATE activity
SET specific_email = regexp_replace(specific_email, '\s+', '-', 'g')
WHERE specific_email IS NOT NULL
  AND specific_email ~ '\s';
