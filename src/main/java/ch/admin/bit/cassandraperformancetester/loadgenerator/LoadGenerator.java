package ch.admin.bit.cassandraperformancetester.loadgenerator;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class LoadGenerator {

    //Default is current directory
    private static final String DEFAULT_SAVEPATH = System.getProperty("user.dir") + "/load.json";

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Please provide JSON-File-Path");
            System.exit(1);
        }

        Path savePath = Paths.get(DEFAULT_SAVEPATH);
        if (args.length == 2) {
            savePath = Paths.get(args[1]);
        }

        Path confPath = Paths.get(args[0]);
        try {
            List<String> lines = Files.readAllLines(confPath);
            StringBuilder allInOne = new StringBuilder();
            lines.forEach(allInOne::append);
            generateLoad(new LoadGenerationTemplate(allInOne.toString(), savePath));
        } catch (IOException e) {
            System.out.println("Please provide existing JSON-File-Path");
            System.exit(1);
        }

    }

    public static void generateLoad(LoadGenerationTemplate loadGenerationTemplate) {
        loadGenerationTemplate.generate();
    }
}
