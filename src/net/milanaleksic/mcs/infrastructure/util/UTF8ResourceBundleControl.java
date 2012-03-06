package net.milanaleksic.mcs.infrastructure.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.*;
import java.util.*;

public class UTF8ResourceBundleControl extends ResourceBundle.Control {

    private static class TryToGetViaUrl implements PrivilegedExceptionAction<InputStream> {

        private ClassLoader loader;
        private boolean reload;
        private String resourceName;

        public TryToGetViaUrl(ClassLoader loader, boolean reload, String resourceName) {
            this.loader = loader;
            this.reload = reload;
            this.resourceName = resourceName;
        }

        public InputStream run() throws IOException {
            InputStream is = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        // Disable caches to get fresh data for
                        // reloading.
                        connection.setUseCaches(false);
                        is = connection.getInputStream();
                    }
                }
            } else {
                is = loader.getResourceAsStream(resourceName);
            }
            return is;
        }
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                    ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        String bundleName = toBundleName(baseName, locale);
        if (format.equals("java.properties")) {
            InputStream stream;
            try {
                stream = AccessController.doPrivileged(new TryToGetViaUrl(loader, reload, toResourceName(bundleName, "properties")));
            } catch (PrivilegedActionException e) {
                throw (IOException) e.getException();
            }
            if (stream != null) {
                try {
                    // the only changed line from default implementation:
                    return new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
        }
        return super.newBundle(baseName, locale, format, loader, reload);
    }

}

