package payment.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import payment.util.PDFUtil;
import payment.util.SignatureUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@RestController
@RequestMapping("/pdf")
public class PDFService {
    @GetMapping("/gateway")
    public String gateway(){
        return "PDF Server is Running";
    }

    public static ResponseEntity<?> getSignature(String title){
        try{
            Instant expiration = Instant.now().plus(SignatureUtil.PDFexpiry);
            String sign = SignatureUtil.generateSignature(title, expiration);

            String jsonResponse = String.format("{\"signature\":\"%s\"}", sign);

            return ResponseEntity.ok().body(jsonResponse);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/file", params = "signature")
    public ResponseEntity<Resource> download(@RequestParam String signature){
        boolean valid;
        try {
            valid = SignatureUtil.verifySignature(signature);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed to verify signature");
            return ResponseEntity.badRequest().build();
        }

        if(valid){
            String title;
            try {
                title = SignatureUtil.getIdentifier(signature);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Failed to decode title");
                return ResponseEntity.notFound().build();
            }

            if(title == null){
                return ResponseEntity.notFound().build();
            }

            try {
                String filepath = PDFUtil.basepath + title;
                Path path = Paths.get(filepath);
                Resource resource = new UrlResource(path.toUri());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + title);
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Failed to load resource");
                return ResponseEntity.notFound().build();
            }
        } else{
            System.out.println("Invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
