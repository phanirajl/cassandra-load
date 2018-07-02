package ch.admin.bit.cassandraperformancetester;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class CassandraPerformancetesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CassandraPerformancetesterApplication.class, args);

        String contactPoint = "localhost";
        String keyspace = "ebdstress";
        List<String> queries = new ArrayList<>();

        queries.add("SELECT * FROM document WHERE document_id = 00000003-4012-100d-0000-0006b538c7a9");
        queries.add("SELECT * FROM document WHERE document_id = 0000000c-ed9e-070a-0000-0003de0a3873");

        UUID uuid = UUID.randomUUID();
        queries.add("INSERT INTO document (document_id, filename) VALUES (" + uuid + ", 'cassandra test')");
        queries.add("SELECT * FROM document WHERE document_id = " + uuid);

        Test t = new Test(contactPoint, keyspace, queries);
        t.executeQueries();
    }


    static class Test {

        private CassandraOperations conn;
        private List<String> queries;

        public Test(String contactPoint, String keyspace, List<String> queries) {
            this.conn = cassandraConfig(contactPoint, keyspace);
            this.queries = queries;
        }

        public void executeQueries() {
            LocalDateTime start = LocalDateTime.now();
            this.queries.forEach(this.conn.getCqlOperations()::execute);
            LocalDateTime end = LocalDateTime.now();

            System.out.println("----------------------------");
            System.out.println("EXECUTED FOLLOWING QUERIES: ");
            System.out.println("----------------------------");
            this.queries.forEach(System.out::println);
            System.out.println("----------------------------");
            System.out.println("START: " + start + ", END: " + end);
            System.out.println("----------------------------");
        }

        public CassandraOperations cassandraConfig(String contactPoint, String keyspace) {

            Cluster c = Cluster.builder().addContactPoint(contactPoint).build();
            Session s = c.connect(keyspace);

            return new CassandraTemplate(s);
        }
    }
}
