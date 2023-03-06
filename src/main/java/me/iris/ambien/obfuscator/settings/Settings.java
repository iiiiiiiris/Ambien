package me.iris.ambien.obfuscator.settings;

import com.google.gson.*;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.settings.data.Setting;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;

import java.io.*;

public class Settings {
    private static final File DEFAULT_SETTINGS_FILE = new File("settings.json");

    public static void create() throws IOException {
        // Make sure the default settings file doesn't already exist
        if (DEFAULT_SETTINGS_FILE.exists()) {
            Ambien.LOGGER.error("Default settings file already exists.");
            return;
        }

        // Global object everything will be stored in
        final JsonObject obj = new JsonObject();

        // Add current obfuscator version to json
        obj.addProperty("version", Ambien.VERSION);

        // Add string properties for input jar & output path
        obj.addProperty("input", "somejar.jar");
        obj.addProperty("output", "somejar-obfuscated.jar");
        obj.add("exclusions", new JsonArray());

        // Array all transformers will be in
        final JsonArray transformersArr = new JsonArray();

        // Add transformer stuff to transformers array
        for (Transformer transformer : Ambien.get.transformerManager.getTransformers()) {
            final JsonObject transformerObj = new JsonObject();
            transformerObj.addProperty("name", transformer.getName());

            for (Setting<?> setting : transformer.getSettings()) {
                if (setting instanceof BooleanSetting)
                    transformerObj.addProperty(setting.getName(), ((BooleanSetting)setting).isEnabled());
                else if (setting instanceof StringSetting)
                    transformerObj.addProperty(setting.getName(), ((StringSetting)setting).getValue());
                else if (setting instanceof NumberSetting)
                    transformerObj.addProperty(setting.getName(), ((NumberSetting<?>)setting).getValue());
            }

            // Add transformer object to transformers array
            transformersArr.add(transformerObj);
        }

        // Add transformers array to global object
        obj.add("transformers", transformersArr);

        // Create gson w/ pretty printing
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convert json object to a string
        final String jsonStr = gson.toJson(obj);

        // Write to file
        final FileWriter writer = new FileWriter(DEFAULT_SETTINGS_FILE);
        writer.write(jsonStr);
        writer.flush();
        writer.close();

        // Feedback
        Ambien.LOGGER.info("Created default config.");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void load(final File file) throws IOException {
        // Parse file
        final JsonObject obj = JsonParser.parseReader(new BufferedReader(new FileReader(file))).getAsJsonObject();

        // Check versions
        if (!obj.get("version").getAsString().equals(Ambien.VERSION))
            Ambien.LOGGER.warn("Settings file was made for a different version of Ambien, some features may have changed or don't exist anymore.");

        // Set input & output jar strings
        Ambien.get.inputJar = obj.get("input").getAsString();
        Ambien.get.outputJar = obj.get("output").getAsString();

        // Get excluded classes
        final JsonArray exclusionArray = obj.get("exclusions").getAsJsonArray();
        for (int i = 0; i < exclusionArray.size(); i++) {
            final String exclusion = exclusionArray.get(i).getAsString();
            Ambien.get.excludedClasses.add(exclusion);
            Ambien.LOGGER.info("Added to exclusion list: {}", exclusion);
        }

        // Get transformers array
        final JsonArray transformersArr = obj.get("transformers").getAsJsonArray();

        // Enumerate through transformer entries
        for (int i = 0; i < transformersArr.size(); i++) {
            // Get entry object
            final JsonObject transformerObj = transformersArr.get(i).getAsJsonObject();

            // Get transformer from name
            final String transformerName = transformerObj.get("name").getAsString();
            final Transformer transformer = Ambien.get.transformerManager.getTransformer(transformerName);
            if (transformer == null) {
                Ambien.LOGGER.warn("Could not find transformer with the name \"{}\".", transformerName);
                continue;
            }

            // Set enabled state
            transformer.setEnabled(transformerObj.get("enabled").getAsBoolean());

            // Assume rest of the entries are settings of the transformer
            for (Setting<?> setting : transformer.getSettings()) {
                final JsonElement element = transformerObj.get(setting.getName());
                if (setting instanceof BooleanSetting)
                    ((BooleanSetting)setting).setEnabled(element.getAsBoolean());
                else if (setting instanceof StringSetting)
                    ((StringSetting)setting).setValue(element.getAsString());
                else if (setting instanceof NumberSetting) {
                    final NumberSetting numberSetting = (NumberSetting)setting;
                    if (numberSetting.getValue() instanceof Integer)
                        numberSetting.setValue(element.getAsInt());
                    else if (numberSetting.getValue() instanceof Long)
                        numberSetting.setValue(element.getAsLong());
                    else if (numberSetting.getValue() instanceof Float)
                        numberSetting.setValue(element.getAsFloat());
                    else if (numberSetting.getValue() instanceof Double)
                        numberSetting.setValue(element.getAsDouble());
                }
            }
        }

        // Feedback
        Ambien.LOGGER.info("Loaded config \"{}\"", file.getAbsolutePath());
    }
}
