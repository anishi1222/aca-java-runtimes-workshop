// tag::adocHeader[]
package io.containerapps.javaruntime.workshop.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = {
    "server.port=8803",
    "spring.datasource.url=jdbc:tc:postgresql:14-alpine://testcontainers/postgres",
    "spring.datasource.username=postgres",
    "spring.datasource.password=password"
})
class SpringbootResourceTest {

    private static String basePath = "http://localhost:8803/springboot";

    private final RestClient restClient = RestClient.create();
// end::adocHeader[]

// tag::adocTestHello[]
    @Test
    public void testHelloEndpoint() {
        ResponseEntity<String> response = getForEntity(basePath);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertThat(response.getBody()).contains("Spring Boot: hello");
    }
// end::adocTestHello[]

    @Test
    public void testCpuEndpoint() {
        ResponseEntity<String> response = getForEntity(basePath + "/cpu?iterations=1");

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertThat(response.getBody())
            .startsWith("Spring Boot: CPU consumption is done with")
            .endsWith("nano-seconds.");
    }

    @Test
    public void testCpuWithDBEndpoint() {
        ResponseEntity<String> response = getForEntity(basePath + "/cpu?iterations=1&db=true");

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertThat(response.getBody())
            .startsWith("Spring Boot: CPU consumption is done with")
            .endsWith("The result is persisted in the database.");
    }

// tag::adocTestCPU[]
    @Test
    public void testCpuWithDBAndDescEndpoint() {
        ResponseEntity<String> response = getForEntity(basePath + "/cpu?iterations=1&db=true&desc=Java25");

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertThat(response.getBody())
            .startsWith("Spring Boot: CPU consumption is done with")
            .doesNotContain("Java25")
            .endsWith("The result is persisted in the database.");
    }
// end::adocTestCPU[]

    @Test
    public void testMemoryEndpoint() {
        ResponseEntity<String> response = getForEntity(basePath + "/memory?bites=1");

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertThat(response.getBody())
            .startsWith("Spring Boot: Memory consumption is done with")
            .endsWith("nano-seconds.");
    }

    @Test
    public void testMemoryWithDBEndpoint() {
        ResponseEntity<String> response = getForEntity(basePath + "/memory?bites=1&db=true");

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertThat(response.getBody())
            .startsWith("Spring Boot: Memory consumption is done with")
            .endsWith("The result is persisted in the database.");
    }

// tag::adocTestMemory[]
    @Test
    public void testMemoryWithDBAndDescEndpoint() {
        ResponseEntity<String> response = getForEntity(basePath + "/memory?bites=1&db=true&desc=Java25");

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertThat(response.getBody())
            .startsWith("Spring Boot: Memory consumption is done with")
            .doesNotContain("Java25")
            .endsWith("The result is persisted in the database.");
    }
// end::adocTestMemory[]

// tag::adocTestStats[]
    @Test
    public void testStats() {
        ResponseEntity<String> response = getForEntity(basePath + "/stats");

        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
// end::adocTestStats[]

    private ResponseEntity<String> getForEntity(String url) {
        return restClient.get().uri(url).retrieve().toEntity(String.class);
    }
}
