package org.example.lambda.handler;

import org.example.lambda.model.Record;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final DynamoDbClient dynamo;
    private final String tableName;

    public PostHandler() {
        // Use default region resolver (from lambda environment) or set explicitly
        this.dynamo = DynamoDbClient.create();
        this.tableName = System.getenv("TABLE_NAME");
        if (this.tableName == null) {
            throw new RuntimeException("Environment variable TABLE_NAME not set");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            String body = event.getBody();
            if (body == null || body.isBlank()) {
                return generateResponse(400, "{\"error\":\"Empty request body\"}");
            }

            // Parse to model
            Record record = mapper.readValue(body, Record.class);

            // Assign server-side ID if not provided
            if (record.getId() == null || record.getId().isBlank()) {
                record.setId(UUID.randomUUID().toString());
            }

            // Build item
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(record.getId()).build());
            if (record.getName() != null) item.put("name", AttributeValue.builder().s(record.getName()).build());
            if (record.getEmail() != null) item.put("email", AttributeValue.builder().s(record.getEmail()).build());
            if (record.getMessage() != null) item.put("message", AttributeValue.builder().s(record.getMessage()).build());

            PutItemRequest putReq = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamo.putItem(putReq);

            // Build success response
            Map<String, String> resp = Map.of("id", record.getId(), "status", "saved");
            String respJson = mapper.writeValueAsString(resp);
            return generateResponse(200, respJson);

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            try {
                String err = mapper.writeValueAsString(Map.of("error", e.getMessage()));
                return generateResponse(500, err);
            } catch (Exception inner) {
                return generateResponse(500, "{\"error\":\"unknown\"}");
            }
        }
    }

    private APIGatewayProxyResponseEvent generateResponse(int status, String body) {
        APIGatewayProxyResponseEvent resp = new APIGatewayProxyResponseEvent();
        resp.setStatusCode(status);
        resp.setHeaders(Map.of("Content-Type", "application/json"));
        resp.setBody(body);
        return resp;
    }
}