package com.abaixarversaocdronline;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class CloudConvertService {

    public String converterArquivo(MultipartFile arquivo) throws Exception {
        // 1. Salva o arquivo temporariamente em /tmp
        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.toLowerCase().endsWith(".cdr")) {
            return "❌ Arquivo inválido. Envie um arquivo .CDR";
        }

        File tempInput = new File("/tmp/" + System.currentTimeMillis() + "_" + nomeOriginal);
        try (InputStream in = arquivo.getInputStream(); FileOutputStream out = new FileOutputStream(tempInput)) {
            in.transferTo(out);
        }

        // 2. Define o nome de saída com extensão .pdf (ou .svg, se preferir)
        String nomeSaida = tempInput.getName().replace(".cdr", ".pdf");
        File tempOutput = new File("/tmp/" + nomeSaida);

        // 3. Executa o Inkscape
        ProcessBuilder processBuilder = new ProcessBuilder(
                "inkscape",
                tempInput.getAbsolutePath(),
                "--export-type=pdf",
                "--export-filename=" + tempOutput.getAbsolutePath()
        );

        processBuilder.redirectErrorStream(true);
        Process processo = processBuilder.start();
        int status = processo.waitFor();

        if (status != 0 || !tempOutput.exists()) {
            return "❌ Erro ao converter com o Inkscape. Código: " + status;
        }

        return "✅ Conversão realizada com sucesso! 📄 Arquivo gerado: <a href='/download/1745370164753_Modelos Carro.pdf' target='_blank'>1745370164753_Modelos Carro.pdf</a>";
    }
}
