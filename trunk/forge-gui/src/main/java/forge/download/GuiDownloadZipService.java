package forge.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.esotericsoftware.minlog.Log;
import com.google.common.io.Files;

import forge.FThreads;
import forge.interfaces.IProgressBar;
import forge.util.FileUtil;

public class GuiDownloadZipService extends GuiDownloadService {
    private final String name, desc, sourceUrl, destFolder, deleteFolder;
    private int filesDownloaded;

    public GuiDownloadZipService(String name0, String desc0, String sourceUrl0, String destFolder0, String deleteFolder0, IProgressBar progressBar0) {
        name = name0;
        desc = desc0;
        sourceUrl = sourceUrl0;
        destFolder = destFolder0;
        deleteFolder = deleteFolder0;
        progressBar = progressBar0;
    }

    @Override
    public String getTitle() {
        return "Download " + name;
    }

    @Override
    protected String getStartOverrideDesc() {
        return desc;
    }

    @Override
    protected final Map<String, String> getNeededFiles() {
        HashMap<String, String> files = new HashMap<String, String>();
        files.put("_", "_");
        return files; //not needed by zip service, so just return map of size 1
    }

    @Override
    public final void run() {
        downloadAndUnzip();
        if (!cancel) {
            FThreads.invokeInEdtNowOrLater(new Runnable() {
                @Override
                public void run() {
                    progressBar.setDescription(filesDownloaded + " " + desc + " downloaded");
                    finish();
                }
            });
        }
    }

    public void downloadAndUnzip() {
        filesDownloaded = 0;
        String zipFilename = download("temp.zip");
        if (zipFilename == null) { return; }

        //if assets.zip downloaded successfully, unzip into destination folder
        try {
            if (deleteFolder != null) {
                File deleteDir = new File(deleteFolder);
                if (deleteDir.exists()) {
                    //attempt to delete previous res directory if to be rebuilt
                    progressBar.reset();
                    progressBar.setDescription("Deleting old " + desc + "...");
                    if (deleteFolder.equals(destFolder)) { //move zip file to prevent deleting it
                        String oldZipFilename = zipFilename;
                        zipFilename = deleteDir.getParentFile().getAbsolutePath() + File.separator + "temp.zip";
                        Files.move(new File(oldZipFilename), new File(zipFilename));
                    }
                    FileUtil.deleteDirectory(deleteDir);
                }
            }

            ZipFile zipFile = new ZipFile(zipFilename, Charset.forName("CP866")); //ensure unzip doesn't fail due to non UTF-8 chars
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            progressBar.reset();
            progressBar.setPercentMode(true);
            progressBar.setDescription("Extracting " + desc);
            progressBar.setMaximum(zipFile.size());

            FileUtil.ensureDirectoryExists(destFolder);

            int count = 0;
            while (entries.hasMoreElements()) {
                if (cancel) { break; }

                ZipEntry entry = (ZipEntry)entries.nextElement();

                String path = destFolder + entry.getName();
                if (entry.isDirectory()) {
                    new File(path).mkdir();
                    progressBar.setValue(++count);
                    continue;
                }
                copyInputStream(zipFile.getInputStream(entry), path);
                progressBar.setValue(++count);
                filesDownloaded++;
            }

            zipFile.close();
            new File(zipFilename).delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String download(String filename) {
        progressBar.reset();
        progressBar.setPercentMode(true);
        progressBar.setDescription("Downloading " + desc);

        try {
            URL url = new URL(sourceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(getProxy());

            if (url.getPath().endsWith(".php")) {
                //ensure file can be downloaded if returned from PHP script
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)");
            }

            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            long contentLength = conn.getContentLength();
            if (contentLength == 0) {
                return null;
            }

            progressBar.setMaximum(100);

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(conn.getInputStream(), 8192);

            FileUtil.ensureDirectoryExists(destFolder);
 
            // output stream to write file
            String destFile = destFolder + filename;
            OutputStream output = new FileOutputStream(destFile);
 
            int count;
            long total = 0;
            byte data[] = new byte[1024];
 
            while ((count = input.read(data)) != -1) {
                if (cancel) { break; }

                total += count;
                progressBar.setValue((int)(100 * total / contentLength));
                output.write(data, 0, count);
            }
 
            output.flush();
            output.close();
            input.close();

            if (cancel) {
                new File(destFile).delete();
                return null;
            }
            return destFile;
        }
        catch (final Exception ex) {
            Log.error("Downloading " + desc, "Error downloading " + desc, ex);
        }
        return null;
    }

    protected void copyInputStream(InputStream in, String outPath) throws IOException{
        byte[] buffer = new byte[1024];
        int len;
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));

        while((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }
}
