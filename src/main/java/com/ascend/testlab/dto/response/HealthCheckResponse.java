package com.ascend.testlab.dto.response;

/**
 * Record encapsulating the health check response for the health check endpoint.
 *
 * @param isPgReaderUp Indicates if the PostgreSQL reader is up.
 * @param isAerospikeUp Indicates if Aerospike is up.
 * @param isUnderMaintenance Indicates if the system is under maintenance.
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see com.ascend.testlab.rest.HealthCheck
 */
public record HealthCheckResponse(
    Boolean isPgReaderUp, Boolean isAerospikeUp, Boolean isUnderMaintenance) {}
