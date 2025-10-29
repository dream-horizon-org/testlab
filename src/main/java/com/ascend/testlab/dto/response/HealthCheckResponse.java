package com.ascend.testlab.dto.response;

public record HealthCheckResponse(
    Boolean isMySQLReaderUp, Boolean isAerospikeup, Boolean isUnderMaintenance) {}
