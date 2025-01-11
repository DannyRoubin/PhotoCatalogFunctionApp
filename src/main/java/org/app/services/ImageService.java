package org.app.services;

import com.azure.ai.vision.common.*;
import com.azure.ai.vision.imageanalysis.*;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import org.app.Secrets;
import org.app.models.ImageRequest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

public class ImageService {

    public static String analyzeAndStoreImage(ImageRequest imageRequest) {
        try {
            String tags = analyzeImage(imageRequest.getInputStream(), Secrets.getEndpoint(), Secrets.getKey());

            if (tags != null && !tags.isEmpty()) {
                System.out.println(String.format("Image processed successfully. PhotoID: %s, PhotoGUID: %s, Tags: %s",
                        imageRequest.getPhotoID(), imageRequest.getPhotoGUID(), tags));

                // Upload the image to Azure Blob Storage
                uploadImageToBlobStorage("photo-container", imageRequest.getPhotoGUID(), imageRequest.getInputStream());

                // Send data to Lambda
                String lambdaResult = sendDataToLambda(imageRequest.getPhotoID(), imageRequest.getPhotoGUID(), tags);

                if (lambdaResult.equalsIgnoreCase("Success")) {
                    return String.format("Image processed successfully. Tags: %s", tags);
                } else {
                    return String.format("Image processed, but sending data to Lambda failed. Tags: %s", tags);
                }
            } else {
                return "Image tagging failed. No tags with sufficient confidence.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }

    private static String analyzeImage(InputStream inputStream, String endpoint, String key) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            inputStream.transferTo(byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Debugging: Log byte array details
            System.out.println("Byte Array (First 10 Bytes): " + Arrays.toString(Arrays.copyOfRange(imageBytes, 0, 10)));

            try (ImageSourceBuffer imageSourceBuffer = new ImageSourceBuffer();
                 ImageWriter imageWriter = imageSourceBuffer.getWriter()) {
                imageWriter.write(ByteBuffer.wrap(imageBytes));

                VisionSource imageSource = VisionSource.fromImageSourceBuffer(imageSourceBuffer);
                ImageAnalysisOptions analysisOptions = new ImageAnalysisOptions();
                analysisOptions.setFeatures(EnumSet.of(ImageAnalysisFeature.TAGS));

                try (VisionServiceOptions serviceOptions = new VisionServiceOptions(URI.create(endpoint).toURL(), key);
                     ImageAnalyzer analyzer = new ImageAnalyzer(serviceOptions, imageSource, analysisOptions);
                     ImageAnalysisResult result = analyzer.analyze()) {

                    if (result.getReason() == ImageAnalysisResultReason.ANALYZED) {
                        StringBuilder tags = new StringBuilder();
                        for (ContentTag tag : result.getTags()) {
                            if (tag.getConfidence() >= 0.85) {
                                tags.append(tag.getName()).append(",");
                            }
                        }
                        if (tags.length() > 0) {
                            tags.setLength(tags.length() - 1);
                            return tags.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void uploadImageToBlobStorage(String containerName, String blobName, InputStream imageStream) {
        try {
            // Initialize BlobServiceClient with the connection string from Secrets
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(Secrets.getBlobConnectionString())
                    .buildClient();

            // Get the container client
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Get a client for the specific blob
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Upload the image stream to the blob
            blobClient.upload(imageStream, imageStream.available(), true);

            System.out.println("Image uploaded successfully to blob: " + blobName);
        } catch (Exception e) {
            System.err.println("Error uploading image to Azure Blob Storage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String analyzeSampleImage(String endpoint, String key) {
        try (InputStream inputStream = new FileInputStream("src/main/java/org/app/sample.jpg")) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            inputStream.transferTo(byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            try (ImageSourceBuffer imageSourceBuffer = new ImageSourceBuffer();
                 ImageWriter imageWriter = imageSourceBuffer.getWriter()) {
                imageWriter.write(ByteBuffer.wrap(imageBytes));

                VisionSource imageSource = VisionSource.fromImageSourceBuffer(imageSourceBuffer);

                ImageAnalysisOptions analysisOptions = new ImageAnalysisOptions();
                analysisOptions.setFeatures(EnumSet.of(ImageAnalysisFeature.TAGS));

                try (VisionServiceOptions serviceOptions = new VisionServiceOptions(URI.create(endpoint).toURL(), key);
                     ImageAnalyzer analyzer = new ImageAnalyzer(serviceOptions, imageSource, analysisOptions);
                     ImageAnalysisResult result = analyzer.analyze()) {

                    if (result.getReason() == ImageAnalysisResultReason.ANALYZED) {
                        StringBuilder tags = new StringBuilder();
                        for (ContentTag tag : result.getTags()) {
                            if (tag.getConfidence() >= 0.85) {
                                tags.append(tag.getName()).append(",");
                            }
                        }

                        if (tags.length() > 0) {
                            tags.setLength(tags.length() - 1);
                            return tags.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No tags found or an error occurred.";
    }

    public static String sendDataToLambda(String photoID, String photoGUID, String tags) {
        try {
            // Create the JSON payload
            String jsonPayload = String.format("{\"photoID\":\"%s\",\"photoGUID\":\"%s\",\"tags\":\"%s\"}", photoID, photoGUID, tags);

            // Create a RestTemplate for HTTP requests
            RestTemplate restTemplate = new RestTemplate();

            // Create the headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the HTTP entity with the payload and headers
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            // Send the POST request
            ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8082/api/process-image-tags", requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Data successfully sent to Lambda.");
                return "Success";
            } else {
                System.err.println("Failed to send data to Lambda. Response: " + response.getBody());
                return "Failure";
            }
        } catch (Exception e) {
            System.err.println("Error sending data to Lambda: " + e.getMessage());
            e.printStackTrace();
            return "Failure";
        }
    }
}

