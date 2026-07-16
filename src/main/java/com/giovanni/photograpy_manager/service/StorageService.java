package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.config.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    private final StorageProperties properties;
    private S3Client s3Client;

    public StorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        try {
            this.s3Client = S3Client.builder()
                    .endpointOverride(URI.create(properties.getEndpoint()))
                    .region(Region.of(properties.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                    ))
                    .forcePathStyle(true) // Required for MinIO
                    .build();

            // Vérifier/créer le bucket
            ensureBucketExists();
            log.info("✅ Connexion S3/MinIO initialisée — bucket: {}", properties.getBucket());
        } catch (Exception e) {
            log.warn("⚠️ Impossible de se connecter à S3/MinIO: {}. Le stockage fichiers sera indisponible.", e.getMessage());
        }
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.getBucket()).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(properties.getBucket()).build());
            log.info("📦 Bucket créé: {}", properties.getBucket());
        }
    }

    /**
     * Upload un fichier vers S3/MinIO.
     * @return la clé S3 du fichier uploadé
     */
    public String upload(MultipartFile file, String folder) throws IOException {
        String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        log.info("📤 Fichier uploadé: {}", key);
        return key;
    }

    /**
     * Télécharge un fichier depuis S3/MinIO.
     */
    public byte[] download(String key) {
        return s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(key)
                        .build()
        ).asByteArray();
    }

    /**
     * Supprime un fichier de S3/MinIO.
     */
    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(key)
                        .build()
        );
        log.info("🗑️ Fichier supprimé: {}", key);
    }
}
