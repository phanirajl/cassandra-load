package ch.admin.bit.cassandraperformancetester.loadgenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public class LoadDataGenerator {

    private static Random random = new Random();

    public static String valueAsString(String type) {
       switch(type.toLowerCase()) {
           case "text":
               return getText();
           case "ascii":
               return getAscii();
           case "int":
               return getInt();
           case "bigint":
               return getBigint();
           case "varint":
               return getVarint();
           case "float":
               return getFloat();
           case "double":
               return getDouble();
           case "decimal":
               return getDecimal();
           case "timestamp":
               return getTimestamp();
           case "uuid":
               return getUuid();
           case "timeuuid":
               return getTimeuuid();
           case "boolean":
               return getBoolean();
           case "blob":
               return getBlob();
           default:
               return "UNDEFINED TYPE";
       }
    }

    private static String getText() {
        return uniqueString;
    }

    private static String getAscii() {
        return uniqueString;
    }

    private static String getInt() {
        return "" + random.nextInt();
    }

    private static String getBigint() {
        return "" + random.nextLong();
    }

    private static String getVarint() {
        return "" + Long.MAX_VALUE;
    }

    private static String getFloat() {
        return "" + random.nextFloat();
    }

    private static String getDouble() {
        return "" + random.nextDouble();
    }

    private static String getDecimal() {
        return "" + Double.MAX_VALUE;
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private static String getUuid () {
        return UUID.randomUUID().toString();
    }

    private static String getTimeuuid() {
        return UUID.randomUUID().toString();
    }

    private static String getBoolean() {
        return "true";
    }

    //bigintAsBlob is a built-in function of cassandra
    private static String getBlob() {
        return "bigintAsBlob(" + Long.MAX_VALUE + ")";
    }

    private static String uniqueString = LocalDateTime.now().toString() + " is a unique time in stringformat " + UUID.randomUUID().toString();
}
