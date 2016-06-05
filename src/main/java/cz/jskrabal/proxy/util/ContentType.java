package cz.jskrabal.proxy.util;

import java.util.Optional;

/**
 * Created by janskrabal on 03/06/16.
 */
public enum ContentType {
    IMAGE_JPEG("image/jpeg", "jpg"),
    IMAGE_GIF("image/gif", "gif"),
    IMAGE_PNG("image/png", "png");

    private String contentType;
    private String fileExtension;

    ContentType(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static Optional<ContentType> fromContentType(String contentType) {
        for(ContentType val: ContentType.values()){
            if(val.getContentType().equals(contentType)){
                return Optional.of(val);
            }
        }
        return Optional.empty();
    }
}
