package net.milanaleksic.mcs.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: Milan Aleksic
 * Date: 25/08/11
 * Time: 11:00
 */
public final class StreamUtil {

    private final static Logger log = Logger.getLogger(StreamUtil.class);

    public static String returnMD5ForFile(File input) {
        StringBuilder hash = new StringBuilder("");
        FileInputStream stream;
        MessageDigest digestAlg;
        try {
            digestAlg = MessageDigest.getInstance("MD5");
            stream = new FileInputStream(input);
            DigestInputStream digest = new DigestInputStream(stream, digestAlg);
            while (digest.read() != -1) {
                // keep reading
            }
            digest.close();

            byte[] calcDigest = digestAlg.digest();
            for (byte aCalcDigest : calcDigest)
                hash.append(String.format("%1$02X", aCalcDigest));

            log.debug("Calculated hash for file " + input.getAbsolutePath() + " is " + hash.toString());

        } catch (Exception e) {
            log.error("Failure while calculating MD5", e);
        }
        return hash.toString();
    }

    public static void writeFileToZipStream(ZipOutputStream zos, String fileName, String entryName) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("restore\\"+fileName);
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            copyStream(fis, zos);
        } finally {
            if (fis != null) try {
                fis.close();
            } catch (Throwable ignored) {
            }
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer, 0, buffer.length)) > 0) {
            output.write(buffer, 0, bytesRead);
        }
    }

}
