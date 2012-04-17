package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.*;
import com.google.common.io.ByteStreams;
import com.twmacinta.util.MD5;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URI;
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

    static {
         MD5.initNativeLibrary();
    }

    public static String returnMD5ForFile(File input) {
        try {
            String hash = MD5.asHex(MD5.getHash(input));
            if (log.isDebugEnabled())
                log.debug("Calculated hash for file " + input.getAbsolutePath() + " is " + hash); //NON-NLS
            return hash;
        } catch (IOException e) {
            log.error("Failure while calculating MD5", e); //NON-NLS
            throw new IllegalStateException(e);
        }
    }

    public static void writeFileToZipStream(ZipOutputStream zos, String fileName, String entryName) throws IOException {
        Optional<FileInputStream> fis = Optional.absent();
        try {
            fis = Optional.of(new FileInputStream(fileName));
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            ByteStreams.copy(fis.get(), zos);
        } finally {
            if (fis.isPresent()) try {
                fis.get().close();
            } catch (Throwable ignored) {
            }
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

    public static <T> T useURIResource(URI uri, PersistentHttpContext persistentHttpContext, Function<InputStream, ? extends T> function) throws IOException {
        try (InputStream stream = new BufferedInputStream(persistentHttpContext.execute(new HttpGet(uri)).getEntity().getContent())) {
            if (stream == null)
                throw new IOException("URI Resource null: "+uri);
            return function.apply(stream);
        }
    }

}
