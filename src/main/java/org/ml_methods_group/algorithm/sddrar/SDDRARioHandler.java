package org.ml_methods_group.algorithm.sddrar;

import com.simiyutin.au.sddrar.RulePack;

import java.io.*;
import java.util.List;

public class SDDRARioHandler {

    public static final String RULES_FILE = "rules.data";
    public static final String METRICS_FILE = "metrics.data";
    private static final String DATA_FOLDER = "/sddrar_data/";

    public static void dumpRulePack(RulePack pack) {
        dump(pack, RULES_FILE);
    }

    public static RulePack loadRulePack() {
        return load(RULES_FILE);
    }

    public static void dumpMetrics(List<String> metrics) {
        dump(metrics, METRICS_FILE);
    }

    public static List<String> loadMetrics() {
        return load(METRICS_FILE);
    }

    public static void dumpDataSet(DataSet dataSet, String name) {
        dump(dataSet, name);
    }

    public static DataSet loadDataSet(String name) {
        return load(name);
    }

    public static void dump(Object obj, String name) {
        try {
            FileOutputStream fos = new FileOutputStream(DATA_FOLDER + name);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T load(String name) {
        try {
            InputStream is = SDDRARioHandler.class.getResourceAsStream(DATA_FOLDER + name);
            ObjectInputStream ois = new ObjectInputStream(is);
            T obj = (T) ois.readObject();
            return obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
