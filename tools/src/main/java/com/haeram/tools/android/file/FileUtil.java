package com.haeram.tools.android.file;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.widget.Toast;

import com.haeram.tools.com.debug.Tracer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by swhwang on 2016-12-05.
 */

public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * get File instance of Android Public Directory in External Storage
     * <p>
     * below example shows how to get Directory Path <tt>"/storage/emulated/0/Download/test"</tt>.
     * <pre>
     * File f = getExternalStoragePublicDir(Environment.DIRECTORY_DOWNLOADS, test);
     * </pre>
     *
     * @param pubDirType public directory types specified in {@link Environment} class
     * @param subDirName the name of directory placed under public directory
     */
    public static File getExternalStoragePublicDir(String pubDirType, String subDirName) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(pubDirType),
                    subDirName);
            if (!file.mkdirs()) {
                Tracer.e(TAG, "Directory not created");
            }
            return file;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static File getExternalStoragePrivateFile(Context context,
                                                     String pubDirType,
                                                     String subDirName) {
        try {
            File file = new File(context.getExternalFilesDir(pubDirType), subDirName);
            if (!file.mkdirs()) {
                Tracer.e(TAG, "Directory not created");
            }
            return file;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static String getCurrentTimeString() {
        return getCurrentTimeString("yyyy_MM_dd_HH_mm_ss");
    }

    public static String getCurrentTimeString(String pattern) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return dateFormat.format(date);
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ?
                applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    /**
     * Make a text file which has string contents
     *
     * @param path full path of the text file
     * @param s    string contents
     */
    public static void toTextFile(String path, String s) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(path, false));
            bw.write(s);
            bw.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            if (bw != null) try {
                bw.close();
            } catch (IOException e) {
            }
        }
    }

    public static String fromTextFile(String path)
            throws IOException {
        BufferedReader br = null;
        StringBuilder sb = null;
        try {
            br = new BufferedReader(new FileReader(path));
            sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }

    public static String linenumberfromTextFile(String path)
            throws IOException {
        LineNumberReader reader = null;
        StringBuilder sb = null;
        try {
            reader = new LineNumberReader(new FileReader(path));
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(reader.getLineNumber());
                sb.append(": ");
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
            }
        }

        return sb.toString();
    }

    public static void toLog(Context ctx, String content) {
        try {
            String appName = getApplicationName(ctx.getApplicationContext());

            //dir : /storage/emulated/0/Android/data/{appname}/files/log
            File dir = getExternalStoragePrivateFile(ctx, null, "log");

            if (dir != null) {
                String fileName = String.format("%s/%s_%s.log",
                        dir.getCanonicalPath(), appName, getCurrentTimeString());
                toTextFile(fileName, content);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(ctx, "Failed to save file",
                    Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(ctx, "Success to save file",
                Toast.LENGTH_SHORT).show();
    }

    public static void toLog(Context ctx, String fileName, String content) {
        try {
            //dir : /storage/emulated/0/Android/data/{appname}/files/log
            File dir = getExternalStoragePrivateFile(ctx, null, "log");

            if (dir != null) {
                String fullName = String.format("%s/%s.log",
                        dir.getCanonicalPath(), fileName);
                toTextFile(fullName, content);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(ctx, "Failed to save file",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(ctx, "Success to save file",
                Toast.LENGTH_SHORT).show();
    }
}
