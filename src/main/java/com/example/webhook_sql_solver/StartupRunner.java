package com.example.webhook_sql_solver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Generate Webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        WebhookRequest request = new WebhookRequest("John Doe", "REG12347", "john@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<WebhookResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, WebhookResponse.class);

        WebhookResponse webhookResponse = response.getBody();

        if (webhookResponse != null) {
            String webhookUrl = webhookResponse.getWebhook();
            String accessToken = webhookResponse.getAccessToken();

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            String sqlSolution = "SELECT COUNT(*) FROM EMPLOYEE;";

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.setBearerAuth(accessToken);

            String answerJson = "{ \"sqlQuery\": \"" + sqlSolution + "\" }";
            HttpEntity<String> answerEntity = new HttpEntity<>(answerJson, authHeaders);

            ResponseEntity<String> submitResponse =
                    restTemplate.exchange(webhookUrl, HttpMethod.POST, answerEntity, String.class);

            System.out.println("First Submission Response: " + submitResponse.getBody());

            String finalSqlQuery =
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

            String finalUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
            String finalBody = "{ \"finalQuery\": \"" + finalSqlQuery.replace("\"", "\\\"") + "\" }";

            HttpEntity<String> finalEntity = new HttpEntity<>(finalBody, authHeaders);
            ResponseEntity<String> finalResponse =
                    restTemplate.exchange(finalUrl, HttpMethod.POST, finalEntity, String.class);

            System.out.println("Final Submission Response: " + finalResponse.getBody());
        }
    }
}
