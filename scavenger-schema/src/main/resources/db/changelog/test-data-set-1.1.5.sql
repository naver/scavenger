--liquibase formatted sql

--changeset scavenger:10

INSERT INTO environments (id, customerid, name, createdat, updatedat, enabled)
VALUES (2, 1, 'prod', '2026-01-05 09:00:00.000000', '2026-01-05 09:00:00.000000', 1),
       (3, 1, 'staging', '2026-01-05 09:00:00.000000', '2026-01-05 09:00:00.000000', 0);

INSERT INTO agent_state (id, customerid, jvmuuid, createdat, lastpolledat, nextpollexpectedat, timestamp, enabled)
VALUES (3, 1, '7f1d2c3b-5a64-4e80-9c1f-2b8e0da17777', '2026-01-05 09:01:00.000000', '2026-01-10 09:00:00.000000',
        '2026-01-10 09:02:00.000000', '2026-01-10 09:00:00.000000', 1);

INSERT INTO jvms (id, customerid, applicationid, applicationversion, environmentid, uuid, codebasefingerprint,
                  createdat, publishedat, hostname)
VALUES (3, 1, 1, 'unspecified', 2, '7f1d2c3b-5a64-4e80-9c1f-2b8e0da17777',
        'c8b9f2048be8cf919e563767d5213709f7cef5640d2f775c6a10b9b69d28be13',
        '2026-01-05 09:01:00.000000', '2026-01-10 09:00:00.000000', 'PROD-HOST-01');

-- customer 2: MCP scenario data isolated from customer 1, whose method counts the
-- collector GarbageCollectServiceTest asserts exactly. No call_stacks rows on purpose
-- (powers the DISABLED_OR_NO_DATA state).
INSERT INTO customers (id, name, licensekey, createdat, updatedat, groupId)
VALUES (2, 'demo-second', '11e8e9f2-7a5b-4f00-bd1e-1f2a3c4d5e6f', '2026-01-05 09:00:00.000000',
        '2026-01-05 09:00:00.000000', 'default-group');

INSERT INTO applications (id, customerid, name, createdat)
VALUES (3, 2, 'other-app', '2026-01-05 09:00:00.000000');

INSERT INTO environments (id, customerid, name, createdat, updatedat, enabled)
VALUES (4, 2, 'test', '2026-01-05 09:00:00.000000', '2026-01-05 09:00:00.000000', 1),
       (5, 2, 'prod', '2026-01-05 09:00:00.000000', '2026-01-05 09:00:00.000000', 1),
       (6, 2, 'staging', '2026-01-05 09:00:00.000000', '2026-01-05 09:00:00.000000', 0);

INSERT INTO methods (id, customerid, visibility, signature, signaturehash, createdat, lastseenatmillis, declaringtype,
                     methodname, modifiers, garbage)
VALUES (35, 2, 'public', 'com.example.demo.legacy.LegacyService.oldMethod()', 'a1b2c3d4e5f60718293a4b5c6d7e8f90',
        '2026-01-05 09:01:00.000000', 1767862800000, 'com.example.demo.legacy.LegacyService', 'oldMethod', 'public', 0),
       (36, 2, 'public', 'com.example.demo.legacy.LegacyService.usedInTestOnly()', 'b2c3d4e5f60718293a4b5c6d7e8f90a1',
        '2026-01-05 09:01:00.000000', 1767862800000, 'com.example.demo.legacy.LegacyService', 'usedInTestOnly', 'public', 0),
       (37, 2, 'public', 'com.example.demo.legacy.LegacyContract.mustImplement()', 'c3d4e5f60718293a4b5c6d7e8f90a1b2',
        '2026-01-05 09:01:00.000000', 1767862800000, 'com.example.demo.legacy.LegacyContract', 'mustImplement',
        'public abstract', 0),
       (38, 2, 'public', 'com.example.demo.legacy.LegacyService.removedMethod()', 'd4e5f60718293a4b5c6d7e8f90a1b2c3',
        '2026-01-05 09:01:00.000000', 1767862800000, 'com.example.demo.legacy.LegacyService', 'removedMethod', 'public', 1),
       (39, 2, 'public', 'com.other.App.run()', 'e5f60718293a4b5c6d7e8f90a1b2c3d4',
        '2026-01-05 09:01:00.000000', 1767862800000, 'com.other.App', 'run', 'public', 0);

-- usedInTestOnly (b2c3...) is INVOKED in env 'test'(4) but NOT_INVOKED in 'prod'(5) —
-- powers the cross-env divergence assertions in MethodUsageQueryServiceTest / StaleMethodSearchServiceTest
INSERT INTO invocations (id, customerid, applicationid, environmentid, signaturehash, invokedatmillis, status,
                         createdat, lastseenatmillis, timestamp)
VALUES (36, 2, 3, 4, 'a1b2c3d4e5f60718293a4b5c6d7e8f90', 0, 'NOT_INVOKED', '2026-01-05 09:02:00.000', 1767862800000,
        '2026-01-05 09:02:00.000'),
       (37, 2, 3, 5, 'a1b2c3d4e5f60718293a4b5c6d7e8f90', 0, 'NOT_INVOKED', '2026-01-05 09:02:00.000', 1767862800000,
        '2026-01-05 09:02:00.000'),
       (38, 2, 3, 4, 'b2c3d4e5f60718293a4b5c6d7e8f90a1', 1767603600000, 'INVOKED', '2026-01-05 09:02:00.000',
        1767862800000, '2026-01-08 09:00:00.000'),
       (39, 2, 3, 5, 'b2c3d4e5f60718293a4b5c6d7e8f90a1', 0, 'NOT_INVOKED', '2026-01-05 09:02:00.000', 1767862800000,
        '2026-01-05 09:02:00.000'),
       (40, 2, 3, 4, 'c3d4e5f60718293a4b5c6d7e8f90a1b2', 0, 'NOT_INVOKED', '2026-01-05 09:02:00.000', 1767862800000,
        '2026-01-05 09:02:00.000'),
       (41, 2, 3, 4, 'd4e5f60718293a4b5c6d7e8f90a1b2c3', 0, 'NOT_INVOKED', '2026-01-05 09:02:00.000', 1767862800000,
        '2026-01-05 09:02:00.000'),
       (42, 2, 3, 4, 'e5f60718293a4b5c6d7e8f90a1b2c3d4', 0, 'NOT_INVOKED', '2026-01-05 09:02:00.000', 1767862800000,
        '2026-01-05 09:02:00.000');
