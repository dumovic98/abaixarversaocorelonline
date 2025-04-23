package com.abaixarversaocdronline;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class CloudConvertService {

    public File converterArquivoRetornandoArquivo(MultipartFile arquivo) throws Exception {
        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.toLowerCase().endsWith(".cdr")) {
            return null;
        }

        // Salva o arquivo de entrada no /tmp
        File tempInput = new File("/tmp/" + System.currentTimeMillis() + "_" + nomeOriginal);
        try (InputStream in = arquivo.getInputStream(); FileOutputStream out = new FileOutputStream(tempInput)) {
            in.transferTo(out);
        }

        // Define o nome do arquivo de saída
        String nomeSaida = tempInput.getName().replace(".cdr", ".pdf");
        File tempOutput = new File("/tmp/" + nomeSaida);

        // Executa o Inkscape
        ProcessBuilder pb = new ProcessBuilder(
                "inkscape",
                tempInput.getAbsolutePath(),
                "--export-type=pdf",
                "--export-filename=" + tempOutput.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process processo = pb.start();
        int status = processo.waitFor();

        return (status == 0 && tempOutput.exists()) ? tempOutput : null;
    }
}
