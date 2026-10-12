delete from guest_magic_link_token where guest_id in (select id from guest where email like '%@sample.dywy.cloud');
delete from guest_rsvp where guest_id in (select id from guest where email like '%@sample.dywy.cloud');
delete from invitation where exists (
    select 1 from guest
    where guest.email like '%@sample.dywy.cloud'
      and jsonb_exists(invitation.invitation_data -> 'guestIds', guest.id::text));
delete from guest where email like '%@sample.dywy.cloud';

insert into guest (id, version, creation_date, update_date, deletion_date, first_name, last_name, email, language) values
    ('01a0f642-cddf-7ffa-b00a-ac5f949b020e', 1, '2026-10-01 06:59:28.351', '2026-10-01 06:59:28.351', null, 'Alice', 'Martin', 'alice.martin@sample.dywy.cloud', 'FR'),
    ('01a0f642-cddf-7e45-b4c3-a8bdba94f97e', 1, '2026-10-01 06:59:28.351', '2026-10-01 06:59:28.351', null, 'Bruno', 'Martin', 'bruno.martin@sample.dywy.cloud', 'FR'),
    ('01a0f647-62c4-70ff-9fe9-646dd9b7c2b2', 1, '2026-10-01 07:04:28.612', '2026-10-01 07:04:28.612', null, 'Clara', 'Dupont', 'clara.dupont@sample.dywy.cloud', 'FR'),
    ('01a0f64b-f455-71c2-99bf-17f90eeeb4d2', 1, '2026-10-01 07:09:28.021', '2026-10-01 07:09:28.021', null, 'David', 'Smith', 'david.smith@sample.dywy.cloud', 'EN'),
    ('01a0f650-8bd2-7f11-b104-48c170ba34c4', 1, '2026-10-01 07:14:28.946', '2026-10-01 07:14:28.946', null, 'Emily', 'Johnson', 'emily.johnson@sample.dywy.cloud', 'EN'),
    ('01a0f650-8bd2-7773-b764-950192d6cb99', 1, '2026-10-01 07:14:28.946', '2026-10-01 07:14:28.946', null, 'Frank', 'Johnson', 'frank.johnson@sample.dywy.cloud', 'EN'),
    ('01a0f650-8bd2-79cb-a420-361ed65758ed', 1, '2026-10-01 07:14:28.946', '2026-10-01 07:14:28.946', null, 'Grace', 'Johnson', 'grace.johnson@sample.dywy.cloud', 'EN'),
    ('01a0f655-1fe2-7de1-904c-9c72e2584612', 1, '2026-10-01 07:19:28.994', '2026-10-01 07:19:28.994', null, 'Hans', 'Muller', 'hans.muller@sample.dywy.cloud', 'FR'),
    ('01a0f659-b2ed-7f2c-a452-c3772c0b545c', 1, '2026-10-01 07:24:28.781', '2026-10-01 07:24:28.781', null, 'Isabelle', 'Rey', 'isabelle.rey@sample.dywy.cloud', 'FR'),
    ('01a0f65e-4546-7d6b-906a-76925bbc8b6a', 1, '2026-10-01 07:29:28.390', '2026-10-01 07:29:28.390', null, 'Jack', 'Wilson', 'jack.wilson@sample.dywy.cloud', 'EN'),
    ('01a0f662-d9d0-76fc-80e0-63d1334601f9', 1, '2026-10-01 07:34:28.560', '2026-10-01 07:34:28.560', null, 'Karine', 'Leroy', 'karine.leroy@sample.dywy.cloud', 'FR'),
    ('01a0f667-6ca0-7ff8-b284-91c38236de99', 2, '2026-10-01 07:39:28.288', '2026-10-10 15:59:28.662', '2026-10-10 15:59:28.662', 'Louis', 'Petit', 'louis.petit@sample.dywy.cloud', 'FR'),
    ('01a0f64b-f455-7c4f-8ca5-a1883fc4a46e', 1, '2026-10-01 07:09:28.021', '2026-10-01 07:09:28.021', null, 'Eva', 'Smith', 'eva.smith@sample.dywy.cloud', 'EN')
on conflict (id) do update set
    version = excluded.version,
    creation_date = excluded.creation_date,
    update_date = excluded.update_date,
    deletion_date = excluded.deletion_date,
    first_name = excluded.first_name,
    last_name = excluded.last_name,
    email = excluded.email,
    language = excluded.language;

insert into invitation (id, version, creation_date, update_date, invitation_data) values
    ('01a0fba0-19e3-7050-993d-e898eb856439', 1, '2026-10-02 07:59:28.739', '2026-10-02 07:59:28.739', jsonb_build_object(
        'schemaVersion', 1,
        'label', 'Alice & Bruno Martin',
        'description', 'Posted couple invitation with a full multi-line French address',
        'deliveryMethod', 'POSTED',
        'postalAddress', jsonb_build_object('line1', '12 Rue de la Paix', 'line2', 'Bâtiment B', 'line3', 'Chez Martin', 'postalCode', '75002', 'locality', 'Paris', 'region', 'Île-de-France', 'countryCode', 'FR'),
        'guestIds', jsonb_build_array('01a0f642-cddf-7ffa-b00a-ac5f949b020e', '01a0f642-cddf-7e45-b4c3-a8bdba94f97e'),
        'accessToken', 'c6a58cd1-43b3-4de1-aff3-eeb00ad6775e')),
    ('01a0fba4-ad0c-70d2-9084-11997d6d0207', 1, '2026-10-02 08:04:28.556', '2026-10-02 08:04:28.556', jsonb_build_object(
        'schemaVersion', 1,
        'label', 'Clara Dupont',
        'description', 'Posted solo invitation with a minimal Belgian address',
        'deliveryMethod', 'POSTED',
        'postalAddress', jsonb_build_object('line1', '5 Place Rouppe', 'line2', null, 'line3', null, 'postalCode', null, 'locality', 'Bruxelles', 'region', null, 'countryCode', 'BE'),
        'guestIds', jsonb_build_array('01a0f647-62c4-70ff-9fe9-646dd9b7c2b2'),
        'accessToken', 'beb382b0-23f4-418d-9767-ef07fc632511')),
    ('01a0fba9-3fa9-7896-8320-64007a81781e', 1, '2026-10-02 08:09:28.233', '2026-10-02 08:09:28.233', jsonb_build_object(
        'schemaVersion', 1,
        'label', 'David & Eva Smith',
        'description', 'Posted couple invitation abroad with a region and mixed answers (one declined, one attending)',
        'deliveryMethod', 'POSTED',
        'postalAddress', jsonb_build_object('line1', '221B Baker Street', 'line2', null, 'line3', null, 'postalCode', 'NW1 6XE', 'locality', 'London', 'region', 'England', 'countryCode', 'GB'),
        'guestIds', jsonb_build_array('01a0f64b-f455-71c2-99bf-17f90eeeb4d2', '01a0f64b-f455-7c4f-8ca5-a1883fc4a46e'),
        'accessToken', '7fae6d71-0277-43c6-9f3e-d45434dfe7b3')),
    ('01a0fbad-d46d-7f10-99a8-485d6e73439a', 1, '2026-10-02 08:14:28.461', '2026-10-02 08:14:28.461', jsonb_build_object(
        'schemaVersion', 1,
        'label', 'Johnson family',
        'description', 'Hand-delivered family invitation with three guests and no address',
        'deliveryMethod', 'HAND_DELIVERED',
        'postalAddress', null,
        'guestIds', jsonb_build_array('01a0f650-8bd2-7f11-b104-48c170ba34c4', '01a0f650-8bd2-7773-b764-950192d6cb99', '01a0f650-8bd2-79cb-a420-361ed65758ed'),
        'accessToken', '9a7e5832-3a9f-4c11-8df6-6ca765e0520a')),
    ('01a0fbb2-694a-75e6-9394-a04d39a67a3e', 1, '2026-10-02 08:19:28.714', '2026-10-02 08:19:28.714', jsonb_build_object(
        'schemaVersion', 1,
        'label', 'Hans Muller',
        'description', 'Invitation without delivery method yet',
        'deliveryMethod', null,
        'postalAddress', null,
        'guestIds', jsonb_build_array('01a0f655-1fe2-7de1-904c-9c72e2584612'),
        'accessToken', '519ef0b0-9ceb-442e-b755-74d8de81f634')),
    ('01a0fbb6-fbee-78ed-9b69-1b71132b0e54', 1, '2026-10-02 08:24:28.398', '2026-10-02 08:24:28.398', jsonb_build_object(
        'schemaVersion', 1,
        'label', 'Isabelle Rey',
        'description', 'Posted solo invitation to Switzerland',
        'deliveryMethod', 'POSTED',
        'postalAddress', jsonb_build_object('line1', '8 Rue du Rhône', 'line2', null, 'line3', null, 'postalCode', '1204', 'locality', 'Genève', 'region', null, 'countryCode', 'CH'),
        'guestIds', jsonb_build_array('01a0f659-b2ed-7f2c-a452-c3772c0b545c'),
        'accessToken', '1ad1ef7c-f953-486d-968d-d19937cf97ae')),
    ('01a0fbbb-9069-7b88-9480-495eee89fa88', 1, '2026-10-02 08:29:28.553', '2026-10-02 08:29:28.553', jsonb_build_object(
        'schemaVersion', 1,
        'label', 'Jack Wilson',
        'description', 'Posted solo invitation to the United States with a state',
        'deliveryMethod', 'POSTED',
        'postalAddress', jsonb_build_object('line1', '350 Fifth Avenue', 'line2', 'Floor 21', 'line3', null, 'postalCode', '10118', 'locality', 'New York', 'region', 'NY', 'countryCode', 'US'),
        'guestIds', jsonb_build_array('01a0f65e-4546-7d6b-906a-76925bbc8b6a'),
        'accessToken', 'd11f32bb-f8da-4ce0-8f9f-f4cce07decda'))
on conflict (id) do update set
    version = excluded.version,
    creation_date = excluded.creation_date,
    update_date = excluded.update_date,
    invitation_data = excluded.invitation_data;

insert into guest_rsvp (id, guest_id, version, creation_date, update_date, attendance, answers) values
    ('01a10b81-09b9-7a97-b726-61c15ab912e9', '01a0f642-cddf-7ffa-b00a-ac5f949b020e', 1, '2026-10-05 09:59:28.441', '2026-10-05 09:59:28.441', 'ATTENDING', '{"meal":"MEAT","song":{"deezerId":3135556,"title":"La Vie en rose","artist":"Édith Piaf","link":"https://www.deezer.com/track/3135556","preview":"https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3","synchronized":true}}'::jsonb),
    ('01a10b85-9dcb-7523-9cad-58c0e49ba0e0', '01a0f642-cddf-7e45-b4c3-a8bdba94f97e', 1, '2026-10-05 10:04:28.491', '2026-10-05 10:04:28.491', 'ATTENDING', '{"meal":"VEGGIE","song":null}'::jsonb),
    ('01a10bb7-fa17-71cf-a7e8-61dfb080581e', '01a0f647-62c4-70ff-9fe9-646dd9b7c2b2', 2, '2026-10-05 10:59:28.919', '2026-10-06 06:29:28.394', 'ATTENDING', '{"meal":"FISH","song":{"deezerId":12345,"title":"Bohemian Rhapsody","artist":"Queen","link":"https://www.deezer.com/track/12345","preview":null,"synchronized":false}}'::jsonb),
    ('01a10bee-e5ad-78fe-bef0-cfca7a5cef80', '01a0f64b-f455-71c2-99bf-17f90eeeb4d2', 1, '2026-10-05 11:59:28.173', '2026-10-05 11:59:28.173', 'DECLINED', null),
    ('01a10bf3-7c9a-7ae0-bfb3-2167d18f9600', '01a0f64b-f455-7c4f-8ca5-a1883fc4a46e', 1, '2026-10-05 12:04:28.954', '2026-10-05 12:04:28.954', 'ATTENDING', '{"meal":"FISH","song":null}'::jsonb),
    ('01a1155f-e694-79ed-a063-cd978e2f7e27', '01a0f650-8bd2-7f11-b104-48c170ba34c4', 1, '2026-10-07 07:59:28.916', '2026-10-07 07:59:28.916', 'ATTENDING', '{"meal":"MEAT","song":{"deezerId":67890,"title":"Dancing Queen","artist":"ABBA","link":"https://www.deezer.com/track/67890","preview":null,"synchronized":true}}'::jsonb),
    ('01a11564-797d-7bb6-8f25-b02b94463dd1', '01a0f650-8bd2-7773-b764-950192d6cb99', 1, '2026-10-07 08:04:28.669', '2026-10-07 08:04:28.669', 'ATTENDING', '{"meal":"FISH","song":null}'::jsonb),
    ('01a11a4f-53fc-790b-865a-35bfde6d9fc5', '01a0f659-b2ed-7f2c-a452-c3772c0b545c', 3, '2026-10-08 06:59:28.892', '2026-10-12 17:44:28.089', 'DECLINED', null),
    ('01a120f6-337f-7b9f-b0d6-3f3447d2f966', '01a0f65e-4546-7d6b-906a-76925bbc8b6a', 1, '2026-10-09 13:59:28.383', '2026-10-09 13:59:28.383', 'ATTENDING', '{"meal":"VEGGIE","song":{"deezerId":917265,"title":"September","artist":"Earth, Wind & Fire","link":"https://www.deezer.com/track/917265","preview":"https://cdns-preview.deezer.com/stream/september.mp3","synchronized":false}}'::jsonb)
on conflict (id) do update set
    guest_id = excluded.guest_id,
    version = excluded.version,
    creation_date = excluded.creation_date,
    update_date = excluded.update_date,
    attendance = excluded.attendance,
    answers = excluded.answers;

insert into guest_magic_link_token (id, token_hash, invitation_id, guest_id, creation_date, expires_at, used_at) values
    ('01a10058-99bd-751e-be9e-d592b75a4142', encode(sha256(convert_to('3f6c2b8e-9a41-4d7a-8c52-1e0b7d5a9f34', 'UTF8')), 'hex'), '01a0fba0-19e3-7050-993d-e898eb856439', '01a0f642-cddf-7ffa-b00a-ac5f949b020e', '2026-10-03 05:59:28.957', '2026-10-03 06:14:28.975', null),
    ('01a10b9c-7f71-75e3-96d4-0ece8a177105', encode(sha256(convert_to('b81d4e07-5c3a-4f92-a6d8-72c9e0f1b3a5', 'UTF8')), 'hex'), '01a0fba4-ad0c-70d2-9084-11997d6d0207', '01a0f647-62c4-70ff-9fe9-646dd9b7c2b2', '2026-10-05 10:29:28.049', '2026-10-05 10:44:28.949', '2026-10-05 10:34:28.191')
on conflict (id) do update set
    token_hash = excluded.token_hash,
    invitation_id = excluded.invitation_id,
    guest_id = excluded.guest_id,
    creation_date = excluded.creation_date,
    expires_at = excluded.expires_at,
    used_at = excluded.used_at;

