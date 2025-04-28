package com.example.B2F.config;

import com.backblaze.b2.client.B2AccountAuthorizerSimpleImpl;
import com.backblaze.b2.client.B2ClientConfig;
import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.B2StorageClientFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class B2Config {
    @Value("${backblaze.applicationKeyId}")
    private String keyId;

    @Value("${backblaze.applicationKey}")
    private String secretKey;

    @Value("${backblaze.bucketName}")
    private String buketName;

    @Bean
    public B2StorageClient b2StorageClient(){
        var authorizer = B2AccountAuthorizerSimpleImpl.builder(keyId, secretKey).build();
        var config = B2ClientConfig.builder(authorizer, "B2F").build();
        return B2StorageClientFactory.createDefaultFactory().create(config);
    }
}
