package com.ibm.watsonwork;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watsonwork.client.*;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class WatsonWorkConfiguration {

    @Autowired
    private WatsonWorkProperties watsonWorkProperties;

    @Autowired
    private BreweryDbProperties breweryDbProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean(name = "watsonwork")
    @Primary
    public Retrofit retrofit(OkHttpClient client) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(watsonWorkProperties.getApiUri())
                .client(client)
                .build();
    }

    @Bean(name = "brewerydb")
    public Retrofit retrofitBreweryDB(OkHttpClient client) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(breweryDbProperties.getApiUri())
                .client(client)
                .build();
    }

    @Bean
    public GraphQLClient graphQLClient(@Qualifier(value = "watsonwork") Retrofit retrofit) {
        return retrofit.create(GraphQLClient.class);
    }

    @Bean
    public BreweryDBClient breweryDBClient(@Qualifier(value = "brewerydb") Retrofit retrofit) {
        return retrofit.create(BreweryDBClient.class);
    }


    @Bean
    public AuthClient authClient(Retrofit retrofit) {
        return retrofit.create(AuthClient.class);
    }


}
