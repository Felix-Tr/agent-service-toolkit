package de.trafficvalidator.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ValidationApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String TEST_INTERSECTION_ID = "644";

    @Test
    public void testGetIntersectionSummary() {
        String url = "http://localhost:" + port + "/api/validate/" + TEST_INTERSECTION_ID + "/summary";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(TEST_INTERSECTION_ID, body.get("id"));

        // Check only that summary exists and has expected structure
        Map<String, Object> summary = (Map<String, Object>) body.get("summary");
        assertNotNull(summary);
        assertTrue(summary.containsKey("laneCount"));
        assertTrue(summary.containsKey("connectionCount"));
    }

    @Test
    public void testValidateIntersection() {
        String url = "http://localhost:" + port + "/api/validate/" + TEST_INTERSECTION_ID;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = response.getBody();
        assertEquals(TEST_INTERSECTION_ID, body.get("id"));
        assertEquals("cyclist-arrow", body.get("ruleset"));

        // Check that results exist with expected structure
        assertNotNull(body.get("results"));
    }

    @Test
    public void testInvalidIntersectionId() {
        String url = "http://localhost:" + port + "/api/validate/non-existent";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = response.getBody();
        assertNotNull(body.get("error"));
    }
}