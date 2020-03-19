package org.blip.ms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.SimpleDateFormat;

public class Objects {

    /**
     * Converts the specified object to a byte array, ensuring any streams and closed and disposed of.
     *
     * @param o The object to serialize to a native java byte array.
     * @return Returns the byte array of the specified object.
     */
    public static <T> byte[] toBytes(T o) {
        try (ByteArrayOutputStream bs = new ByteArrayOutputStream()) {
            try (ObjectOutputStream os = new ObjectOutputStream(bs)) {
                os.writeObject(o);
                return bs.toByteArray();
            } catch (Exception ex) {
                throw new RuntimeException("Unable to serialize object to byte[] array back, check inner exception for details.", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the specified object to a byte array, ensuring any streams and closed and disposed of.
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(byte[] bits, Class<T> clazz) {
        ObjectOutputStream os = null;
        try (ByteArrayInputStream bs = new ByteArrayInputStream(bits)) {
            ObjectInputStream o = new ObjectInputStream(bs);
            Object obj = o.readObject();
            if (obj == null) {
                return null;
            }
            return (T)obj;
        } catch (Exception ex) {
            throw new RuntimeException("Unable to deserialize byte[] array back from object, check inner exception for details.", ex);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    /**
     * Convert the specified JSON to an object, keeping any polymorphic
     * properties and configuration settings so that polymorphic classes can be
     * used. See <JsonTypeInfo> for more information on how to work with
     * polymorhpic json objects.
     *
     * @param json The json string
     * @param type The type to which to deserialize
     * @return The JSON string for the specified object.
     */
    public static <T extends Object> T fromJson(final String json, final Class<T> type) {
        if (type.isAssignableFrom(IndexedRecord.class)) {
            T o = gson.fromJson(json, type);
            return o;
        }
        try {
            return mapper.readValue(json, type);
        } catch (Exception ex) {
            if (ex instanceof JsonMappingException) {  // fallback to JSON using GSON as we likely have a collection of avro docs
                T o = gson.fromJson(json, type);
                return o;
            }
            throw new RuntimeException("Error converting to json", ex);
        }
    }

    /**
     * Convert the specified object to JSON, keeping any polymorphic properties
     * and configuration settings. See <JsonTypeInfo> for more information on
     * how to work with polymorhpic json objects.
     *
     * @param obj
     *            The object to convert
     * @return The JSON string for the specified object.
     */
    public static String toJsonPrettyFormat(final Object obj) {
        if (obj == null)
            return null;
        // avro serializer
        if (obj instanceof IndexedRecord) {
            try {
                return gson_pretty_format.toJson(obj);
            } catch (Exception e) {
                throw new RuntimeException("Error avro to json", e);
            }
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            if (e instanceof JsonMappingException) {  // fallback to JSON using GSON as we likely have a collection of avro docs
                try {
                    String o = gson.toJson(obj);
                    return o;
                } catch (Throwable ex) {
                    // ignore our fallback fail
                }
            }
            throw new RuntimeException("Error converting to json", e);
        }
    }

    /**
     * Given the specified object, serialize it to JSON using a appendable output stream for highest performance.
     * @param obj The object to serialize
     * @param schema The schema to use for serialization
     * @param os The output stream to write to
     * @throws IOException An IO exception if we could not serialize the object.
     */
    public static void toJsonStream(final Object obj, final Schema schema, final OutputStreamWriter os) throws IOException {
        if (obj == null) {
            os.append("{}");
            return;
        }
        gson.toJson(obj, os);
    }

    /**
     * Convert the specified object to JSON, keeping any polymorphic properties
     * and configuration settings. See <JsonTypeInfo> for more information on
     * how to work with polymorhpic json objects.
     *
     * @param obj
     *            The object to convert
     * @return The JSON string for the specified object.
     */
    public static String toJson(final Object obj) {
        if (obj == null) {
            return null;
        }
        // avro serializer
        if (obj instanceof IndexedRecord) {
            try {
                return gson.toJson(obj);
            } catch (Exception e) {
                throw new RuntimeException("Error avro to json", e);
            }
        }
        // default serializer
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            try {
                return gson.toJson(obj);
            } catch (Throwable ex) {
                // ignore... our fallback failed
            }
            throw new RuntimeException("Error converting to json", e);
        }
    }

    private static final ObjectMapper mapper = JsonFactory.build();
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().create();
    private static final Gson gson_pretty_format = new GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().setPrettyPrinting().create();

    private static class JsonFactory {
        public static ObjectMapper build() {
            ObjectMapper o = new ObjectMapper();
            // ISO3 format for JSON documents
            o = o.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
            // serialization defaults to not include nulls for bandwidth and parsing times
            o = o.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return o;
        }
    }
}
