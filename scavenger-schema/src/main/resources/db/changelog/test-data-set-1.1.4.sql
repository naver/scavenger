--liquibase formatted sql

--changeset scavenger:8

INSERT INTO call_stacks (id, customerid, applicationid, environmentid, signaturehash, callersignaturehash, invokedatmillis, createdat)
    VALUES (1, 1, 2, 1, '1b3fdd577d38e12aca87e111a65c5c47', 'c48c30b4fd1deff3bfd2deaebf9b9a8c', 1743148320000, '2025-03-28 16:23:25.998541'),
           (2, 1, 2, 1, 'd91cd856cd539a7043908484ad57071e', 'd58f664b5f55a7e39f1967f2b6320750', 1743148200000, '2025-03-28 16:23:25.998699'),
           (3, 1, 2, 1, 'fd6ff5ca18bf9907470d4d406108d016', '1b3fdd577d38e12aca87e111a65c5c47', 1743148320000, '2025-03-28 16:23:25.998752'),
           (4, 1, 2, 1, '1b3fdd577d38e12aca87e111a65c5c47', 'aa86ba4e330eb54aa890566e54ced5c2', 1743146580000, '2025-03-28 16:23:46.007919'),
           (5, 1, 2, 1, 'c6ca6a0e5342c08dbd7617617b156e9b', 'e776e2183ba68bcfc4ab748faf687d94', 1743146580000, '2025-03-28 16:23:46.000000');
