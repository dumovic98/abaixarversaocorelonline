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

    @PostMapping("/enviar")
    @ResponseBody
    public String receberArquivo(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            if (arquivo.isEmpty()) {
                return "Arquivo não foi enviado!";
            }

            String resposta = cloudConvertService.converterArquivo(arquivo);
            return "Arquivo recebido e enviado para conversão. Resposta:\n\n" + resposta;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao processar o arquivo: " + e.getMessage();
        }
    }

    @GetMapping("/arquivos/{nome}")
    public ResponseEntity<FileSystemResource> baixarArquivo(@PathVariable String nome) {
        File arquivo = new File("/app/uploads/" + nome);

        if (!arquivo.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(arquivo);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(nome).build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/download/{arquivo}")
    public ResponseEntity<FileSystemResource> baixar(@PathVariable String arquivo) {
        File output = new File("/tmp/" + arquivo);
        if (!output.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + output.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(output));
    }

}
