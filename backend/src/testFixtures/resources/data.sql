
truncate table guest_magic_link_token, invitation, guest cascade;

insert into guest (id, version, creation_date, update_date, first_name, last_name, email) values ('019e82e0-0cc1-727e-a483-947fea529ef3', 1, '2026-06-13 10:00:00', '2026-06-13 10:00:00', 'John', 'Doe', 'john.doe@example.com');
insert into guest (id, version, creation_date, update_date, first_name, last_name, email) values ('019e8807-5321-75ed-8a40-99a73529e50f', 1, '2026-06-13 10:00:00', '2026-06-13 10:00:00', 'Jane', 'Doe', 'jane.doe@example.com');
insert into guest (id, version, creation_date, update_date, first_name, last_name, email) values ('019f2287-efe1-748b-a7f6-e20962d062cf', 1, '2026-06-13 10:00:00', '2026-06-13 10:00:00', 'Mickael', 'Kael', 'mickael.kael@example.com');

insert into invitation (id, version, creation_date, update_date, invitation_data) values ('019f2282-6db9-72a3-8d9d-2f6dc80cb89d', 1, '2026-07-01 10:45:28', '2026-07-01 10:45:28', jsonb_build_object('label', 'Bridesmaid', 'description', 'Bridesmaid invitation', 'deliveryMethod', 'POSTED', 'postalAddress', jsonb_build_object('line1', '12 Rue de la Paix', 'line2', null, 'line3', null, 'postalCode', '75002', 'locality', 'Paris', 'region', null, 'countryCode', 'FR'), 'guestIds', jsonb_build_array('019e8807-5321-75ed-8a40-99a73529e50f'), 'accessToken', 'dca71c6f-4b29-43a0-80df-426786ca9075'));
insert into invitation (id, version, creation_date, update_date, invitation_data) values ('019f2282-71a1-7276-87f9-f80375e2570e', 1, '2026-07-01 10:45:28', '2026-07-01 10:45:28', jsonb_build_object('label', 'Bestman', 'description', 'Best man invitation', 'deliveryMethod', 'POSTED', 'postalAddress', jsonb_build_object('line1', '8 Rue du Rhône', 'line2', null, 'line3', null, 'postalCode', '1204', 'locality', 'Genève', 'region', null, 'countryCode', 'CH'), 'guestIds', jsonb_build_array('019f2287-efe1-748b-a7f6-e20962d062cf'), 'accessToken', '35cae9dd-4e1a-4a01-95c0-4c286cafc3e4'));

