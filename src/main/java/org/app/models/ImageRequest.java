package org.app.models;

import java.io.InputStream;

public class ImageRequest {
    private String photoID;
    private String photoGUID;
    private InputStream inputStream;

    public ImageRequest(String photoID, String photoGUID, InputStream inputStream) {
        this.photoID = photoID;
        this.photoGUID = photoGUID;
        this.inputStream = inputStream;
    }

    public String getPhotoID() {
        return photoID;
    }

    public String getPhotoGUID() {
        return photoGUID;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
