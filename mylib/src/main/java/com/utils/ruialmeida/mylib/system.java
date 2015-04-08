package com.utils.ruialmeida.mylib;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by ruialmeida on 4/8/15.
 */
public class system {


    private static File mFile;
    private static ProgressDialog myProgress;
    private static Handler myHandler;

    public static void unzipFile(File zipfile, final Context context) {
        mFile = zipfile;
        myProgress = ProgressDialog.show(context, "Extract Zip",
                "Extracting Files...", true, false);
        String directory = zipfile.getParent() + "/" + zipfile.getName().replace(".zip", "") + File.separator;
        File dir = new File(directory);
        dir.delete();
         myHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // process incoming messages here
                switch (msg.what) {
                    case 0:
                        // update progress bar
                        myProgress.setMessage((String) msg.obj);
                        break;
                    case 1:
                        myProgress.cancel();
                        Toast toast = Toast.makeText(context,
                                "Zip extracted successfully",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case 2:
                        myProgress.cancel();
                        break;
                }
                super.handleMessage(msg);
            }

        };
        Thread workthread = new Thread(new UnZip(zipfile, directory));
        workthread.start();
    }

    private static class UnZip implements Runnable {

        File archive;
        String outputDir;

        private UnZip(File ziparchive, String directory) {
            archive = ziparchive;
            outputDir = directory;
        }


        @SuppressWarnings("unchecked")
        public void run() {
            Message msg;
            try {
                ZipFile zipfile = new ZipFile(archive);
                for (Enumeration e = zipfile.entries();
                     e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    msg = new Message();
                    msg.what = 0;
                    msg.obj = "Extracting " + entry.getName();
                    myHandler.sendMessage(msg);

                    unzipEntry(zipfile, entry, outputDir);
                }
            } catch (Exception e) {

                e.printStackTrace();
            }
            mFile.delete();
            msg = new Message();
            msg.what = 1;
            myHandler.sendMessage(msg);
        }

        private void unzipEntry(ZipFile zipfile, ZipEntry entry,
                                String outputDir) throws IOException {

            if (entry.isDirectory()) {
                createDir(new File(outputDir, entry.getName()));
                return;
            }

            File outputFile = new File(outputDir, entry.getName());
            if (!outputFile.getParentFile().exists()) {
                createDir(outputFile.getParentFile());
            }
            BufferedInputStream inputStream = new
                    BufferedInputStream(zipfile
                    .getInputStream(entry));
            BufferedOutputStream outputStream = new BufferedOutputStream(
                    new FileOutputStream(outputFile));

            try {
                copyInputStreamToFile(inputStream, outputStream);
            } finally {
                outputStream.close();
                inputStream.close();
            }
        }

        private void createDir(File dir) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Can not create dir " + dir);
            }

        }

        private static void copyInputStreamToFile(InputStream in, OutputStream out) {
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
