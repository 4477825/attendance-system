package com.attendance.controller;

import com.attendance.common.ErrorCode;
import com.attendance.common.Result;
import com.attendance.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "File Upload API", description = "File upload and download endpoints")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    private Path resolvedUploadPath;

    @PostConstruct
    public void init() {
        resolvedUploadPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        if (!resolvedUploadPath.toFile().exists()) {
            resolvedUploadPath.toFile().mkdirs();
        }
    }

    @PostMapping("/file")
    @Operation(summary = "Upload file")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        File dest = resolvedUploadPath.resolve(fileName).toFile();

        try {
            file.transferTo(dest);
            String url = "/uploads/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "Download file by name")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try {
            File file = resolvedUploadPath.resolve(fileName).toFile();
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", file.getName());
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
