# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: com/navercorp/scavenger/model/CodeBasePublication.proto
"""Generated protocol buffer code."""
from google.protobuf.internal import builder as _builder
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from scavenger.model import CommonPublicationData_pb2 as com_dot_navercorp_dot_scavenger_dot_model_dot_CommonPublicationData__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n7com/navercorp/scavenger/model/CodeBasePublication.proto\x1a\x39\x63om/navercorp/scavenger/model/CommonPublicationData.proto\"\xb5\x02\n\x13\x43odeBasePublication\x12+\n\x0b\x63ommon_data\x18\x01 \x01(\x0b\x32\x16.CommonPublicationData\x12\x31\n\x05\x65ntry\x18\x02 \x03(\x0b\x32\".CodeBasePublication.CodeBaseEntry\x1a\xbd\x01\n\rCodeBaseEntry\x12\x16\n\x0e\x64\x65\x63laring_type\x18\x01 \x01(\t\x12\x12\n\nvisibility\x18\x02 \x01(\t\x12\x11\n\tsignature\x18\x03 \x01(\t\x12\x13\n\x0bmethod_name\x18\x04 \x01(\t\x12\x11\n\tmodifiers\x18\x05 \x01(\t\x12\x14\n\x0cpackage_name\x18\x06 \x01(\t\x12\x17\n\x0fparameter_types\x18\x07 \x01(\t\x12\x16\n\x0esignature_hash\x18\x08 \x01(\tB!\n\x1d\x63om.navercorp.scavenger.modelP\x01\x62\x06proto3')

_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, globals())
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'com.navercorp.scavenger.model.CodeBasePublication_pb2', globals())
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\035com.navercorp.scavenger.modelP\001'
  _CODEBASEPUBLICATION._serialized_start=119
  _CODEBASEPUBLICATION._serialized_end=428
  _CODEBASEPUBLICATION_CODEBASEENTRY._serialized_start=239
  _CODEBASEPUBLICATION_CODEBASEENTRY._serialized_end=428
# @@protoc_insertion_point(module_scope)
