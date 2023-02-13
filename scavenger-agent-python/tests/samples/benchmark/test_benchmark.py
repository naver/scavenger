from scavenger.internal.invocation_registry import InvocationRegistry
from scavenger.internal.patch import Patcher

invocation_registry = InvocationRegistry()
Patcher(["patch"], [], [], True, invocation_registry).patch()

import patch
import not_patch


def test_benchmark_patch_sum_10_thousand(benchmark):
    benchmark(patch.sum_10_thousand)


def test_benchmark_not_patch_sum_10_thousand(benchmark):
    benchmark(not_patch.sum_10_thousand)


def test_benchmark_patch_count(benchmark):
    benchmark(patch.count)


def test_benchmark_not_patch_count(benchmark):
    benchmark(not_patch.count)
