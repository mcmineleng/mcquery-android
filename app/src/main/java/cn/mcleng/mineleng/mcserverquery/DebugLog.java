package cn.mcleng.mineleng.mcserverquery;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugLog {

    private static final String DIR = "/data/data/cn.mcleng.mineleng.mcserverquery/cache/logs";

    public static void log(String tag, String msg) {
        write(tag + ": " + msg);
    }

    public static void error(String tag, String msg, Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        write(tag + " ERROR: " + msg + "\n" + sw.toString());
    }

    private static synchronized void write(String line) {
        try {
            File dir = new File(DIR);
            if (!dir.exists()) dir.mkdirs();
            String timestamp = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
            File file = new File(dir, "debug.log");
            FileWriter fw = new FileWriter(file, true);
            fw.write("[" + timestamp + "] " + line + "\n");
            fw.close();
        } catch (Exception ignored) {}
    }
}
