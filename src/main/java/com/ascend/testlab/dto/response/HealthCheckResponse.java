package com.ascend.testlab.dto.response;

public record HealthCheckResponse(
    Boolean isPgReaderUp, Boolean isAerospikeup, Boolean isUnderMaintenance) {}
