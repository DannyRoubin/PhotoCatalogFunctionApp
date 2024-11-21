package org.app;

public class SecretsTemplate {

    private static final String KEY = "your-key-here";
    private static final String ENDPOINT = "your-endpoint-here";

    // Blob Storage secrets
    private static final String BLOB_ACCOUNT_NAME = "your-blob-account-name-here";
    private static final String BLOB_KEY = "your-blob-key-here";
    private static final String BLOB_CONNECTION_STRING = "your-blob-connection-string-here";

    public static String getKey() {
        return KEY;
    }

    public static String getEndpoint() {
        return ENDPOINT;
    }

    public static String getBlobAccountName() {
        return BLOB_ACCOUNT_NAME;
    }

    public static String getBlobKey() {
        return BLOB_KEY;
    }

    public static String getBlobConnectionString() {
        return BLOB_CONNECTION_STRING;
    }
}