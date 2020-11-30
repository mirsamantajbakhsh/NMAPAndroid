package ir.mstajbakhsh.nmapandroid;

import android.content.Context;
import android.util.Log;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class NMAPUtilities {

    private Context cntx;
    public static String fileName;
    public static String dataFileName;
    File nmapBinDirectory;
    File nmapBin;

    public NMAPUtilities(Context cntx) {
        this.cntx = cntx;
        nmapBinDirectory = cntx.getDir("bin", Context.MODE_PRIVATE);
        nmapBin = new File(nmapBinDirectory.getAbsolutePath() + File.separator + "nmap");
    }

    public void startInstallation() {
        if (!isInstalled()) {
            String arch = System.getProperty("os.arch");
            if (arch.contains("arm")) {
                fileName = "nmap-7.31-binaries-armeabi.zip";
            } else if (arch.contains("86")) {
                fileName = "nmap-7.31-binaries-x86.zip";
            }

            dataFileName = "nmap-7.31-data.zip";
            unzipFilesAndExtract();
        }
    }

    private void unzipFilesAndExtract() {
        boolean isErrorHappened = false;
        File zipFile = new File(cntx.getFilesDir() + "/", "nmap.zip");
        File dataFile = new File(cntx.getFilesDir() + "/", "nmap-data.zip");
        File nmapBinDirectory = cntx.getDir("bin", Context.MODE_PRIVATE);

        if (!nmapBinDirectory.exists()) {
            nmapBinDirectory.mkdirs();
        } else {
            try {
                new ProcessExecutor().command("rm", "-rf", nmapBinDirectory.getAbsolutePath()).execute();
            } catch (TimeoutException | IOException | InterruptedException ex) {
                Log.e("MSTNMAP", "Can't delete NMAP directory.\n" + ex.getMessage());
            }
            nmapBinDirectory.mkdirs();
        }

        //Copy nmap binary from resource to private file
        isErrorHappened = !copy(fileName, zipFile);

        //Unzip
        if (!isErrorHappened) {
            try {
                UnzipUtility.unzip(zipFile, nmapBinDirectory);
            } catch (IOException ex) {
                isErrorHappened = true;
                Log.e("MSTNMAP", "Can't unzip NMAP.\n" + ex.getMessage());
            }
        }

        isErrorHappened = !copy(dataFileName, dataFile);
        //Unzip Resources
        if (!isErrorHappened) {
            try {
                UnzipUtility.unzip(dataFile, nmapBinDirectory);
            } catch (IOException ex) {
                isErrorHappened = true;
                Log.e("MSTNMAP", "Can't unzip NMAP data.\n" + ex.getMessage());
            }
        }

        //Change owner
        if (!isErrorHappened) {
            for (File f : nmapBinDirectory.listFiles()) {
                try {
                    new ProcessExecutor().command("chmod", "6755", f.getAbsolutePath()).execute();
                } catch (Exception ex) {
                    Log.e("MSTNMAP", "Can't change owner of [" + f.getAbsolutePath() + "]. \n" + ex.getMessage());
                }
            }
        }
    }

    private boolean copy(String assetName, File outputFile) {
        try {
            InputStream is = cntx.getAssets().open(assetName);
            FileOutputStream fos = new FileOutputStream(outputFile);

            int read;
            byte[] bytes = new byte[1024];

            while ((read = is.read(bytes)) != -1) {
                fos.write(bytes, 0, read);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int execCommand(OutputStream os, String... args) throws Exception {
        startInstallation();

        if (!(nmapBin.exists() && nmapBin.canExecute())) {
            throw new Exception("Error in installing nmap! Check installation process.");
        }

        List<String> commands = new ArrayList<>();
        commands.add(nmapBin.getAbsolutePath());
        for (String element : args) {
            commands.add(element);
        }
        ProcessResult ps = new ProcessExecutor().command(commands)
                .redirectOutput(os)
                .redirectError(os)
                .readOutput(true).execute();

        return ps.getExitValue();
    }

    private boolean isInstalled() {
        return (nmapBin != null && nmapBinDirectory != null && nmapBinDirectory.exists() && nmapBinDirectory.isDirectory() && nmapBin.exists() && nmapBin.isFile() && nmapBin.canExecute());
    }
}
