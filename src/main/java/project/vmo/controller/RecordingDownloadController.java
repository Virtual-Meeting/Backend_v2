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
    public static final String CONVERTED_DIR = "/home/ubuntu/recording/converted/";

    @GetMapping("/{fileName}")
    @CrossOrigin(origins = {
            "https://localhost:3000",
            "http://localhost:3000",
            "http://localhost:5500",
            "https://vtestmo.kro.kr",
            "https://frontend-deploy-test-rosy.vercel.app",
            "https://front-v2-develop-test.vercel.app"
    })
    public ResponseEntity<?> downloadRecording(@PathVariable String fileName) throws IOException {
        if (fileName.contains("..")) return ResponseEntity.badRequest().body("잘못된 파일 경로입니다.");
        if (!fileName.endsWith(".webm")) return ResponseEntity.badRequest().body("지원되지 않는 파일 확장자입니다.");

        String roomId = fileName.substring(0, 5);
        File webmFile = new File(RECORDING_DIR + roomId + "/" + fileName);

        if (!webmFile.exists()) return ResponseEntity.notFound().build();

        String mp4FileName = fileName.replace(".webm", ".mp4");
        File mp4File = new File(CONVERTED_DIR + roomId + "/" + mp4FileName);

        if (!mp4File.exists()) {
            try {
                boolean converted = convertWebmToMp4(webmFile, mp4File);
                if (!converted) {
                    return ResponseEntity.internalServerError().body("mp4 변환에 실패했습니다: " + fileName);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.internalServerError().body("mp4로 변환 도중 인터럽트가 발생했습니다.");
            }
        }

        return buildFileResponse(mp4File);
    }

    private static boolean convertWebmToMp4(File webmFile, File mp4File) throws IOException, InterruptedException {
        File parent = mp4File.getParentFile();
        if (!parent.exists()) parent.mkdirs();

        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-i", webmFile.getAbsolutePath(),
                "-c:v", "libx264", "-c:a", "aac",
                mp4File.getAbsolutePath()
        );
        builder.redirectErrorStream(true);
        Process process = builder.start();
        int exitCode = process.waitFor();

        return exitCode == 0 && mp4File.exists();
    }

    private static ResponseEntity<Resource> buildFileResponse(File file) throws FileNotFoundException {
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://front-v2-develop-test.vercel.app")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}