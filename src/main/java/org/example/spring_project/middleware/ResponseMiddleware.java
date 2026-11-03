package org.example.spring_project.middleware;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseMiddleware {

    public static ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        if (data != null) {
            body.put("data", data);
        }
        return ResponseEntity.ok(body);
    }

    public static ResponseEntity<Map<String, Object>> successMessage(String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", message);
        if (data != null) {
            body.put("data", data);
        }
        return ResponseEntity.ok(body);
    }

    public static ResponseEntity<Map<String, Object>> created(String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", message);
        if (data != null) {
            body.put("data", data);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    public static ResponseEntity<Map<String, Object>> error(HttpStatus status, String error) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error", error);
        return ResponseEntity.status(status).body(body);
    }
}
