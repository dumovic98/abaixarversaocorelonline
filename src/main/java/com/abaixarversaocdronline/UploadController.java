package com.abaixarversaocdronline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
public class UploadController {

    @Autowired
    private CloudConvertService cloudConvertService;

    @GetMapping("/")
    public String index() {
        return "upload";
    }

    @PostMapping(value = "/enviar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<?> receberArquivo(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            if (arquivo.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Arquivo não foi enviado!");
            }

            File convertido = cloudConvertService.converterArquivoRetornandoArquivo(arquivo);

            if (convertido == null || !convertido.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("❌ Erro na conversão ou arquivo não gerado.");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + convertido.getName() + "\"")
                    .contentLength(convertido.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(convertido));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erro inesperado: " + e.getMessage());
        }
    }
}
