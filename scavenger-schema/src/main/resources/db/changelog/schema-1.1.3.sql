--liquibase formatted sql

--changeset scavenger:6

ALTER TABLE snapshot_nodes MODIFY COLUMN signature TEXT NOT NULL;
