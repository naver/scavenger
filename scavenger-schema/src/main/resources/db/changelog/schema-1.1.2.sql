--liquibase formatted sql

--changeset scavenger:5

ALTER TABLE snapshot_nodes MODIFY COLUMN signature TEXT;
