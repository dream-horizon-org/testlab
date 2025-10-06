package com.hulk.testlab.dto.response;

public record HealthCheckResponse(
        Boolean isMySQLReaderUp,
                Boolean isAerospikeup,
            Boolean isUnderMaintenance
) {}
