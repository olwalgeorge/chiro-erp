package org.chiro.core_business_service.integration

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test

/**
 * Integration tests for Core Business Service
 */
@QuarkusTest
class CoreBusinessServiceIntegrationTest {
    
    @Test
    fun `should return health status`() {
        given()
            .`when`().get("/api/v1/health/status")
            .then()
            .statusCode(200)
            .body("success", `is`(true))
            .body("data.status", `is`("UP"))
            .body("data.version", notNullValue())
            .body("data.dependencies", notNullValue())
    }
    
    @Test
    fun `should return service info`() {
        given()
            .`when`().get("/api/v1/health/info")
            .then()
            .statusCode(200)
            .body("success", `is`(true))
            .body("data.name", `is`("Core Business Service"))
            .body("data.version", notNullValue())
            .body("data.modules", notNullValue())
            .body("data.features", notNullValue())
    }
    
    @Test
    fun `should return liveness probe`() {
        given()
            .`when`().get("/q/health/live")
            .then()
            .statusCode(200)
            .body("status", `is`("UP"))
    }
    
    @Test
    fun `should return readiness probe`() {
        given()
            .`when`().get("/q/health/ready")
            .then()
            .statusCode(200)
            .body("status", `is`("UP"))
    }
    
    @Test
    fun `should return openapi specification`() {
        given()
            .`when`().get("/q/openapi")
            .then()
            .statusCode(200)
    }
}
