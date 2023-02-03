--liquibase formatted sql

--changeset scavenger:2

INSERT INTO customers (id, name, licensekey, createdat, updatedat, groupId)
VALUES (1, 'demo', '4c94e0dd-ad04-4b17-9238-f46bba75c684', '2022-02-28 14:38:14.167074', '2022-02-28 14:38:14.167074',
        'default-group');

INSERT INTO agent_state (id, customerid, jvmuuid, createdat, lastpolledat, nextpollexpectedat, timestamp, enabled)
VALUES (1, 1, 'd0dfa3c2-809c-428f-b501-7419197d91c5', '2022-02-28 14:39:14.474391', '2022-02-28 14:43:34.954776',
        '2022-02-28 14:45:34.954776', '2022-02-28 14:43:34.968998', 1);

INSERT INTO applications (id, customerid, name, createdat)
VALUES (1, 1, 'demo', '2022-02-28 14:39:03.668000');

INSERT INTO codebase_fingerprints (id, customerid, applicationid, codebasefingerprint, createdat, publishedat)
VALUES (1, 1, 1,
        'CodeBaseFingerprint(numClassFiles=0, numJarFiles=1, sha256=b90e15678202f78cee45fa05cef8cba7c070114e37e81ff9131858c1d9c488c7)',
        '2022-02-28 14:39:14.982000', '2022-02-28 14:39:14.982');

INSERT INTO environments (id, customerid, name, createdat, updatedat, enabled)
VALUES (1, 1, 'test', '2022-02-28 14:39:03.668000', '2022-02-28 14:39:15.123202', 1);

INSERT INTO methods (id, customerid, visibility, signature, signaturehash, createdat, lastseenatmillis, declaringtype,
                     methodname, modifiers, garbage)
VALUES (1, 1, 'public', 'com.example.demo.MyController.additional()', 'c6ca6a0e5342c08dbd7617617b156e9b',
        '2022-02-28 14:39:14.982000', 1646026755181, 'com.example.demo.MyController', 'additional', 'public', 0),
       (2, 1, 'public', 'com.example.demo.MyController.getMyService()', 'e776e2183ba68bcfc4ab748faf687d94',
        '2022-02-28 14:39:14.982000', 1646026755181, 'com.example.demo.MyController', 'getMyService', 'public', 0),
       (3, 1, 'public', 'com.example.demo.additional.AdditionalService.get()', '1b3fdd577d38e12aca87e111a65c5c47',
        '2022-02-28 14:39:14.982000', 1646026755181, 'com.example.demo.additional.AdditionalService', 'get',
        'public final', 0),
       (4, 1, 'public', 'com.example.demo.additional.AdditionalService.WOW.doSth()', '22abf00dd5d3310e46d2fd4340aa6d0c',
        '2022-02-28 14:39:14.982000', 1646026755181, 'com.example.demo.additional.AdditionalService$WOW', 'doSth',
        'public final', 0),
       (5, 1, 'public', 'com.example.demo.MyController.hello()', 'f7d2cf2e97f3190a9dac392b072e2591',
        '2022-02-28 14:39:14.982000', 1646026755181, 'com.example.demo.MyController', 'hello', 'public', 0);

INSERT INTO invocations (id, customerid, applicationid, environmentid, signaturehash, invokedatmillis, status,
                         createdat, lastseenatmillis, timestamp)
VALUES (1, 1, 1, 1, 'c6ca6a0e5342c08dbd7617617b156e9b', 0, 'NOT_INVOKED', '2022-02-28 14:39:15.282', 1646026755181,
        '2022-02-28 14:39:15.282'),
       (2, 1, 1, 1, 'e776e2183ba68bcfc4ab748faf687d94', 1646027014967, 'INVOKED', '2022-02-28 14:39:15.285',
        1646026755181, '2022-02-28 14:43:54.067'),
       (3, 1, 1, 1, '1b3fdd577d38e12aca87e111a65c5c47', 0, 'NOT_INVOKED', '2022-02-28 14:39:15.285', 1646026755181,
        '2022-02-28 14:39:15.285'),
       (4, 1, 1, 1, '22abf00dd5d3310e46d2fd4340aa6d0c', 0, 'NOT_INVOKED', '2022-02-28 14:39:15.286', 1646026755181,
        '2022-02-28 14:39:15.286'),
       (5, 1, 1, 1, 'f7d2cf2e97f3190a9dac392b072e2591', 1646027033946, 'INVOKED', '2022-02-28 14:39:15.286',
        1646026755181, '2022-02-28 14:44:13.997');

INSERT INTO jvms (id, customerid, applicationid, applicationversion, environmentid, uuid, codebasefingerprint,
                  createdat, publishedat, hostname)
VALUES (1, 1, 1, 'unspecified', 1, 'd0dfa3c2-809c-428f-b501-7419197d91c5',
        'CodeBaseFingerprint(numClassFiles=0, numJarFiles=1, sha256=b90e15678202f78cee45fa05cef8cba7c070114e37e81ff9131858c1d9c488c7)',
        '2022-02-28 14:39:15.134725', '2022-02-28 14:44:13.948000', 'AL01978856.local');

INSERT INTO github_mappings (id, customerid, package, url)
VALUES (1, 1, 'com.example.demo', 'https://github_url/tree/develop/scavenger-demo/src/main/kotlin/com/example/demo');

INSERT INTO snapshots (id, createdat, customerid, name, filterinvokedatmillis, packages, status, excludeabstract)
VALUES (1, '2022-02-28 14:42:25.089098', 1, 'test', 0, '', null, null),
       (2, '2022-02-28 14:44:05.021461', 1, 'testt', 0, '', null, null);

INSERT INTO snapshot_application (snapshotid, applicationid, customerid)
VALUES (1, 1, 1),
       (2, 1, 1);

INSERT INTO snapshot_environment (snapshotid, environmentid, customerid)
VALUES (1, 1, 1),
       (2, 1, 1);

INSERT INTO snapshot_nodes (id, snapshotid, signature, usedcount, unusedcount, parent, customerid, lastinvokedatmillis,
                            type)
VALUES (1, 1, 'com.example.demo.additional.AdditionalService.get()', 0, 1,
        'com.example.demo.additional.AdditionalService', 1, null, 'METHOD'),
       (2, 1, 'com.example.demo.additional.AdditionalService.WOW.doSth()', 0, 1,
        'com.example.demo.additional.AdditionalService.WOW', 1, null, 'METHOD'),
       (3, 1, 'com.example.demo.additional.AdditionalService.WOW', 0, 1,
        'com.example.demo.additional.AdditionalService', 1, null, 'CLASS'),
       (4, 1, 'com.example.demo.additional.AdditionalService', 0, 2, 'com.example.demo.additional', 1, null, 'CLASS'),
       (5, 1, 'com.example.demo.additional', 0, 2, 'com.example.demo', 1, null, 'PACKAGE'),
       (6, 1, 'com.example.demo.MyController.hello()', 0, 1, 'com.example.demo.MyController', 1, null, 'METHOD'),
       (7, 1, 'com.example.demo.MyController.additional()', 0, 1, 'com.example.demo.MyController', 1, null, 'METHOD'),
       (8, 1, 'com.example.demo.MyController.getMyService()', 0, 1, 'com.example.demo.MyController', 1, null, 'METHOD'),
       (9, 1, 'com.example.demo.MyController', 0, 3, 'com.example.demo', 1, null, 'CLASS'),
       (10, 1, 'com.example.demo', 0, 5, '', 1, null, 'PACKAGE'),
       (21, 2, 'com.example.demo.additional.AdditionalService.get()', 0, 1,
        'com.example.demo.additional.AdditionalService', 1, null, 'METHOD'),
       (22, 2, 'com.example.demo.additional.AdditionalService.WOW.doSth()', 0, 1,
        'com.example.demo.additional.AdditionalService.WOW', 1, null, 'METHOD'),
       (23, 2, 'com.example.demo.additional.AdditionalService.WOW', 0, 1,
        'com.example.demo.additional.AdditionalService', 1, null, 'CLASS'),
       (24, 2, 'com.example.demo.additional.AdditionalService', 0, 2, 'com.example.demo.additional', 1, null, 'CLASS'),
       (25, 2, 'com.example.demo.additional', 0, 2, 'com.example.demo', 1, null, 'PACKAGE'),
       (26, 2, 'com.example.demo.MyController.hello()', 1, 0, 'com.example.demo.MyController', 1, 1646027014967,
        'METHOD'),
       (27, 2, 'com.example.demo.MyController.additional()', 0, 1, 'com.example.demo.MyController', 1, null, 'METHOD'),
       (28, 2, 'com.example.demo.MyController.getMyService()', 1, 0, 'com.example.demo.MyController', 1, 1646027014967,
        'METHOD'),
       (29, 2, 'com.example.demo.MyController', 2, 1, 'com.example.demo', 1, 1646027014967, 'CLASS'),
       (30, 2, 'com.example.demo', 2, 3, '', 1, 1646027014967, 'PACKAGE');
