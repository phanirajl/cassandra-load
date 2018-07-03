package ch.admin.bit.cassandraperformancetester;

import ch.admin.bit.cassandraperformancetester.loadgenerator.LoadGenerationTemplate;
import ch.admin.bit.cassandraperformancetester.loadgenerator.LoadGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:63342")
public class CassandraPerformanceApi {

    @PostMapping(value = "/starttest/{host}/{keyspace}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> start(@RequestBody String queries, @PathVariable("host") String host, @PathVariable("keyspace") String keyspace) {

        List<String> qss = new ArrayList<>();

        JSONObject queryObject = new JSONObject(queries);
        JSONArray qs = (JSONArray) queryObject.get("queries");

        Iterator<Object> it = qs.iterator();
        while (it.hasNext()) {
            JSONObject current = ((JSONObject) it.next());
            qss.add((String) current.get("query"));
        }

        CassandraPerformancetesterApplication.Test t = new CassandraPerformancetesterApplication.Test(host, keyspace, qss);
        t.executeQueries();

        return ResponseEntity.ok("START: " + t.getStartTime() + ", END: " + t.getEndTime());
    }

    @PostMapping(value = "/getload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getLoad(@RequestBody String generationConfig) {

        LoadGenerator.generateLoad(new LoadGenerationTemplate(generationConfig, LoadGenerator.DEFAULT_SAVEPATH));

        List<String> lines;
        try {
            lines = Files.readAllLines(LoadGenerator.DEFAULT_SAVEPATH);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.ok(e.getMessage());
        }
        StringBuilder sb = new StringBuilder();
        lines.forEach(sb::append);

        return ResponseEntity.ok(sb.toString());
    }
}
