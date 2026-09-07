package com.osmig.Jweb.framework.upload;

import org.springframework.web.multipart.MultipartFile;

/**
 * @deprecated Moved to {@link jweb.UploadedFile} — same class, shorter import.
 *             {@code request.file("...")} returns {@code jweb.UploadedFile}.
 */
@Deprecated
public class UploadedFile extends jweb.UploadedFile {

    public UploadedFile(MultipartFile multipartFile) {
        super(multipartFile);
    }
}
