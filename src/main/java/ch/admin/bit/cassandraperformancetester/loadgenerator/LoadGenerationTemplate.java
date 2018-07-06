package ch.admin.bit.cassandraperformancetester.loadgenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class LoadGenerationTemplate {

    //Configuration of LoadGenerator
    JSONObject conf;
    Path savePath;
    //Maps storing name and type of column as string
    String tableName;
    Map<String, String> columnNamesAndTypes;
    Map<String, String> partitionKeys;
    Map<String, String> secondaryIndizes;
    Map<String, String> indizes;
    int s;
    int i;
    int u;
    int d;

    List<String> selects;
    List<String> inserts;
    List<String> updates;
    List<String> deletes;

    Map<String, String> typeAndValue;

    public LoadGenerationTemplate(String json, Path loadSavePath) {

        this.conf = new JSONObject(json);
        this.savePath = loadSavePath;

        this.columnNamesAndTypes = new HashMap<>();
        this.partitionKeys = new HashMap<>();
        this.secondaryIndizes = new HashMap<>();
        this.indizes = new HashMap<>();

        this.selects = new ArrayList<>();
        this.inserts = new ArrayList<>();
        this.updates = new ArrayList<>();
        this.deletes = new ArrayList<>();

        parseConfig();

        this.typeAndValue = new HashMap<>();
        generateDummyValues();

        System.out.println("THIS : " + this.toString());
    }

    private void parseConfig() {

        JSONArray cols = (JSONArray) conf.get("columns");
        JSONArray partKeys = (JSONArray) conf.get("partitionKeys");
        JSONArray secInd = (JSONArray) conf.get("secondaryIndizes");
        JSONArray ind = (JSONArray) conf.get("indizes");

        this.tableName = conf.getString("tableName");

        fillMapFromJsonArray(cols, columnNamesAndTypes);
        fillMapFromJsonArray(partKeys, partitionKeys);
        fillMapFromJsonArray(secInd, secondaryIndizes);
        fillMapFromJsonArray(ind, indizes);

        this.s = conf.getInt("selects");
        this.i = conf.getInt("inserts");
        this.u = conf.getInt("updates");
        this.d = conf.getInt("deletes");
    }

    private void generateDummyValues() {
        for (String col : columnNamesAndTypes.keySet()) {
            if (!typeAndValue.containsKey(columnNamesAndTypes.get(col))) {
                typeAndValue.put(columnNamesAndTypes.get(col),LoadDataGenerator.valueAsString(columnNamesAndTypes.get(col)));
            }
        }
    }

    public void generate() {
        generate(true,true,true,true);
    }

    public void generate(boolean createSelects,
                         boolean createInserts,
                         boolean createUpdates,
                         boolean createDeletes) {


        if (createSelects) {
            for (int in = 0; in < s; in++) {
                createSelect();
            }
        }

        if (createInserts) {
            for (int in = 0; in < i; in++) {
                createInsert();
            }
        }

        if (createUpdates) {
            for (int in = 0; in < u; in++) {
                createUpdate();
            }
        }

        if (createDeletes) {
            for (int in = 0; in < d; in++) {
                createDelete();
            }
        }

        try {
            OutputStream out = Files.newOutputStream(savePath,StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING);
            out.write("{\n\t\"queries\": [".getBytes());
            writeListToFile(out, inserts, false);
            writeListToFile(out, selects, false);
            writeListToFile(out, updates, false);
            writeListToFile(out, deletes, true);
            out.write("\n\t]\n}".getBytes());
            System.out.println("writing to : " + savePath.toFile().getAbsolutePath());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeListToFile(OutputStream out, List<String> lines, boolean lastListInScope) {
        int lineCounter = lines.size();
        for (String line : lines) {
            try {
                System.out.println("Writing " + line + " as bytes (" + line.getBytes() + ") to file");
                if (lineCounter == 1 && lastListInScope) {
                    out.write(("\n\t\t{\"query\":\"" + line + "\"}").getBytes());
                } else {
                    out.write(("\n\t\t{\"query\":\"" + line + "\"},").getBytes());
                }
                lineCounter--;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createSelect() {
        String select = "SELECT * FROM " + tableName + ";";
        selects.add(select);
    }

    private void createInsert() {
        String insert = "INSERT INTO " + tableName + " (";
        for (String col : columnNamesAndTypes.keySet()) {
            insert += col + ",";
        }
        //delete last comma
        insert = insert.substring(0,insert.length()-1);
        insert += ") VALUES (";
        for (String type : columnNamesAndTypes.values()) {
            if (type.equals("text") || type.equals("ascii")) {
                insert += "'" + LoadDataGenerator.valueAsString(type) + "',";
            } else {
                insert += LoadDataGenerator.valueAsString(type) + ",";
            }
        }
        //delete last comma
        insert = insert.substring(0,insert.length()-1);
        insert += ");";
        inserts.add(insert);
    }

    private void createUpdate() {

        String col = getRandomColumn();
        if (partitionKeys.keySet().contains(col) || secondaryIndizes.keySet().contains(col)) {
            createUpdate();
            return;
        }

        String update = "UPDATE " + tableName + " SET ";
        if (columnNamesAndTypes.get(col).equals("text") || columnNamesAndTypes.get(col).equals("ascii")) {
            update += col + " = '" + typeAndValue.get(columnNamesAndTypes.get(col)) + "'";
        } else {
            update += col + " = " + typeAndValue.get(columnNamesAndTypes.get(col));
        }
        update += " WHERE " + getPartitionKeyAssertionString() + " AND " + getSecondaryIndizesAssertionString() + ";";
        updates.add(update);
    }

    private void createDelete() {

        String col = getRandomColumn();
        if (partitionKeys.keySet().contains(col) || secondaryIndizes.keySet().contains(col)) {
            createDelete();
            return;
        }

        String delete = "DELETE " + col + " FROM " + tableName + " WHERE " + getPartitionKeyAssertionString() + " AND " + getSecondaryIndizesAssertionString() + ";";
        deletes.add(delete);
    }

    private void fillMapFromJsonArray(JSONArray jsonArray, Map<String,String> map) {
        jsonArray.iterator().forEachRemaining(o -> {
            JSONObject col = (JSONObject) o;
            String key = col.keys().next();
            map.put(key, col.getString(key));
        });
    }

    private String getRandomColumn() {

        Random r = new Random();
        int size = (columnNamesAndTypes.size() > 0) ? columnNamesAndTypes.size()-1 : 1;
        int randomCol = r.nextInt(size);
        String col = "";
        int counter = 0;
        Iterator<String> cols = columnNamesAndTypes.keySet().iterator();
        while (counter <= randomCol) {
            col = cols.next();
            counter++;
        }

        return col;
    }

    private String getPartitionKeyAssertionString() {
        String pkas = "";

        if (partitionKeys.isEmpty()) {
            return "";
        }

        for (String pk : partitionKeys.keySet()) {
            if (columnNamesAndTypes.get(pk).equals("text") || columnNamesAndTypes.get(pk).equals("ascii")) {
                pkas += pk + " = '" + typeAndValue.get(partitionKeys.get(pk)) + "' AND";
            } else {
                pkas += pk + " = " + typeAndValue.get(partitionKeys.get(pk)) + " AND";
            }
        }

        pkas.substring(0,pkas.length()-4);
        System.out.println("ASSERTION STRING PK : " + pkas);
        return pkas;
    }

    private String getSecondaryIndizesAssertionString() {
        String sias = "";

        if (secondaryIndizes.isEmpty()) {
            return "";
        }

        for (String si : secondaryIndizes.keySet()) {
            if (columnNamesAndTypes.get(si).equals("text") || columnNamesAndTypes.get(si).equals("ascii")) {
                sias += si + " = '" + typeAndValue.get(secondaryIndizes.get(si)) + "' AND";
            } else {
                sias += si + " = " + typeAndValue.get(secondaryIndizes.get(si)) + " AND";
            }
        }

        sias.substring(0,sias.length()-4);
        System.out.println("ASSERTION STRING SI : " + sias);
        return sias;
    }

    @Override
    public String toString() {
        return "LoadGenerationTemplate{" +
                "conf=" + conf +
                ", savePath=" + savePath +
                ", tableName='" + tableName + '\'' +
                ", columnNamesAndTypes=" + columnNamesAndTypes +
                ", partitionKeys=" + partitionKeys +
                ", secondaryIndizes=" + secondaryIndizes +
                ", indizes=" + indizes +
                ", s=" + s +
                ", i=" + i +
                ", u=" + u +
                ", d=" + d +
                ", selects=" + selects +
                ", inserts=" + inserts +
                ", updates=" + updates +
                ", deletes=" + deletes +
                ", typeAndValue=" + typeAndValue +
                '}';
    }
}
