/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.haw3d.jadvalKalemat;

import java.io.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.haw3d.jadvalKalemat.io.IO;
import com.haw3d.jadvalKalemat.puz.Playboard;
import com.haw3d.jadvalKalemat.view.PlayboardRenderer;

public class jadvalKalematApplication extends Application {

    public static File CROSSWORDS_DIR;
    public static File TEMP_DIR;
    public static File QUARANTINE_DIR;

    public static File CACHE_DIR;
    public static File DEBUG_DIR;

    private static final Logger LOG = Logger.getLogger("com.haw3d.jadvalKalemat");

    public static final String DEVELOPER_EMAIL = "jadvalKalemat@haw3d.com";

    private static final String PREFERENCES_VERSION_PREF = "preferencesVersion";
    private static final int PREFERENCES_VERSION = 4;

    private static Context mContext;

    public static Playboard BOARD;
    public static PlayboardRenderer RENDERER;

    private static PuzzleDatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // Check preferences version and perform any upgrades if necessary
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int prefsVersion = prefs.getInt(PREFERENCES_VERSION_PREF, 0);
        if (prefsVersion != PREFERENCES_VERSION) {
            migratePreferences(prefs, prefsVersion);
        }

        File externalStorageDir = new File(
            Environment.getExternalStorageDirectory(),
            "Android/data/" + getPackageName() + "/files");
//todo assets copy
        CROSSWORDS_DIR = new File(externalStorageDir, "crosswords");
        TEMP_DIR = new File(externalStorageDir, "temp");
        QUARANTINE_DIR = new File(externalStorageDir, "quarantine");

        //if (!PreferenceManager.getDefaultSharedPreferences(
        //        getApplicationContext())
        //        .getBoolean("installed", false)) {
        //    PreferenceManager.getDefaultSharedPreferences(
        //            getApplicationContext())
        //            .edit().putBoolean("installed", true).commit();
            copyAssetFolder(getAssets(), "puzzles",externalStorageDir+"/crosswords");
        //}

        CACHE_DIR = getCacheDir();
        DEBUG_DIR = new File(CACHE_DIR, "debug");

        makeDirs();

        if (DEBUG_DIR.isDirectory() || DEBUG_DIR.mkdirs()) {
            File infoFile = new File(DEBUG_DIR, "device.txt");
            try {
                PrintWriter writer = new PrintWriter(infoFile);
                try {
                    writer.println("VERSION INT: " + android.os.Build.VERSION.SDK_INT);
                    writer.println("VERSION RELEASE: " + android.os.Build.VERSION.RELEASE);
                    writer.println("MODEL: " + android.os.Build.MODEL);
                    writer.println("DEVICE: " + android.os.Build.DEVICE);
                    writer.println("DISPLAY: " + android.os.Build.DISPLAY);
                    writer.println("MANUFACTURER: " + android.os.Build.MANUFACTURER);
                } finally {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOG.warning("Failed to create directory tree: " + DEBUG_DIR);
        }

        dbHelper = new PuzzleDatabaseHelper(this);
    }

    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                {
                    File f=new File(toPath , file);
                    if(!f.exists())
                        res &= copyAsset(assetManager,
                                fromAssetPath + "/" + file,
                                toPath + "/" + file);
                }
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static Context getContext() {
        return mContext;
    }

    private void migratePreferences(SharedPreferences prefs, int prefsVersion) {
        LOG.info("Upgrading preferences from version " + prefsVersion + " to to version " + PREFERENCES_VERSION);

        SharedPreferences.Editor editor = prefs.edit();

        switch (prefsVersion) {
        case 0:
            editor.putBoolean("enableIndividualDownloadNotifications", !prefs.getBoolean("suppressMessages", false));
            // Fall-through
        case 1:
            editor.putBoolean("showRevealedLetters", !prefs.getBoolean("suppressHints", false));
            // Fall-through
        case 2:
            editor.putString("showKeyboard",
                             prefs.getBoolean("forceKeyboard", false) ? "SHOW" : "AUTO");
            // Fall-through
        case 3:
            try {
                // This is ugly.  But I don't see a clean way of detecting
                // what data type a preference is.
                int clueSize = prefs.getInt("clueSize", 12);
                editor.putString("clueSize", Integer.toString(clueSize));
            } catch (ClassCastException e) {
                // Ignore
            }
        }

        editor.putInt(PREFERENCES_VERSION_PREF, PREFERENCES_VERSION);
        editor.commit();
    }

    public static boolean makeDirs() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        for (File dir : new File[]{CROSSWORDS_DIR, TEMP_DIR, QUARANTINE_DIR, DEBUG_DIR}) {
            if (!dir.isDirectory() && !dir.mkdirs()) {
                LOG.warning("Failed to create directory tree: " + dir);
                return false;
            }
        }

        return true;
    }

    @SuppressLint("WorldReadableFiles")
    public static Intent sendDebug(Context context) {
        String filename = "debug.zip";
        File zipFile = new File(context.getFilesDir(), filename);
        if (zipFile.exists()) {
            zipFile.delete();
        }

        if (!DEBUG_DIR.exists()) {
            LOG.warning("Can't send debug package, " + DEBUG_DIR + " doesn't exist");
            return null;
        }

        saveLogFile();

        try {
            ZipOutputStream zos = new ZipOutputStream(
                context.openFileOutput(filename, MODE_WORLD_READABLE));
            try {
                zipDir(DEBUG_DIR.getAbsolutePath(), zos);
            } finally {
                zos.close();
            }

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { DEVELOPER_EMAIL });
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Words With Crosses Debug Package");
            Uri uri = Uri.fromFile(zipFile);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            LOG.info("Sending debug info: " + uri);
            sendIntent.setType("application/octet-stream");
            return sendIntent;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void saveLogFile() {
        try {
            // Use logcat to copy the log file into the debug directory
            File logFile = new File(DEBUG_DIR, "jadvalKalemat.log");
            FileOutputStream fos = new FileOutputStream(logFile);
            try {
                Process process = Runtime.getRuntime().exec("logcat -d");
                IO.copyStream(process.getInputStream(), fos);
                process.waitFor();
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void zipDir(String dir2zip, ZipOutputStream zos)
        throws IOException {
        File zipDir = new File(dir2zip);
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[4096];
        int bytesIn = 0;
        for (int i = 0; i < dirList.length; i++) {
            File f = new File(zipDir, dirList[i]);
            if (f.isDirectory()) {
                String filePath = f.getPath();
                zipDir(filePath, zos);
                continue;
            }
            FileInputStream fis = new FileInputStream(f);

            ZipEntry anEntry = new ZipEntry(f.getPath());
            zos.putNextEntry(anEntry);
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            fis.close();
        }
    }

    public static PuzzleDatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }

    public static double getScreenSizeInInches(DisplayMetrics metrics) {
        double x = metrics.widthPixels/metrics.xdpi;
        double y = metrics.heightPixels/metrics.ydpi;
        return Math.hypot(x, y);
    }

    public static boolean isTabletish(DisplayMetrics metrics) {
        if (android.os.Build.VERSION.SDK_INT < 11) {
            return false;
        }

        double screenInches = getScreenSizeInInches(metrics);
        return (screenInches > 9.0);  // look for a 9" or larger screen.
    }
}
