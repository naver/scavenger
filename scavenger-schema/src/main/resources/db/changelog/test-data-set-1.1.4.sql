--liquibase formatted sql

--changeset scavenger:8

INSERT INTO call_stacks (id, customerid, applicationid, environmentid, signaturehash, callersignaturehash, invokedatmillis, createdat)
VALUES (1, 1, 2, 1, '1b3fdd577d38e12aca87e111a65c5c47', 'c48c30b4fd1deff3bfd2deaebf9b9a8c', 1743148320000, '2025-03-28 16:23:25.998541'),
       (2, 1, 2, 1, 'd91cd856cd539a7043908484ad57071e', 'd58f664b5f55a7e39f1967f2b6320750', 1743148200000, '2025-03-28 16:23:25.998699'),
       (3, 1, 2, 1, 'fd6ff5ca18bf9907470d4d406108d016', '1b3fdd577d38e12aca87e111a65c5c47', 1743148320000, '2025-03-28 16:23:25.998752'),
       (4, 1, 2, 1, '1b3fdd577d38e12aca87e111a65c5c47', 'aa86ba4e330eb54aa890566e54ced5c2', 1743146580000, '2025-03-28 16:23:46.007919'),
       (5, 1, 2, 1, 'c6ca6a0e5342c08dbd7617617b156e9b', 'e776e2183ba68bcfc4ab748faf687d94', 1743146580000, '2025-03-28 16:23:46.000000');

INSERT INTO snapshots (id, createdat, customerid, name, filterinvokedatmillis, packages, status, excludeabstract)
VALUES (4, '2025-04-02 12:21:03.571094', 1, 'demo4', 0, '', null, null);

INSERT INTO snapshot_application (snapshotid, applicationid, customerid)
VALUES (4, 2, 1);

INSERT INTO snapshot_environment (snapshotid, environmentid, customerid)
VALUES (4, 1, 1);

INSERT INTO snapshot_nodes (snapshotid, signature, usedcount, unusedcount, parent, customerid, lastinvokedatmillis, type)
VALUES (4, 'com.example.demo.extmodel.MyExtensionModel.getId()', 0, 1, 'com.example.demo.extmodel.MyExtensionModel', 1, null, 'METHOD'),
       (4, 'com.example.demo.extmodel.MyExtensionModel.getName()', 0, 1, 'com.example.demo.extmodel.MyExtensionModel', 1, null, 'METHOD'),
       (4, 'com.example.demo.extmodel.MyExtensionModel.copy(long,java.lang.String)', 0, 1, 'com.example.demo.extmodel.MyExtensionModel', 1, null,
        'METHOD'),
       (4, 'com.example.demo.extmodel.MyExtensionModel.component2()', 0, 1, 'com.example.demo.extmodel.MyExtensionModel', 1, null, 'METHOD'),
       (4, 'com.example.demo.extmodel.MyExtensionModel.component1()', 0, 1, 'com.example.demo.extmodel.MyExtensionModel', 1, null, 'METHOD'),
       (4, 'com.example.demo.extmodel.MyExtensionModel.buildModels()', 1, 0, 'com.example.demo.extmodel.MyExtensionModel', 1, 1700624040000,
        'METHOD'),
       (3,
        'com.example.demo.extmodel.MyExtensionModel.copy$default(com.example.demo.extmodel.MyExtensionModel,long,java.lang.String,int,java.lang.Object)',
        0, 1, 'com.example.demo.extmodel.MyExtensionModel.copy', 1, null, 'METHOD'),
       (4, 'com.example.demo.extmodel.MyExtensionModel.copy', 0, 1, 'com.example.demo.extmodel.MyExtensionModel', 1, null, 'CLASS'),
       (4, 'com.example.demo.extmodel.MyExtensionModel.equals(java.lang.Object)', 0, 1, 'com.example.demo.extmodel.MyExtensionModel', 1, null,
        'METHOD'),
       (4, 'com.example.demo.extmodel.MyExtensionModel.hashCode()', 0, 1, 'com.example.demo.extmodel.MyExtensionModel', 1, null, 'METHOD'),
       (4, 'com.example.demo.extmodel.MyExtensionModel', 1, 8, 'com.example.demo.extmodel', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.extmodel', 1, 8, 'com.example.demo', 1, 1700624040000, 'PACKAGE'),
       (4, 'com.example.demo.BenchmarkController.getBenchmarkService()', 0, 1, 'com.example.demo.BenchmarkController', 1, null, 'METHOD'),
       (4, 'com.example.demo.BenchmarkController.benchmark()', 0, 1, 'com.example.demo.BenchmarkController', 1, null, 'METHOD'),
       (4, 'com.example.demo.BenchmarkController', 0, 2, 'com.example.demo', 1, null, 'CLASS'),
       (4, 'com.example.demo.controller.BaseController.base()', 0, 1, 'com.example.demo.controller.BaseController', 1, null, 'METHOD'),
       (4, 'com.example.demo.controller.BaseController', 0, 1, 'com.example.demo.controller', 1, null, 'CLASS'),
       (4, 'com.example.demo.controller.FeignController.getLocalApiClient()', 0, 1, 'com.example.demo.controller.FeignController', 1, null,
        'METHOD'),
       (4, 'com.example.demo.controller.FeignController.testFeign()', 0, 1, 'com.example.demo.controller.FeignController', 1, null, 'METHOD'),
       (4, 'com.example.demo.controller.FeignController.runFeignRun()', 0, 1, 'com.example.demo.controller.FeignController', 1, null, 'METHOD'),
       (4, 'com.example.demo.controller.FeignController', 0, 3, 'com.example.demo.controller', 1, null, 'CLASS'),
       (4, 'com.example.demo.controller.ExtensionController.runExtension()', 0, 1, 'com.example.demo.controller.ExtensionController', 1, null,
        'METHOD'),
       (4, 'com.example.demo.controller.ExtensionController', 0, 1, 'com.example.demo.controller', 1, null, 'CLASS'),
       (4, 'com.example.demo.controller.MyController$MyTest.nesting()', 1, 0, 'com.example.demo.controller.MyController$MyTest', 1, 1700624040000,
        'METHOD'),
       (4, 'com.example.demo.controller.MyController$MyTest', 1, 0, 'com.example.demo.controller.MyController', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.controller.MyController.hello()', 1, 0, 'com.example.demo.controller.MyController', 1, 1700624040000, 'METHOD'),
       (4, 'com.example.demo.controller.MyController.additional()', 1, 0, 'com.example.demo.controller.MyController', 1, 1700624040000, 'METHOD'),
       (4, 'com.example.demo.controller.MyController.async()', 0, 1, 'com.example.demo.controller.MyController', 1, null, 'METHOD'),
       (4, 'com.example.demo.controller.MyController', 3, 1, 'com.example.demo.controller', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.controller', 3, 6, 'com.example.demo', 1, 1700624040000, 'PACKAGE'),
       (4, 'com.example.demo.service.BridgeService.getAa()', 1, 0, 'com.example.demo.service.BridgeService', 1, 1700624040000, 'METHOD'),
       (4, 'com.example.demo.service.BridgeService.doSth()', 1, 0, 'com.example.demo.service.BridgeService', 1, 1700624040000, 'METHOD'),
       (4, 'com.example.demo.service.BridgeService', 2, 0, 'com.example.demo.service', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.service.CglibProxyService.aspect()', 1, 0, 'com.example.demo.service.CglibProxyService', 1, 1700624040000, 'METHOD'),
       (4, 'com.example.demo.service.CglibProxyService', 1, 0, 'com.example.demo.service', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.service.DynamicProxyServiceImpl.aspect()', 1, 0, 'com.example.demo.service.DynamicProxyServiceImpl', 1,
        1700624040000, 'METHOD'),
       (4, 'com.example.demo.service.DynamicProxyServiceImpl', 1, 0, 'com.example.demo.service', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.service.MyParentService.test()', 1, 0, 'com.example.demo.service.MyParentService', 1, 1700624040000, 'METHOD'),
       (4, 'com.example.demo.service.MyParentService', 1, 0, 'com.example.demo.service', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.service.AsyncService.asyncJob()', 0, 1, 'com.example.demo.service.AsyncService', 1, null, 'METHOD'),
       (4, 'com.example.demo.service.AsyncService', 0, 1, 'com.example.demo.service', 1, null, 'CLASS'),
       (4, 'com.example.demo.service', 5, 1, 'com.example.demo', 1, 1700624040000, 'PACKAGE'),
       (4, 'com.example.demo.additional.AdditionalService$WOW.doSth()', 1, 0, 'com.example.demo.additional.AdditionalService$WOW', 1,
        1700624040000, 'METHOD'),
       (4, 'com.example.demo.additional.AdditionalService$WOW', 1, 0, 'com.example.demo.additional.AdditionalService', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.additional.AdditionalService.get()', 1, 0, 'com.example.demo.additional.AdditionalService', 1, 1700624040000,
        'METHOD'),
       (4, 'com.example.demo.additional.AdditionalService', 2, 0, 'com.example.demo.additional', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.additional', 2, 0, 'com.example.demo', 1, 1700624040000, 'PACKAGE'),
       (4, 'com.example.demo.service2.TestService.call()', 1, 0, 'com.example.demo.service2.TestService', 1, 1700624040000, 'METHOD'),
       (4, 'com.example.demo.service2.TestService', 1, 0, 'com.example.demo.service2', 1, 1700624040000, 'CLASS'),
       (4, 'com.example.demo.service2', 1, 0, 'com.example.demo', 1, 1700624040000, 'PACKAGE'),
       (4, 'com.example.demo.BenchmarkService.doNothing(int)', 0, 1, 'com.example.demo.BenchmarkService', 1, null, 'METHOD'),
       (4, 'com.example.demo.BenchmarkService', 0, 1, 'com.example.demo', 1, null, 'CLASS'),
       (4, 'com.example.demo', 12, 18, '', 1, 1700624040000, 'PACKAGE');
