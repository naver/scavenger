--liquibase formatted sql

--changeset scavenger:1

CREATE TABLE IF NOT EXISTS customers
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    licenseKey          VARCHAR(40)  NOT NULL,
    createdAt           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updatedAt           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    groupId             VARCHAR(80)  NULL,

    CONSTRAINT ix_customers_licenseKey
        UNIQUE (licenseKey)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS agent_state
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    customerId         BIGINT      NOT NULL,
    jvmUuid            VARCHAR(40) NOT NULL,
    createdAt          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    lastPolledAt       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    nextPollExpectedAt TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    timestamp          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    enabled            TINYINT(1)  NOT NULL,

    CONSTRAINT ix_agent_state_identity
        UNIQUE (customerId, jvmUuid)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS applications
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    customerId BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    createdAt  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),

    CONSTRAINT ix_applications_name_identity
        UNIQUE (customerId, name),
    CONSTRAINT ix_applications_id_identity
        UNIQUE (customerId, id)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS codebase_fingerprints
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    customerId          BIGINT       NOT NULL,
    applicationId       BIGINT       NOT NULL,
    codeBaseFingerprint VARCHAR(200) NOT NULL,
    createdAt           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    publishedAt         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),

    CONSTRAINT ix_codebase_fingerprints_identity
        UNIQUE (customerId, applicationId, codeBaseFingerprint)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS environments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    customerId BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    createdAt  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updatedAt  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    enabled    TINYINT(1)   NOT NULL,

    CONSTRAINT ix_environments_name_identity
        UNIQUE (customerId, name),
    CONSTRAINT ix_environments_id_identity
        UNIQUE (customerId, id)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS jvms
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    customerId          BIGINT       NOT NULL,
    applicationId       BIGINT       NOT NULL,
    applicationVersion  VARCHAR(80)  NULL,
    environmentId       BIGINT       NOT NULL,
    uuid                VARCHAR(40)  NOT NULL,
    codeBaseFingerprint VARCHAR(200) NULL,
    createdAt           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    publishedAt         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    hostname            VARCHAR(255) NOT NULL,

    CONSTRAINT ix_jvms_uuid
        UNIQUE (customerId, uuid)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS methods
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    customerId       BIGINT       NOT NULL,
    visibility       VARCHAR(20)  NULL,
    signature        TEXT         NULL,
    signatureHash    VARCHAR(32)  NOT NULL,
    createdAt        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    lastSeenAtMillis BIGINT       NULL,
    declaringType    VARCHAR(256) NULL,
    methodName       TEXT NULL,
    modifiers        VARCHAR(50) NULL,
    garbage          TINYINT(1) NOT NULL DEFAULT 0,

    CONSTRAINT ix_methods_id_identity UNIQUE (customerId, id),
    CONSTRAINT ix_methods_signaturehash_identity UNIQUE (customerId, signatureHash)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS invocations
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    customerId       BIGINT                          NOT NULL,
    applicationId    BIGINT                          NOT NULL,
    environmentId    BIGINT                          NOT NULL,
    signatureHash    VARCHAR(32)                     NULL,
    invokedAtMillis  BIGINT                          NOT NULL,
    status           ENUM ('NOT_INVOKED', 'INVOKED') NOT NULL COMMENT 'Same values as SignatureStatus',
    createdAt        TIMESTAMP                       NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    lastSeenAtMillis BIGINT                          NULL,
    timestamp        TIMESTAMP                       NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),

    CONSTRAINT ix_invocations_identity UNIQUE (customerId, applicationId, environmentId, signatureHash)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS snapshots
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    createdAt             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    customerId            BIGINT        NOT NULL,
    name                  VARCHAR(100)  NOT NULL,
    filterInvokedAtMillis BIGINT        NULL,
    packages              VARCHAR(3000) NULL,
    status                VARCHAR(100)  NULL,
    excludeAbstract       TINYINT(1)    NULL
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS snapshot_application
(
    snapshotId    BIGINT NOT NULL,
    applicationId BIGINT NOT NULL,
    customerId    BIGINT NOT NULL,

    PRIMARY KEY (customerId, snapshotId, applicationId)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS snapshot_environment
(
    snapshotId    BIGINT NOT NULL,
    environmentId BIGINT NOT NULL,
    customerId    BIGINT NOT NULL,

    PRIMARY KEY (customerId, snapshotId, environmentId)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS snapshot_nodes
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshotId          BIGINT        NOT NULL,
    signature           VARCHAR(1000) NOT NULL,
    usedCount           INT           NOT NULL,
    unUsedCount         INT           NOT NULL,
    parent              VARCHAR(1000) NOT NULL,
    customerId          BIGINT        NOT NULL,
    lastInvokedAtMillis BIGINT        NULL,
    type                VARCHAR(100)  NULL
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS `github_mappings`
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    customerId BIGINT       NOT NULL,
    package    VARCHAR(300) NOT NULL,
    url        VARCHAR(300) NOT NULL,

    CONSTRAINT ix_github_mappings_identity
        UNIQUE (customerId, package)
) COLLATE = utf8mb4_0900_as_cs;

CREATE TABLE IF NOT EXISTS `leadership` (
    anchor      TINYINT(3) unsigned NOT NULL PRIMARY KEY,
    memberId   VARCHAR(128)   NOT NULL,
    lastSeenActive TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) COLLATE = utf8mb4_0900_as_cs;
