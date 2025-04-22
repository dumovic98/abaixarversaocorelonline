package com.abaixarversaocdronline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            e.printStackTrace(); // Mostra o erro no console
            return "Erro ao processar o arquivo: " + e.getMessage();
        }
    }
    @GetMapping("/jobs")
    @ResponseBody
    public String listarJobs() {
        return cloudConvertService.listarJobsRecentes();
    }

    @GetMapping("/deletar/{id}")
    @ResponseBody
    public String deletarJob(@PathVariable String id) {
        return cloudConvertService.deletarJob(id);
    }

}
