package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.Function;
import com.twmacinta.util.MD5;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: Milan Aleksic
 * Date: 25/08/11
 * Time: 11:00
 */
public final class StreamUtil {

    private final static Logger log = Logger.getLogger(StreamUtil.class);

    private static boolean triedToInitNativeMD5 = false;

    public static String returnMD5ForFile(File input) {
        synchronized(StreamUtil.class) {
            if (!triedToInitNativeMD5) {
                boolean success = MD5.initNativeLibrary();
                log.debug("Native MD5 implementation library initialization success: "+success);
            }
        }
        try {
            String hash = MD5.asHex(MD5.getHash(input));
            if (log.isDebugEnabled())
                log.debug("Calculated hash for file " + input.getAbsolutePath() + " is " + hash);
            return hash;
        } catch (IOException e) {
            log.error("Failure while calculating MD5", e);
            throw new IllegalStateException(e);
        }
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

    public static Properties fetchPropertiesFromClasspath(String classpathEntry) throws IOException {
        return useClasspathResource(classpathEntry, new Function<InputStream, Properties>() {
            @Override
            public Properties apply(InputStream inputStream) {
                Properties props = new Properties();
                try {
                    props.load(inputStream);
                } catch (IOException e) {
                    AnyThrow.throwUncheked(e);
                }
                return props;
            }
        });
    }

    public static <T> T useClasspathResource(String classpathEntry, Function<InputStream, ? extends T> function) throws IOException {
        try (InputStream stream = StreamUtil.class.getResourceAsStream(classpathEntry)) {
            if (stream == null)
                throw new IOException("Resource not found: "+classpathEntry);
            return function.apply(stream);
        }
    }

}
