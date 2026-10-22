--liquibase formatted sql
--changeset elgregos:14
alter table guest_magic_link_token
    add column mode text not null default 'INDIVIDUAL';

alter table guest_magic_link_token
    add constraint ck_guest_magic_link_token_mode check (mode in ('INDIVIDUAL', 'GROUP'));

--rollback alter table guest_magic_link_token drop constraint ck_guest_magic_link_token_mode;
--rollback alter table guest_magic_link_token drop column mode;

