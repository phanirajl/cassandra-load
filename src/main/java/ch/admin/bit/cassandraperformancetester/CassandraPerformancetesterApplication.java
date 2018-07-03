package ch.admin.bit.cassandraperformancetester;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class CassandraPerformancetesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CassandraPerformancetesterApplication.class, args);

//        String contactPoint = "localhost";
//        String keyspace = "teststress";
//        List<String> queries = new ArrayList<>();
//
//        String path = "/home/dev/IdeaProjects/cassandra-performancetester/load.json";
//        String json = "";
//        try {
//            Path p = Paths.get(path);
//            List<String> lines = Files.readAllLines(p);
//            StringBuilder sb = new StringBuilder();
//            lines.forEach(sb::append);
//            json = sb.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        JSONObject queryObject = new JSONObject(json);
//        JSONArray qs = (JSONArray) queryObject.get("queries");
//
//        Iterator<Object> it = qs.iterator();
//        while (it.hasNext()) {
//            JSONObject current = ((JSONObject) it.next());
//            queries.add((String) current.get("query"));
//        }
//
//        Test t = new Test(contactPoint, keyspace, queries);
//        t.executeQueries();
    }


    static class Test {

        private CassandraOperations conn;
        private List<String> queries;

        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public Test(String contactPoint, String keyspace, List<String> queries) {
            this.conn = cassandraConfig(contactPoint, keyspace);
            this.queries = queries;
        }

        public void executeQueries() {
            startTime = LocalDateTime.now();
            this.queries.forEach(q -> {
                System.out.println("Running query: " + q);
                this.conn.getCqlOperations().execute(q);
            });
            endTime = LocalDateTime.now();

            System.out.println("----------------------------");
            System.out.println("EXECUTED FOLLOWING QUERIES: ");
            System.out.println("----------------------------");
            this.queries.forEach(System.out::println);
            System.out.println("----------------------------");
            System.out.println("START: " + startTime + ", END: " + endTime);
            System.out.println("----------------------------");
        }

        public CassandraOperations cassandraConfig(String contactPoint, String keyspace) {

            Cluster c = Cluster.builder().addContactPoint(contactPoint).build();
            Session s = c.connect(keyspace);

            return new CassandraTemplate(s);
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }
    }
}
