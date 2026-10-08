--liquibase formatted sql
--changeset elgregos:13
alter table invitation
    add column if not exists invitation_data jsonb;

update invitation
set invitation_data = jsonb_build_object(
    'label', label,
    'description', description,
    'deliveryMethod', null,
    'postalAddress', null,
    'guestIds', coalesce(
        (
            select jsonb_agg(ig.guest_id::text order by ig.guest_id)
            from invitation_guest ig
            where ig.invitation_id = invitation.id
        ),
        '[]'::jsonb
    ),
    'accessToken', access_token
);

alter table invitation
    alter column invitation_data set not null;

alter table invitation
    drop column if exists label,
    drop column if exists description,
    drop column if exists access_token;

drop table if exists invitation_guest;

create unique index if not exists uk_invitation_access_token
    on invitation ((invitation_data ->> 'accessToken'));

alter table invitation
    add constraint ck_invitation_required_text_fields
        check (
            coalesce(jsonb_typeof(invitation_data -> 'label'), '') = 'string'
            and coalesce(jsonb_typeof(invitation_data -> 'description'), '') = 'string'
            and coalesce(jsonb_typeof(invitation_data -> 'accessToken'), '') = 'string'
            and length(btrim(invitation_data ->> 'accessToken')) > 0
        ),
    add constraint ck_invitation_posted_requires_postal_address
        check (
            invitation_data ->> 'deliveryMethod' is distinct from 'POSTED'
            or coalesce(jsonb_typeof(invitation_data -> 'postalAddress'), '') = 'object'
        ),
    add constraint ck_invitation_guest_ids_not_empty
        check (
            case
                when jsonb_typeof(invitation_data -> 'guestIds') = 'array'
                    then jsonb_array_length(invitation_data -> 'guestIds') > 0
                else false
            end
        );

--rollback alter table invitation drop constraint if exists ck_invitation_guest_ids_not_empty; alter table invitation drop constraint if exists ck_invitation_posted_requires_postal_address; alter table invitation drop constraint if exists ck_invitation_required_text_fields;
--rollback drop index if exists uk_invitation_access_token;
--rollback create table if not exists invitation_guest (invitation_id uuid not null, guest_id uuid not null, primary key (invitation_id, guest_id), constraint fk_invitation_guest_invitation foreign key (invitation_id) references invitation (id) on delete cascade, constraint fk_invitation_guest_guest foreign key (guest_id) references guest (id) on delete restrict, constraint uk_invitation_guest_guest_id unique (guest_id));
--rollback insert into invitation_guest (invitation_id, guest_id) select i.id, g.value::uuid from invitation i, jsonb_array_elements_text(i.invitation_data -> 'guestIds') g;
--rollback alter table invitation add column if not exists label text; alter table invitation add column if not exists description text; alter table invitation add column if not exists access_token text;
--rollback update invitation set label = invitation_data ->> 'label', description = invitation_data ->> 'description', access_token = invitation_data ->> 'accessToken';
--rollback alter table invitation alter column label set not null, alter column description set not null, alter column access_token set default gen_random_uuid()::text, alter column access_token set not null;
--rollback alter table invitation add constraint uk_invitation_access_token unique (access_token);
--rollback alter table invitation drop column if exists invitation_data;



