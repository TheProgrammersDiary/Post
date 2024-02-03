package com.evalvis.post;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

@Service
public class MinioStorage implements ContentStorage {
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public void upload(String objectId, int version, String content) {
        try {
            InputStream stream = new ByteArrayInputStream(content.getBytes());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId + "_v" + version)
                    .stream(stream, stream.available(), -1)
                    .build());
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to upload data to minio.");
        }
    }

    @Override
    public String download(String objectId, int version) {
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId + "_v" + version)
                    .build());
            return new BufferedReader(new InputStreamReader(stream))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to download data from minio.");
        }
    }
}