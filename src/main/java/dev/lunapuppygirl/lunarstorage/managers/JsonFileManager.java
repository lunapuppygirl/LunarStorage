package dev.lunapuppygirl.lunarstorage.managers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JsonFileManager {
    @Getter
    private final File configFile = new File("data", "config.json");
    @Getter
    private final File statsFile = new File("data", "stats.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger = LoggerFactory.getLogger(JsonFileManager.class);

    @Getter
    private final ConcurrentHashMap<File, JsonObject> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void setup() {
        int resourceConfigVersion = 0;

        Map<File, String> resourceNames = Map.of(
                configFile, "data/config.json",
                statsFile, "data/stats.json"
        );

        for (File resource : resourceNames.keySet()) {
            String resourceName = resourceNames.get(resource);

            File parent = resource.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                if (is == null) {
                    logger.warn("Bundled default resource not found on classpath: {}", resourceName);
                    continue;
                }

                byte[] bytes = is.readAllBytes();

                if (resource.equals(configFile)) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {
                        JsonObject object = gson.fromJson(reader, JsonObject.class);
                        if (object != null && object.has("version")) {
                            resourceConfigVersion = object.get("version").getAsInt();
                        }
                    }
                }

                if (!resource.exists()) {
                    logger.info("copied {}", resourceName);
                    Files.write(resource.toPath(), bytes);
                }
            } catch (IOException e) {
                logger.error("Failed to copy resource '{}' from classpath.", resourceName, e);
            }
        }

        JsonObject config = loadJson(configFile);
        if (config != null) {
            try {
                int version = config.has("version") ? config.get("version").getAsInt() : -1;

                logger.debug("config ver: {}", version);
                logger.debug("resource config ver: {}", resourceConfigVersion);

                if (resourceConfigVersion > version) {
                    logger.warn("Your current config file is outdated!");
                } else if (resourceConfigVersion < version) {
                    logger.warn("Your current config version is newer than latest config file?");
                }
            } catch (Exception e) {
                logger.error("Failed to read config version!", e);
            }

            cache.put(configFile, config);
        } else {
            logger.error("Config file failed to load!");
        }

        loadStats();
    }

    @PreDestroy
    public void onExit() {
        saveStats();
        saveConfig();
    }

    public @Nullable JsonObject loadJson(File file) {
        try (JsonReader reader = new JsonReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            logger.error("Failed to read json file: {}", file, e);
            return null;
        }
    }

    public @Nullable JsonObject loadJson(InputStream is) {
        try (JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            logger.error("Failed to read json file from stream!", e);
            return null;
        }
    }

    public void loadConfig() {
        JsonObject json = loadJson(configFile);
        if (json != null) {
            cache.put(configFile, json);
        } else {
            logger.warn("loadConfig() failed; keeping previous cached value (if any).");
        }
    }

    public void loadStats() {
        JsonObject json = loadJson(statsFile);
        if (json != null) {
            cache.put(statsFile, json);
        } else {
            logger.warn("loadStats() failed; keeping previous cached value (if any).");
        }
    }



    public JsonElement get(String path) {
        return get(path, configFile);
    }

    public JsonElement get(String path, File file) {
        JsonElement element = cache.get(file);
        if (element == null) {
            logger.warn("No cached data for file: {}", file);
            return null;
        }

        for (String part : path.split("\\.")) {
            if (!element.isJsonObject()) return null;
            JsonElement next = element.getAsJsonObject().get(part);
            if (next == null) return null;
            element = next;
        }

        return element;
    }

    public <K, V> Map<K, V> getMap(String path, File file, Class<K> keyType, Class<V> valueType) {
        JsonElement element = get(path, file);

        if (element == null || !element.isJsonObject()) {
            if (element != null) {
                logger.warn("Path '{}' in {} is not a JSON object; cannot load as map.", path, file);
            }
            return new LinkedHashMap<>();
        }

        Type mapType = TypeToken.getParameterized(LinkedHashMap.class, keyType, valueType).getType();

        try {
            Map<K, V> result = gson.fromJson(element, mapType);
            return result != null ? result : new LinkedHashMap<>();
        } catch (JsonSyntaxException e) {
            logger.error("Failed to deserialize '{}' in {} as Map<{}, {}>",
                    path, file, keyType.getSimpleName(), valueType.getSimpleName(), e);
            return new LinkedHashMap<>();
        }
    }

    public boolean getBoolean(String path, File file, boolean defaultValue) {
        JsonElement element = get(path, file);
        return (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean())
                ? element.getAsBoolean()
                : defaultValue;
    }

    public String getString(String path, File file, String defaultValue) {
        JsonElement element = get(path, file);
        return (element != null && element.isJsonPrimitive())
                ? element.getAsString()
                : defaultValue;
    }

    public int getInt(String path, File file, int defaultValue) {
        JsonElement element = get(path, file);
        return (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber())
                ? element.getAsInt()
                : defaultValue;
    }

    public long getLong(String path, File file, long defaultValue) {
        JsonElement element = get(path, file);
        return (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber())
                ? element.getAsLong()
                : defaultValue;
    }

    public double getDouble(String path, File file, double defaultValue) {
        JsonElement element = get(path, file);
        return (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber())
                ? element.getAsDouble()
                : defaultValue;
    }

    public float getFloat(String path, File file, float defaultValue) {
        JsonElement element = get(path, file);
        return (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber())
                ? element.getAsFloat()
                : defaultValue;
    }

    public char getChar(String path, File file, char defaultValue) {
        JsonElement element = get(path, file);
        if (element == null || !element.isJsonPrimitive()) return defaultValue;
        String s = element.getAsString();
        return s.isEmpty() ? defaultValue : s.charAt(0);
    }



    public void set(String path, JsonElement value, File file) {
        JsonObject root = cache.computeIfAbsent(file, f -> new JsonObject());

        String[] parts = path.split("\\.");
        JsonObject current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement next = current.get(parts[i]);
            if (next == null || !next.isJsonObject()) {
                JsonObject newObj = new JsonObject();
                current.add(parts[i], newObj);
                current = newObj;
            } else {
                current = next.getAsJsonObject();
            }
        }

        current.add(parts[parts.length - 1], value);
    }

    public void setBoolean(String path, boolean value, File file) {
        set(path, new JsonPrimitive(value), file);
    }

    public void setString(String path, String value, File file) {
        set(path, new JsonPrimitive(value), file);
    }

    public void setInt(String path, int value, File file) {
        set(path, new JsonPrimitive(value), file);
    }

    public void setLong(String path, long value, File file) {
        set(path, new JsonPrimitive(value), file);
    }

    public void setDouble(String path, double value, File file) {
        set(path, new JsonPrimitive(value), file);
    }

    public void setFloat(String path, float value, File file) {
        set(path, new JsonPrimitive(value), file);
    }



    public void save(File file, JsonObject json) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                gson.toJson(json, writer);
            }

            cache.put(file, json);
        } catch (IOException e) {
            logger.error("Failed to save json file: {}", file, e);
        }
    }

    public void saveConfig() {
        JsonObject json = cache.get(configFile);
        if (json == null) {
            logger.warn("saveConfig() called but nothing is cached for configFile.");
            return;
        }
        save(configFile, json);
    }

    public void saveConfig(JsonObject json) {
        save(configFile, json);
    }

    public void saveStats() {
        JsonObject json = cache.get(statsFile);
        if (json == null) {
            logger.warn("saveStats() called but nothing is cached for statsFile.");
            return;
        }
        save(statsFile, json);
    }

    public void saveStats(JsonObject json) {
        save(statsFile, json);
    }
}