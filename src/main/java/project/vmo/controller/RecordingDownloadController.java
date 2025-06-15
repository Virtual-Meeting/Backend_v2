package project.vmo.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/recordings")
public class RecordingDownloadController {

    public static final String RECORDING_DIR = "/home/ubuntu/recording/";

    @GetMapping("/{fileName}")
    public ResponseEntity<?> downloadRecording(@PathVariable String fileName) throws IOException {
        if (fileName.contains("..")) return ResponseEntity.badRequest().body("잘못된 파일 경로입니다.");
        if (!fileName.endsWith(".webm")) return ResponseEntity.badRequest().body("지원되지 않는 파일 확장자입니다.");

        String roomId = fileName.substring(0, 6);
        fileName = fileName.replace(".webm.webm", ".webm");
        File webmFile = new File(RECORDING_DIR + roomId + "/" + fileName);

        if (!webmFile.exists()) return ResponseEntity.notFound().build();

        return buildFileResponse(webmFile);
    }

    private static ResponseEntity<Resource> buildFileResponse(File file) throws FileNotFoundException {
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}