package com.wiftwift.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MakeBucketArgs;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;

@Configuration
public class MinioConfig {
    
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket}")
    private String bucket;
    
    @Bean
    public MinioClient minioClient() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials("minioadmin", "minioadmin")
                .build();
        
        boolean bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!bucketExists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        
        return client;
    }
    
    @Bean
    public String minioBucket() {
        return bucket;
    }
}
