package com.ascend.testlab.dto.response;

/**
 * Record encapsulating the health check response for the health check endpoint.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see com.ascend.testlab.rest.HealthCheck
 */
public record HealthCheckResponse(
    /** Whether the MySQL reader is up. */
    Boolean isMySQLReaderUp,
    /** Whether the Aerospike is up. */
    Boolean isAerospikeUp,
    /** Whether the service is under maintenance. */
    Boolean isUnderMaintenance) {}
