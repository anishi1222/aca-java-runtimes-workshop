// tag::adocHeader[]
package io.containerapps.javaruntime.workshop.micronaut;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;

@MicronautTest
class MicronautResourceTest {

  private static final String BASE_PATH = "/micronaut";
// end::adocHeader[]

// tag::adocTestHello[]
    @Test
    public void testHelloEndpoint(RequestSpecification spec) {
        spec
          .when().get(BASE_PATH)
          .then()
            .statusCode(200)
            .body(is("Micronaut: hello"));
    }
// end::adocTestHello[]

    @Test
    public void testCpuEndpoint(RequestSpecification spec) {
        spec.param("iterations", 1)
          .when().get(BASE_PATH + "/cpu")
          .then()
            .statusCode(200)
            .body(startsWith("Micronaut: CPU consumption is done with"))
            .body(endsWith("nano-seconds."));
    }

    @Test
    public void testCpuWithDBEndpoint(RequestSpecification spec) {
        spec.param("iterations", 1).param("db", true)
          .when().get(BASE_PATH + "/cpu")
          .then()
            .statusCode(200)
            .body(startsWith("Micronaut: CPU consumption is done with"))
            .body(endsWith("The result is persisted in the database."));
    }

// tag::adocTestCPU[]
    @Test
    public void testCpuWithDBAndDescEndpoint(RequestSpecification spec) {
        spec.param("iterations", 1).param("db", true).param("desc", "Java25")
          .when().get(BASE_PATH + "/cpu")
          .then()
            .statusCode(200)
            .body(startsWith("Micronaut: CPU consumption is done with"))
            .body(not(containsString("Java25")))
            .body(endsWith("The result is persisted in the database."));
    }
// end::adocTestCPU[]

    @Test
    public void testMemoryEndpoint(RequestSpecification spec) {
        spec.param("bites", 1)
          .when().get(BASE_PATH + "/memory")
          .then()
            .statusCode(200)
            .body(startsWith("Micronaut: Memory consumption is done with"))
            .body(endsWith("nano-seconds."));
    }

    @Test
    public void testMemoryWithDBEndpoint(RequestSpecification spec) {
        spec.param("bites", 1).param("db", true)
          .when().get(BASE_PATH + "/memory")
          .then()
            .statusCode(200)
            .body(startsWith("Micronaut: Memory consumption is done with"))
            .body(endsWith("The result is persisted in the database."));
    }

// tag::adocTestMemory[]
    @Test
    public void testMemoryWithDBAndDescEndpoint(RequestSpecification spec) {
        spec.param("bites", 1).param("db", true).param("desc", "Java25")
          .when().get(BASE_PATH + "/memory")
          .then()
            .statusCode(200)
            .body(startsWith("Micronaut: Memory consumption is done with"))
            .body(not(containsString("Java25")))
            .body(endsWith("The result is persisted in the database."));
    }
// end::adocTestMemory[]

// tag::adocTestStats[]
    @Test
    public void testStats(RequestSpecification spec) {
      spec
            .when().get(BASE_PATH + "/stats")
            .then()
            .statusCode(200);
    }
// end::adocTestStats[]
}
