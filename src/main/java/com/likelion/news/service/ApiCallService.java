package com.likelion.news.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.news.exception.InternalServerException;
import lombok.*;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiCallService {


    private final ObjectMapper objectMapper;

    public<T> ApiServiceResponse<T> callApi(ApiServiceRequest req, Class<T> responseBodyMappingClass) {
        try {
            return callApiProcess(req, responseBodyMappingClass);
        }catch (Exception e){
            throw new InternalServerException("API를 호출하는 도중 오류가 있습니다.", e);
        }
    }

    private <T> ApiServiceResponse<T> callApiProcess(ApiServiceRequest req, Class<T> responseBodyMappingClass) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(req.getUrl());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(req.getRequestType().name());

        // 헤더 추가
        appendHeader(req.getHeaders(), conn);

        // request body 추가
        if (req.getBody() != null) {
            sendRequestBody(req.getBody(), conn);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        T resultBody = objectMapper.readValue(result.toString(), responseBodyMappingClass);

        ApiServiceResponse<T> resp = ApiServiceResponse.<T>builder()
                .body(resultBody)
                .statusCode(conn.getResponseCode())
                .build();

        return resp;
    }

    private void sendRequestBody(Map<String, String>  reqBody, HttpURLConnection conn) throws IOException {
        String requestBody = new JSONObject(reqBody).toString();
        conn.setDoOutput(true); // Enable writing to the connection's output stream
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }catch (Exception e){
            throw new InternalServerException("API 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    private void appendHeader(Map<String, String> headers, HttpURLConnection conn) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ApiServiceRequest{
        private RequestType requestType;
        private Map<String, String> headers;
        private String url;
        private Map<String, String> body;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ApiServiceResponse<T>{
        private Integer statusCode;
        private T body;
    }


    public static enum RequestType{
        GET, POST, PUT, PATCH, DELETE
    }
}

