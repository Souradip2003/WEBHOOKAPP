package com.example.WEBHOOKAPP.sqlsolution;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WebhookService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WebhookService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void executeFlow() {
        try {
            // Step 1: Generate Webhook
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", "John Doe");
            requestBody.put("regNo", "REG12346"); // <-- last 2 digits even
            requestBody.put("email", "john@example.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    generateUrl, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String webhookUrl = root.get("webhook").asText();
            String accessToken = root.get("accessToken").asText();

            // Step 2: SQL solution (Question 2)
            String finalQuery =
                    "SELECT e1.EMP_ID, " +
                    "e1.FIRST_NAME, " +
                    "e1.LAST_NAME, " +
                    "d.DEPARTMENT_NAME, " +
                    "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                    "FROM EMPLOYEE e1 " +
                    "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                    "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT " +
                    "AND e2.DOB > e1.DOB " +
                    "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                    "ORDER BY e1.EMP_ID DESC;";

            // Step 3: Submit answer
            Map<String, Object> answer = new HashMap<>();
            answer.put("finalQuery", finalQuery);

            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            headers2.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity2 = new HttpEntity<>(answer, headers2);

            ResponseEntity<String> submitResponse = restTemplate.exchange(
                    webhookUrl, HttpMethod.POST, entity2, String.class);

            System.out.println("Submission Response: " + submitResponse.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
