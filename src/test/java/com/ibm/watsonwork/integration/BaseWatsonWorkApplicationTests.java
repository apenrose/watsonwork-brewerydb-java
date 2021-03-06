package com.ibm.watsonwork.integration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watsonwork.ApplicationBootstrap;
import com.ibm.watsonwork.WatsonWorkConstants;
import com.ibm.watsonwork.WatsonWorkProperties;
import com.ibm.watsonwork.client.AuthClient;
import com.ibm.watsonwork.model.TokenResponse;
import okhttp3.OkHttpClient;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Call;
import retrofit2.mock.Calls;

import static com.ibm.watsonwork.utils.Utils.prepareSHA256Hash;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationBootstrap.class, BaseWatsonWorkApplicationTests.IntegrationConfigurationBootTest.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class BaseWatsonWorkApplicationTests {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected WatsonWorkProperties watsonWorkProperties;

    static class MockAuthService implements AuthClient {

        @Override
        public Call<TokenResponse> authenticateApp(String basicAuthorization, String grantType) {
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken("test-access-token");
            tokenResponse.setBearerType("bearer");
            tokenResponse.setExpiresIn(43199);
            tokenResponse.setId("a513f1f4-e785-4770-b501-2f50558b2323");
            tokenResponse.setJti("04dc0cd4-46e7-47d9-89eb-ba2a1637b12b");
            tokenResponse.setScope("read write");
            return Calls.response(tokenResponse);
        }

    }


    @Configuration
    public static class IntegrationConfigurationBootTest {

        @Primary
        @Bean
        public OkHttpClient okHttpClient() {
            return new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
        }

        @Primary
        @Bean
        public AuthClient authClient() {
            return new MockAuthService();
        }
    }

    public TestRestTemplate getTestRestTemplate() {
        restTemplate.getRestTemplate().setInterceptors(
                    Collections.singletonList((request, body, execution) -> {
                        String hmacSha256Hex = prepareSHA256Hash(watsonWorkProperties.getWebhookSecret(), body);
                        request.getHeaders()
                                .add(WatsonWorkConstants.X_OUTBOUND_TOKEN, hmacSha256Hex);
                        return execution.execute(request, body);
                    }));
        return restTemplate;
    }

    public void setRestTemplate(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
