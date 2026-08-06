--liquibase formatted sql

--changeset scavenger:9
CREATE INDEX ix_invocations_cust_sighash ON invocations (customerId, signatureHash);
CREATE INDEX ix_methods_cust_decltype ON methods (customerId, declaringType);
CREATE INDEX ix_call_stacks_cust_sighash ON call_stacks (customerId, signatureHash);
--rollback DROP INDEX ix_invocations_cust_sighash ON invocations;
--rollback DROP INDEX ix_methods_cust_decltype ON methods;
--rollback DROP INDEX ix_call_stacks_cust_sighash ON call_stacks;
