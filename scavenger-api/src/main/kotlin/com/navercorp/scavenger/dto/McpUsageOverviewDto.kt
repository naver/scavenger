package com.navercorp.scavenger.dto

data class McpUsageOverviewDto(
    val customerName: String,
    val summary: SummaryDto,
    val snapshotCount: Int,
    val applications: List<ApplicationDetailDto>,
    val environments: List<EnvironmentDetailDto>
)
