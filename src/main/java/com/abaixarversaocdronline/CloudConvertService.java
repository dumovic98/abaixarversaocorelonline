package com.abaixarversaocdronline;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class CloudConvertService {

    private static final String API_URL = "https://api.cloudconvert.com/v2/jobs";
    private static final String API_KEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiMzJjY2VkZjk1NTZjMWVlNjYzZjk5OGU2ZDdkYWNlZjdlYWE5NGQ0M2YxZGJkZDFmYmJlZDFmNWY1ODM5NTdkYzdlMjdhMGE4YmVmMTc4MjkiLCJpYXQiOjE3NDUzNTg4MTMuMjk1NDEsIm5iZiI6MTc0NTM1ODgxMy4yOTU0MTIsImV4cCI6NDkwMTAzMjQxMy4yOTAzNTgsInN1YiI6IjcxNzEyNzU4Iiwic2NvcGVzIjpbInRhc2sucmVhZCIsInRhc2sud3JpdGUiLCJ3ZWJob29rLnJlYWQiLCJ3ZWJob29rLndyaXRlIl19.bfJB2msAhs9JeHg53zQfvGppfRFZ-_45iv7KlXpsbcPfmS29JkJP8bGHXTIBqFDUCeedNrmn1GWtoG5G4Kqcjg190_vxpRqzZvlYfVmwsGe6iBuRfyHL2q2E7OQ3IFKrQGOBWq4ziOddj-cmxEAwXW_HygM1Zk-VRGC56o85cXrzw133_3Fsrq-TeTjmAsho5k--0v7h62pdLa2e1WJVhcBik9ndNRDreZo7v9lH_NT1Nbke5jfRYQs3OgKFYWhlpUw3Fb_FiooBVASGcL_TEaTAhUZZ1kQjVoUcNDd6Mh37ONeWV3RU6HxBVhKcjJFxxoop8wKyXcUuLaLMErRWXaRQMvAyFu_KkUoxIrawHCsxXBrMM6LpxlgCm0w29GhkYdCXfy4GkKO5BSLUwgXGtFiM-utef8C6u6kulSQ8sRxEdzHXIoNca56pwMUeD7cC-FyQbU7geL_ZAO51c1jMLbf-svv1WF0r7wCr7XoB4i1agCw8TQo8yo3Ah1-Znph-HhicZH4aG_HIbeW4O6tQ-ftY-ZL_tB397Un_JFfW15Aq3R6OzBY14Pr31x3__ziKurNsZT60xl-UL4QNNKKzZRA1xWHX0MYLB7KeCPdH-sSUwcMmckTV0QR8m0yhTBDS1wQxqi-jS0gNuaXIhFSBDefIwwQfxdi2Ps1GoJeUdRw"; // Substitua pela sua chave (uma linha só)

    public String converterArquivo(MultipartFile arquivo) throws Exception {

        // 1. Checar se já existe job ativo
        String checkUrl = "https://api.cloudconvert.com/v2/jobs?per_page=5";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY);
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Void> checkRequest = new HttpEntity<>(headers);
        ResponseEntity<Map> checkResponse = restTemplate.exchange(checkUrl, HttpMethod.GET, checkRequest, Map.class);

        Iterable<Map> jobs = (Iterable<Map>) checkResponse.getBody().get("data");
        for (Map job : jobs) {
            String status = (String) job.get("status");
            if ("processing".equals(status) || "waiting".equals(status)) {
                return "⚠️ Já existe um arquivo sendo convertido. Por favor, aguarde a finalização antes de enviar outro.";
            }
        }

        // 2. Criar novo job com engine inkscape
        Map<String, Object> importTask = new HashMap<>();
        importTask.put("operation", "import/upload");

        Map<String, Object> convertTask = new HashMap<>();
        convertTask.put("operation", "convert");
        convertTask.put("input", "import-my-file");
        convertTask.put("input_format", "cdr");
        convertTask.put("output_format", "cdr");
        convertTask.put("engine", "inkscape"); // ⚠️ USANDO INKSCAPE AGORA

        Map<String, Object> exportTask = new HashMap<>();
        exportTask.put("operation", "export/url");
        exportTask.put("input", "convert-my-file");

        Map<String, Object> tasksMap = new HashMap<>();
        tasksMap.put("import-my-file", importTask);
        tasksMap.put("convert-my-file", convertTask);
        tasksMap.put("export-my-file", exportTask);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tasks", tasksMap);

        HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity("https://api.cloudconvert.com/v2/jobs", createRequest, Map.class);

        Map data = (Map) createResponse.getBody().get("data");
        String jobId = (String) data.get("id");

        // 3. Esperar até o link ficar disponível (máx 90s)
        String jobStatusUrl = "https://api.cloudconvert.com/v2/jobs/" + jobId;
        HttpEntity<Void> consulta = new HttpEntity<>(headers);

        for (int i = 0; i < 18; i++) { // 🔁 90 segundos
            Thread.sleep(5000);

            ResponseEntity<Map> consultaResponse = restTemplate.exchange(jobStatusUrl, HttpMethod.GET, consulta, Map.class);
            Map jobData = (Map) consultaResponse.getBody().get("data");
            Iterable<Map> jobTasks = (Iterable<Map>) jobData.get("tasks");

            for (Map task : jobTasks) {
                if ("export/url".equals(task.get("name")) && "finished".equals(task.get("status"))) {
                    Map result = (Map) task.get("result");
                    Iterable<Map> files = (Iterable<Map>) result.get("files");
                    for (Map file : files) {
                        return "✅ Conversão finalizada!\n\n👉 Link de download:\n" + file.get("url");
                    }
                }
            }
        }

        return "⏳ Arquivo enviado, mas o link ainda não está disponível após múltiplas tentativas. Tente novamente em instantes.";
    }

    public String listarJobsRecentes() {
        try {
            String url = "https://api.cloudconvert.com/v2/jobs?per_page=5";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(API_KEY);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map body = response.getBody(); // CORRIGIDO AQUI
            Iterable<Map> jobs = (Iterable<Map>) body.get("data");

            StringBuilder resultado = new StringBuilder("🧾 Últimos jobs:\n\n");

            for (Map job : jobs) {
                resultado.append("🆔 Job: ").append(job.get("id")).append("\n");
                resultado.append("📅 Criado: ").append(job.get("created_at")).append("\n");
                resultado.append("📌 Status: ").append(job.get("status")).append("\n");
                resultado.append("────────────────────\n");
            }

            return resultado.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao consultar jobs recentes: " + e.getMessage();
        }
    }

    public String deletarJob(String jobId) {
        try {
            String url = "https://api.cloudconvert.com/v2/jobs/" + jobId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(API_KEY);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            return "✅ Job " + jobId + " deletado com sucesso.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao deletar job " + jobId + ": " + e.getMessage();
        }
    }
}
