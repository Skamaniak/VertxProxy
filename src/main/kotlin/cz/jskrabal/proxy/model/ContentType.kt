package cz.jskrabal.proxy.model

/**
 * Created by janskrabal on 03/06/16.
 */
enum class ContentType(val contentType: String, val fileExtension: String) {
    IMAGE_JPEG("image/jpeg", "jpg"),
    IMAGE_GIF("image/gif", "gif"),
    IMAGE_PNG("image/png", "png");


    companion object {
        fun fromContentType(contentType: String): ContentType? {
            return ContentType.values()
                    .firstOrNull { it.contentType == contentType }
        }

        fun fromExtension(extension: String): ContentType? {
            return ContentType.values()
                    .firstOrNull { it.fileExtension == extension }
        }
    }
}
