package org.blip.ms;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;

import java.io.*;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Copy of basically underscore type libraries and convience functions.
 *
 */
public class $ {

    private static String hostname;

    /**
     * Precache common lookups for performance.
     */
    static {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            // nothing to do here
            hostname = "unknown";
        }
    }

    /**
     * Returns the hostname for this current machine. Please note
     * this value is cached and will not update if the hostname changes
     * without a restart.
     * @return
     */
    public static String getHostname() {
        return hostname;
    }

    /**
     * Takes the first non null value.
     *
     * @param args
     *            variable number of string values to coalesce
     * @return The first non-null value.
     */
    public static String coalesce(String... args) {
        for (int i = 0; i < args.length; i++) {
            if (StringUtils.isNotBlank(args[i])) {
                return args[i];
            }
        }
        return null;
    }

    /**
     * Attempts a thread sleep, useful for tests and to avoid
     * capturing the exception.
     *
     * @param ms The time to sleep in milliseconds
     */
    public static void trySleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Converts the text based version of the numeric text value to a long
     * in the most optimal and fastest way. Returning null if it was not
     * possible to compute the text into a long.
     *
     * @param v the numeric value as a String
     *
     * @return The value of the UPC in long format, or null.
     */
    public static final Long tryLong(String v) {
        if (StringUtils.isBlank(v)) return null;
        try {
            return Long.parseLong(v.trim());
        } catch (Throwable ex) {
            return null;
        }
    }

    private static final Random r = new SecureRandom();

    /**
     * Random number between the values specified.
     *
     * @param min Min value
     * @param max Max Value
     * @return A random number between the values specified.
     */
    public static final int randomNumber(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        return r.nextInt((max - min) + 1) + min;
    }

    /**
     * Compress the specified string using GZIP.
     * @param str The string to compress
     * @return Returns the bytes of the string compressed
     * @throws IOException If the gzip could not occur.
     */
    public static byte[] gzip(final String str) throws IOException {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }

        try (ByteArrayOutputStream obj = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
                gzip.write(str.getBytes("UTF-8"));
                gzip.flush();
                gzip.close();
                return obj.toByteArray();
            }
        }
    }

    /**
     * Compress the specified string using GZIP.
     * @param bits the byte array
     * @return Returns the bytes of the string compressed
     * @throws IOException If the gzip could not occur.
     */
    public static byte[] gzip(final byte[] bits) throws IOException {
        if (bits == null || bits.length <=0) {
            return bits;
        }

        try (ByteArrayOutputStream obj = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
                gzip.write(bits);
                gzip.flush();
                return obj.toByteArray();
            }
        }
    }

    /**
     * Uncompresses the bytes assuming gzip compression was the original algorithim chosen.
     *
     * @param bits The bits to compress
     * @return Returns the string uncompressed.
     * @throws IOException If the un-gzip operation could not be completed.
     */
    public static String gunzip(final byte[] bits) throws IOException {
        if (bits == null) return null;
        if (bits.length <= 0) return null;

        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bits))) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(gzip, "UTF-8"))) {
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                return sb.toString();
            } finally {
                try {
                    gzip.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
        }

    }

    /**
     * Encodes the passed String as UTF-8 using an algorithm that's compatible
     * with JavaScript's <code>encodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s
     *            The String to be encoded
     * @return the encoded String
     */
    public static String encodeURIComponent(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8")
                            .replaceAll("\\+", "%20")
                            .replaceAll("\\%21", "!")
                            .replaceAll("\\%27", "'")
                            .replaceAll("\\%28", "(")
                            .replaceAll("\\%29", ")")
                            .replaceAll("\\%7E", "~");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    /**
     * Decodes the specified URL back into plain format.
     * @param s The string to encode
     * @return The decoded component
     */
    public static String decodeURIComponent(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns promise/deferred object for cross thread notification and async processing.
     */
    public static <D, F, P> DeferredObject<D, F, P> promise() {
        return new Async<>();
    }

    /**
     * Fixes issue with deferred library where notify was being performed on a synchronized(this) call
     * slowing performance needlessly.
     */
    private static class Async<D, F, P> extends DeferredObject<D, F, P> {
        /**
         * {@inheritDoc}
         */
        @Override
        public Deferred<D, F, P> notify(final P progress) {
            triggerProgress(progress);
            return this;
        }
    }

}