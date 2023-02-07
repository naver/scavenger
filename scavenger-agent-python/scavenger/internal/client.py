import grpc
import requests

from scavenger.internal.constant import PROCESS_UUID
from scavenger.model.CodeBasePublication_pb2 import CodeBasePublication
from scavenger.model.GrpcAgentService_pb2_grpc import GrpcAgentServiceStub
from scavenger.model.InvocationDataPublication_pb2 import InvocationDataPublication
from scavenger.model.GetConfig_pb2 import GetConfigRequest, GetConfigResponse


class Client:

    def __init__(self, server_url: str, license_key: str):
        response = requests.get(f"{server_url}/javaagent/v5/initConfig?licenseKey={license_key}")
        collector_url = response.json()["collectorUrl"]

        self.channel = grpc.insecure_channel(collector_url + ":80")
        self.grpc_agent_service = GrpcAgentServiceStub(self.channel)
        self.poll_config_request = GetConfigRequest(
            jvm_uuid=PROCESS_UUID,
            api_key=license_key
        )

    def send_codebase_publication(self, codebase_publication: CodeBasePublication):
        self.grpc_agent_service.SendCodeBasePublication(codebase_publication)

    def send_invocation_data_publication(self, invocation_data_publication: InvocationDataPublication):
        self.grpc_agent_service.SendInvocationDataPublication(invocation_data_publication)

    def poll_config(self) -> GetConfigResponse:
        return self.grpc_agent_service.PollConfig(self.poll_config_request)
