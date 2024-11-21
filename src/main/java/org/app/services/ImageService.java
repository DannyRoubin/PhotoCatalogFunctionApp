package org.app.services;

import com.azure.ai.vision.common.*;
import com.azure.ai.vision.imageanalysis.*;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import org.app.Secrets;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

public class ImageService {

    public static String analyzeAndStoreImage(org.app.models.ImageRequest imageRequest) {
        try {
            String tags = analyzeImage(imageRequest.getInputStream(), Secrets.getEndpoint(), Secrets.getKey());

            if (tags != null && !tags.isEmpty()) {
                System.out.println(String.format("Image processed successfully. PhotoID: %s, PhotoGUID: %s, Tags: %s",
                        imageRequest.getPhotoID(), imageRequest.getPhotoGUID(), tags));

                // Upload the image to Azure Blob Storage
                uploadImageToBlobStorage("photo-container", imageRequest.getPhotoGUID(), imageRequest.getInputStream());

                return String.format("Image processed successfully. Tags: %s", tags);
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
}
