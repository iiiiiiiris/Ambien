package me.iris.ambien.obfuscator.settings;

import com.google.gson.*;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.data.implementations.ListSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.settings.data.Setting;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.utilities.StringUtil;

import java.io.*;

public class Settings {
    public static void create() throws IOException {
        // Create file to write to
        File outFile = new File("settings.json");
        if (outFile.exists())
            outFile = new File("settings-" + StringUtil.randomString(7) + ".json");

        // Global object everything will be stored in
        final JsonObject obj = new JsonObject();

        // Add current obfuscator version to json
        obj.addProperty("version", Ambien.VERSION);

        // Add string properties for input jar & output path
        obj.addProperty("input", "somejar.jar");
        obj.addProperty("output", "somejar-obfuscated.jar");
        obj.add("libraries", new JsonArray());
        obj.add("exclusions", new JsonArray());
        obj.addProperty("remove-exclude-annotations", true);

        // Array all transformers will be in
        final JsonArray transformersArr = new JsonArray();

        // Add transformer stuff to transformers array
        for (Transformer transformer : Ambien.get.transformerManager.getTransformers()) {
            final JsonObject transformerObj = new JsonObject();
            transformerObj.addProperty("name", transformer.getName());

            for (Setting<?> setting : transformer.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    if (setting.getName().equals("enabled"))
                        transformerObj.addProperty(setting.getName(), transformer.isEnabledByDefault());
                    else
                        transformerObj.addProperty(setting.getName(), ((BooleanSetting)setting).isEnabled());
                } else if (setting instanceof StringSetting)
                    transformerObj.addProperty(setting.getName(), ((StringSetting)setting).getValue());
                else if (setting instanceof NumberSetting)
                    transformerObj.addProperty(setting.getName(), ((NumberSetting<?>)setting).getValue());
                else if (setting instanceof ListSetting)
                    transformerObj.add(setting.getName(), new JsonArray());
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
        final FileWriter writer = new FileWriter(outFile);
        writer.write(jsonStr);
        writer.flush();
        writer.close();

        // Feedback
        Ambien.LOGGER.info("Created default config.");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void load(final File file, final boolean experimentalTransformers) throws IOException {
        // Parse file
        final JsonObject obj = JsonParser.parseReader(new BufferedReader(new FileReader(file))).getAsJsonObject();

        // Check versions
        if (!obj.get("version").getAsString().equals(Ambien.VERSION))
            Ambien.LOGGER.warn("Settings file was made for a different version of Ambien, some features may have changed or don't exist anymore.");

        // Set input & output jar strings
        Ambien.get.inputJar = obj.get("input").getAsString();
        Ambien.get.outputJar = obj.get("output").getAsString();

        // Get libraries
        final JsonArray libraries = obj.get("libraries").getAsJsonArray();
        for (int i = 0; i < libraries.size(); i++) {
            final String libraryPath = libraries.get(i).getAsString();
            Ambien.get.libraries.add(libraryPath);
            Ambien.LOGGER.info("Added path for library: {}", libraryPath);
        }

        // Get excluded classes
        final JsonArray exclusionArray = obj.get("exclusions").getAsJsonArray();
        for (int i = 0; i < exclusionArray.size(); i++) {
            final String exclusion = exclusionArray.get(i).getAsString();
            Ambien.get.excludedClasses.add(exclusion);
            Ambien.LOGGER.info("Added to exclusion list: {}", exclusion);
        }

        // Set remove exclude annotations setting
        Ambien.get.removeExcludeAnnotations = obj.get("remove-exclude-annotations").getAsBoolean();

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

            // Warn if the transformer is experimental
            if (transformer.isEnabled() && transformer.getStability().equals(Stability.EXPERIMENTAL) && !experimentalTransformers) {
                Ambien.LOGGER.warn("Ignoring enabled transformer \"{}\" because Ambien was ran without the experimental flag.", transformer.getName());
                continue;
            }

            // Assume rest of the entries are settings of the transformer
            for (Setting<?> setting : transformer.getSettings()) {
                final JsonElement element = transformerObj.get(setting.getName());
                try {
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
                    } else if (setting instanceof ListSetting) {
                        final ListSetting listSetting = (ListSetting)setting;

                        // Add options
                        final JsonArray array = element.getAsJsonArray();
                        for (int j = 0; j < array.size(); j++) {
                            // Clear settings for first use
                            if (!listSetting.isOptionsCleared()) {
                                listSetting.getOptions().clear();
                                listSetting.setOptionsCleared(true);
                            }

                            // Add option
                            listSetting.getOptions().add(array.get(j).getAsString());
                        }
                    }
                } catch (NullPointerException e) {
                    Ambien.LOGGER.error("Setting \"{}\" not found for transformer {}", setting.getName(), transformerName);
                }
            }
        }

        // Feedback
        Ambien.LOGGER.info("Loaded config \"{}\"", file.getAbsolutePath());
    }
}
