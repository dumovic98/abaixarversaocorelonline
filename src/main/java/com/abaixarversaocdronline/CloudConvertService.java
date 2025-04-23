package com.abaixarversaocdronline;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class CloudConvertService {

    public String converterArquivo(MultipartFile arquivo) throws Exception {
        // 1. Verifica o nome e extensão
        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.toLowerCase().endsWith(".cdr")) {
            return "❌ Arquivo inválido. Envie um arquivo .CDR";
        }

        // 2. Salva arquivo temporário no /tmp com timestamp para evitar conflito
        File tempInput = new File("/tmp/" + System.currentTimeMillis() + "_" + nomeOriginal);
        try (InputStream in = arquivo.getInputStream(); FileOutputStream out = new FileOutputStream(tempInput)) {
            in.transferTo(out);
        }

        // 3. Define nome do arquivo de saída (.pdf)
        String nomeSaida = tempInput.getName().replaceAll("(?i)\\.cdr$", ".pdf");
        File outputFile = new File("/tmp/" + nomeSaida);

        // 4. Executa o comando Inkscape no terminal
        ProcessBuilder pb = new ProcessBuilder(
                "inkscape",
                tempInput.getAbsolutePath(),
                "--export-type=pdf",
                "--export-filename=" + outputFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process processo = pb.start();
        int exitCode = processo.waitFor();

        if (exitCode != 0 || !outputFile.exists()) {
            return "❌ Erro ao converter com o Inkscape. Código: " + exitCode;
        }

        // 5. Retorna o link de download
        String nomeEncoded = URLEncoder.encode(outputFile.getName(), StandardCharsets.UTF_8);
        return "✅ Conversão realizada com sucesso! <br>📥 <a href='/download/" + nomeEncoded + "'>Baixar " + outputFile.getName() + "</a>";
    }
}
