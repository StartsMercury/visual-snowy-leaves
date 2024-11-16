package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Config;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.Chunks;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class VisualSnowyLeavesImpl {
    private Config config;

    private final FabricLoader fabricLoader;

    private final Logger logger;

    private final Minecraft minecraft;

    public VisualSnowyLeavesImpl(final Minecraft minecraft) {
        this.config = Config.DEFAULT;
        this.fabricLoader = FabricLoader.getInstance();
        this.logger = LoggerFactory.getLogger(VslConstants.NAME);
        this.minecraft = minecraft;

        this.logger.info("{} is initialized!", VslConstants.NAME);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Config getConfig() {
        return this.config;
    }

    public void setConfig(final Config config) {
        final var oldConfig = this.config;
        this.config = config;

        //noinspection ConstantValue
        if (
            !oldConfig.targetBlockKeys().equals(config.targetBlockKeys())
                // May be true when too early in construction injection
                && this.minecraft.getResourceManager() != null
        ) {
            this.logger.debug(
                "[{}] Reloading resource packs to modify sprite changes...",
                VslConstants.NAME
            );

            this.minecraft.reloadResourcePacks();
        }

        final var level = this.minecraft.level;
        if (level == null) {
            this.logger.debug(
                "[{}] Skipping level snowy ticker since there is no level...",
                VslConstants.NAME
            );
            return;
        }

        if (oldConfig.transitionDuration() != config.transitionDuration()) {
            this.logger.debug(
                "[{}] Normalizing snowy progress using ratio and proportion...",
                VslConstants.NAME
            );

            ((SnowDataAware) level).visual_snowy_leaves$getSnowData().onTransitionDurationChange(
                oldConfig.transitionDuration().asTicks(),
                config.transitionDuration().asTicks()
            );
        }

        if (oldConfig.snowyMode() != config.snowyMode()) {
            this.logger.debug(
                "[{}] Snowing mode changed, requesting lazy rebuild to all chunks...",
                VslConstants.NAME
            );

            Chunks.requestRebuildAll(level);
        }
    }

    public void openConfigFile() {
        this.logger.debug("[{}] Opening config...", VslConstants.NAME);
        Util.getPlatform().openFile(this.getConfigFile());
    }

    public void reloadConfig() {
        if (this.loadConfig()) {
            this.saveConfig();
        } else {
            this.openConfigFile();
        }
    }

    private boolean loadConfig() {
        this.logger.debug("[{}] Loading config...", VslConstants.NAME);

        final var path = this.getConfigPath();
        final JsonElement json;

        final ArrayList<CharSequence> lines;
        try (final var lineStream = Files.lines(path)) {
            lines = lineStream
                .filter(line -> !line.endsWith(VslConstants.IGNORE_TAG))
                .collect(Collectors.toCollection(ArrayList::new));
        } catch (final IOException cause) {
            this.logger.warn("[{}] Unable to read config json", VslConstants.NAME, cause);
            return false;
        }

        try{
            json = JsonParser.parseString(String.join("\n", lines));
        } catch (final JsonParseException cause) {
            this.logger.warn("[{}] Invalid config json syntax", VslConstants.NAME, cause);

            final var lineMatcher = VslConstants.LINE_PATTERN.matcher(cause.getMessage());
            var line = 0;

            if (lineMatcher.find()) {
                final var capturedLine = lineMatcher.group(1);
                try {
                    line = Integer.parseInt(capturedLine);
                } catch (final NumberFormatException ignored) {

                }
            }


            final var errorMessageBuilder = new StringBuilder();

            final var columnMatcher = VslConstants.COLUMN_PATTERN.matcher(cause.getMessage());

            if (columnMatcher.find()) {
                try {
                    final var capturedColumn = columnMatcher.group(1);
                    final var column = Integer.parseInt(capturedColumn);

                    if (column >= 2) {
                        errorMessageBuilder.append(" ".repeat(column - 2));
                    }

                    errorMessageBuilder.append("^ ");
                } catch (final NumberFormatException ignored) {

                }
            }

            errorMessageBuilder.append(cause.getMessage())
                .append("\t")
                .append(VslConstants.IGNORE_TAG);
            lines.add(line, errorMessageBuilder);

            try {
                Files.write(path, lines);
            } catch (final IOException cause2) {
                this.logger.warn(
                    "[{}] Unable to update config json with an error message",
                    VslConstants.NAME,
                    cause2
                );
            }

            return false;
        }

        Config.CODEC
            .decode(JsonOps.INSTANCE, json)
            .get()
            .ifLeft(result -> this.setConfig(result.getFirst()))
            .ifRight(result -> this.logger
                .warn("[{}] Unable to decode config: {}", VslConstants.NAME, result.message())
            );

        return true;
    }

    private void saveConfig() {
        this.logger.debug("[{}] Saving config...", VslConstants.NAME);

        final var path = this.fabricLoader.getConfigDir().resolve(VslConstants.CONFIG_NAME);
        final var json = (JsonObject) Config.CODEC.encodeStart(JsonOps.INSTANCE, this.config)
            .getOrThrow(false, cause -> {
                this.logger.warn("[{}] Unable to encode config: {}", VslConstants.NAME, cause);
            });

        json.addProperty("__message", "Click the config button again to load changes.");

        try (
            final var bufferedWriter = Files.newBufferedWriter(path);
            final var jsonWriter = new JsonWriter(bufferedWriter)
        ) {
            jsonWriter.setIndent("    ");

            GsonHelper.writeValue(jsonWriter, json, Comparator.naturalOrder());

            bufferedWriter.newLine();
        } catch (final IOException cause) {
            this.logger.warn("[{}] Unable to write config json", VslConstants.NAME, cause);
        }
    }

    private Path getConfigPath() {
        return this.fabricLoader.getConfigDir().resolve(VslConstants.CONFIG_NAME);
    }

    public File getConfigFile() {
        return this.getConfigPath().toFile();
    }
}
