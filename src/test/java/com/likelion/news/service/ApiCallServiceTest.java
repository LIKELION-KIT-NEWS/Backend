package com.likelion.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


@Import(ApiCallServiceTest.TestConfig.class)
@ExtendWith(SpringExtension.class)


public class ApiCallServiceTest {

    @Autowired
    private ApiCallService apiCallService;
    private ClientAndServer mockServer;
    @BeforeEach
    void setUp() {
        mockServer = ClientAndServer.startClientAndServer(8888);
        new MockServerClient("localhost", 8888)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/test")
                                .withHeader(Header.header("test", "test-header"))
                                .withBody("{\"test1\":\"1\",\"test2\":\"2\"}")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(new Header("Content-Type", "text/xml;charset=utf-8"))
                                .withBody("{ \"test\" : \"good\"}")
                );
    }

    @Test
    @DisplayName("ApiCallService를 주입받고 테스트할 수 있다.")
    void t1() throws Exception {
        //given
        ObjectMapper objectMapper = (ObjectMapper)ReflectionTestUtils.getField(apiCallService, "objectMapper");

        //then
        assertThat(apiCallService).isNotNull();
        assertThat(objectMapper).isNotNull();
    }

    @Test
    @DisplayName("Conn객체에 HTTP Header를 주입할 수 있다.")
    void t2() throws Exception {
        //given
        Map<String, String> testHeader = new HashMap<>();

        testHeader.put("test", "1");
        testHeader.put("test2", "2");

        URL url = new URL("http://localhost:8888/test");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //when
        ReflectionTestUtils.invokeMethod(apiCallService, "appendHeader", testHeader, conn);
        //then
        assertThat(conn.getRequestProperty("test")).isEqualTo("1");
        assertThat(conn.getRequestProperty("test2")).isEqualTo("2");
    }


    @Test
    @DisplayName("Http Request를 전송할 수 있다.")
    void t3() throws Exception {
        //given


        Map<String, String> testHeader = new HashMap<>();
        Map<String, String> testBody = new HashMap<>();

        testHeader.put("test", "test-header");

        testBody.put("test1", "1");
        testBody.put("test2", "2");


        ApiCallService.ApiServiceRequest req = ApiCallService.ApiServiceRequest.builder()
                .url("http://localhost:8888/test")
                .headers(testHeader)
                .requestType(ApiCallService.RequestType.POST)
                .body(testBody)
                .build();


        //when
        ApiCallService.ApiServiceResponse<TestResponse> resp = apiCallService.callApi(req, TestResponse.class);

        //then

        assertThat(resp.getStatusCode()).isEqualTo(200);
        assertThat(resp.getBody().getTest()).isEqualTo("good");
    }

    public static class TestResponse{
        private String test;

        public String getTest() {
            return test;
        }
    }
    @TestConfiguration
    public static class TestConfig{


        @Bean
        public ApiCallService apiCallService(){
            return new ApiCallService(objectMapper());
        }

        @Bean
        public ObjectMapper objectMapper(){
            return new ObjectMapper();
        }
    }

}

