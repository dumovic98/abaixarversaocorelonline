package com.abaixarversaocdronline;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class CloudConvertService {

    public String converterArquivo(MultipartFile arquivo) {
        try {
            // 1. Cria diretório temporário
            File uploadDir = new File("/app/uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 2. Salva o arquivo enviado
            String nomeOriginal = arquivo.getOriginalFilename();
            File entrada = new File(uploadDir, nomeOriginal);
            arquivo.transferTo(entrada);

            // 3. Define o nome do arquivo convertido
            String nomeSaida = nomeOriginal.replace(".cdr", "-convertido.svg"); // ou .pdf etc.
            File saida = new File(uploadDir, nomeSaida);

            // 4. Executa o Inkscape via linha de comando
            ProcessBuilder pb = new ProcessBuilder(
                    "inkscape",
                    entrada.getAbsolutePath(),
                    "--export-filename=" + saida.getAbsolutePath()
            );
            pb.inheritIO();
            Process processo = pb.start();
            int status = processo.waitFor();

            if (status == 0 && saida.exists()) {
                return "✅ Conversão finalizada!\n\n👉 Download: /arquivos/" + saida.getName();
            } else {
                return "❌ Falha na conversão com Inkscape. Código de saída: " + status;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "❌ Erro ao processar o arquivo: " + e.getMessage();
        }
    }
}
