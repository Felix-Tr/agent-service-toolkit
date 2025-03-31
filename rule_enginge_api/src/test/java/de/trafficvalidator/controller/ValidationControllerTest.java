package de.trafficvalidator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.trafficvalidator.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCyclistArrowValidation() throws Exception {
        // Perform the validation request
        MvcResult result = mockMvc.perform(get("/api/validate/644")
                .param("ruleset", "cyclist-arrow"))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the response
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        // Get the results map
        Map<String, Object> results = (Map<String, Object>) response.get("results");
        Map<String, Object> approaches = (Map<String, Object>) results.get("approaches");
        
        // Check connections 19 and 4 in the W approach
        List<Map<String, Object>> wApproach = (List<Map<String, Object>>) approaches.get("W");
        Map<String, Object> connection19 = findConnectionById(wApproach, 19);
        Map<String, Object> connection4 = findConnectionById(wApproach, 4);
        
        // Verify the results
        assertNotNull(connection19, "Connection 19 should be present in W approach");
        assertNotNull(connection4, "Connection 4 should be present in W approach");
        
        assertFalse((Boolean) connection19.get("valid"), "Connection 19 should be invalid");
        assertFalse((Boolean) connection4.get("valid"), "Connection 4 should be invalid");
        
        assertTrue((Boolean) connection19.get("isCyclistRightTurn"), "Connection 19 should be a cyclist right turn");
        assertTrue((Boolean) connection4.get("isCyclistRightTurn"), "Connection 4 should be a cyclist right turn");
    }

    private Map<String, Object> findConnectionById(List<Map<String, Object>> connections, int connectionId) {
        return connections.stream()
                .filter(conn -> ((Integer) conn.get("connectionId")) == connectionId)
                .findFirst()
                .orElse(null);
    }
} 