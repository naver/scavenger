CREATE TABLE customers_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE agent_state_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE applications_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE codebase_fingerprints_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE environments_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE jvms_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE methods_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE invocations_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE snapshots_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE snapshot_nodes_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE github_mappings_sequence (id BIGINT, next_id BIGINT, cache BIGINT, PRIMARY KEY (id)) comment 'vitess_sequence';
CREATE TABLE `leadership`
(
    anchor         TINYINT(3) unsigned NOT NULL PRIMARY KEY,
    memberId       VARCHAR(128) NOT NULL,
    lastSeenActive TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) COLLATE = utf8mb4_0900_as_cs;
