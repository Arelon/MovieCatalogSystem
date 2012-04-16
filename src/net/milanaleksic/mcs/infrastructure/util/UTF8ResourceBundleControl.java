package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.Optional;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.*;
import java.util.*;

public class UTF8ResourceBundleControl extends ResourceBundle.Control {

    private static class TryToGetViaUrl implements PrivilegedExceptionAction<Optional<InputStream>> {

        private ClassLoader loader;
        private boolean reload;
        private String resourceName;

        public TryToGetViaUrl(ClassLoader loader, boolean reload, String resourceName) {
            this.loader = loader;
            this.reload = reload;
            this.resourceName = resourceName;
        }

        public Optional<InputStream> run() throws IOException {
            Optional<InputStream> is = Optional.absent();
            if (reload) {
                Optional<URL> url = Optional.fromNullable(loader.getResource(resourceName));
                if (url.isPresent()) {
                    Optional<URLConnection> connection = Optional.fromNullable(url.get().openConnection());
                    if (connection.isPresent()) {
                        // Disable caches to get fresh data for
                        // reloading.
                        connection.get().setUseCaches(false);
                        is = Optional.of(connection.get().getInputStream());
                    }
                }
            } else {
                is = Optional.fromNullable(loader.getResourceAsStream(resourceName));
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
            Optional<InputStream> stream;
            try {
                stream = AccessController.doPrivileged(new TryToGetViaUrl(loader, reload, toResourceName(bundleName, "properties")));
            } catch (PrivilegedActionException e) {
                throw (IOException) e.getException();
            }
            if (stream.isPresent()) {
                try {
                    // the only changed line from default implementation:
                    return new PropertyResourceBundle(new InputStreamReader(stream.get(), StreamUtil.UTF8));
                } finally {
                    stream.get().close();
                }
            }
        }
        return super.newBundle(baseName, locale, format, loader, reload);
    }

}

