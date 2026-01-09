package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Config;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowProgressAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.screens.ConfigScreen;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.Chunks;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.Reporter;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VisualSnowyLeavesImpl {
    @Nullable
    private static GpuBuffer snowProgress;

    public static void setSnowProgress(final GpuBuffer buffer) {
        snowProgress = buffer;
    }

    @Nullable
    public static GpuBuffer getSnowProgress() {
        return snowProgress;
    }

    private Config config;

    private final FabricLoader fabricLoader;

    private final Logger logger;

    private final Minecraft minecraft;

    private final Reporter<Class<? extends BlockStateModel.Unbaked>> blockStateModelReporter;

    private final Reporter<Class<? extends UnbakedGeometry>> geometryReporter;

    private final KeyMapping keyOpenModConfig;

    private @Nullable Path reportFile;

    public VisualSnowyLeavesImpl(final Minecraft minecraft) {
        this.config = Config.DEFAULT;
        this.fabricLoader = FabricLoader.getInstance();
        this.logger = LoggerFactory.getLogger(VslConstants.NAME);
        this.minecraft = minecraft;

        final Function<? super Class<?>, String> classFormatter = Class::getName;
        this.blockStateModelReporter = new Reporter<>(new ReferenceOpenHashSet<>(), classFormatter);
        this.geometryReporter = new Reporter<>(new ReferenceOpenHashSet<>(), classFormatter);

        this.keyOpenModConfig = new KeyMapping(
            "visual-snowy-leaves.key.openModConfig",
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.MISC
        );

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

        if (!Objects.equals(oldConfig.transitionDuration(), config.transitionDuration())
            || !Objects.equals(oldConfig.freshFreezeDelay(), config.freshFreezeDelay())
            || !Objects.equals(oldConfig.fullMeltDelay(), config.fullMeltDelay())
        ) {
            this.logger.debug(
                "[{}] Normalizing snowy progress using ratio and proportion...",
                VslConstants.NAME
            );

            ((SnowProgressAware) level).visual_snowy_leaves$getSnowProgress().update(config);
        }

        final boolean filterChanged = oldConfig.disabled()
            // disabled -> enabled
            ? !config.disabled()
            // enabled -> disabled
            : config.disabled()
                // enabled -> enabled(M)
                || oldConfig.requireSnowyBiomes() != config.requireSnowyBiomes();

        if (filterChanged) {
            this.logger.debug(
                "[{}] Snowy conditions changed, requesting lazy rebuild to all chunks...",
                VslConstants.NAME
            );

            // `VslIndex` gets outdated when we toggle `requireSnowyBiomes`
            Chunks.requestRebuildAll(level);
        }
    }

    public void openModConfigFile() {
        this.logger.debug("[{}] Opening config...", VslConstants.NAME);
        Util.getPlatform().openFile(this.getConfigFile());
    }

    public void reloadConfig() {
        if (this.loadConfig()) {
            this.saveConfig();
        } else {
            this.openModConfigFile();
        }
    }

    /**
     * @return {@code false} if loading encountered json syntax exceptions;
     *     {@code true} otherwise.
     */
    private boolean loadConfig() {
        this.logger.debug("[{}] Loading config...", VslConstants.NAME);

        final var path = this.getConfigPath();
        final JsonElement json;

        final ArrayList<CharSequence> lines;
        try (final var lineStream = Files.lines(path)) {
            lines = lineStream
                .filter(line -> !line.endsWith(VslConstants.IGNORE_TAG))
                .collect(Collectors.toCollection(ArrayList::new));
        } catch (final NoSuchFileException cause) {
            this.logger.info("[{}] Config does not exist, using default", VslConstants.NAME);
            return true;
        } catch (final IOException cause) {
            this.logger.warn("[{}] Unable to read config json", VslConstants.NAME, cause);
            return true;
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

        Config.LENIENT_CODEC
            .decode(JsonOps.INSTANCE, json)
            .ifSuccess(result -> this.setConfig(result.getFirst().upgrade()))
            .ifError(result -> this.logger
                .warn("[{}] Unable to decode config: {}", VslConstants.NAME, result.message())
            );

        return true;
    }

    public void saveConfig() {
        this.logger.debug("[{}] Saving config...", VslConstants.NAME);

        final var path = this.fabricLoader.getConfigDir().resolve(VslConstants.CONFIG_NAME);

        final JsonObject json;
        switch (this.config.encodeAsJson()) {
            case DataResult.Success<JsonElement>(final var value, _):
                json = (JsonObject) value;
                break;
            case DataResult.Error<JsonElement>(final var messageSupplier, _, _):
                final var message = messageSupplier.get();
                this.logger.warn("[{}] Unable to encode config: {}", VslConstants.NAME, message);
                return;
        }

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

    public Reporter<Class<? extends BlockStateModel.Unbaked>> getBlockStateModelRecRep() {
        return this.blockStateModelReporter;
    }

    public Reporter<Class<? extends UnbakedGeometry>> getGeometryRecRep() {
        return this.geometryReporter;
    }

    public void collectReports(final SpriteWhitener whitener) {
        class PathBuf {
            @Nullable Path inner;
        }

        final PathBuf tempFile = new PathBuf();

        try (final var printWriter = whitener.collectReports(() -> {
            final Path path;
            try {
                path = Files.createTempFile(VslConstants.MODID, "reports.txt");

                this.logger.info("[{}] Created new temporary report file", VslConstants.NAME);

                final var template = "[{}] Created new temporary report file at {}";
                this.logger.debug(template, VslConstants.NAME, tempFile);
            } catch (final IOException cause) {
                throw new IOException("Unable to create temporary report file", cause);
            }

            final var writer = new PrintWriter(Files.newBufferedWriter(path));
            tempFile.inner = path;

            writer.print("Minecraft ");
            try {
                writer.println(SharedConstants.getCurrentVersion().name());
            } catch (final RuntimeException cause) {
                writer.print('<');
                writer.print(cause.getMessage());
                writer.println('>');
            }

            final var version = this
                .fabricLoader
                .getModContainer(VslConstants.MODID)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .map(Version::getFriendlyString)
                .orElse("<unknown>");
            writer.print(VslConstants.NAME);
            writer.print(' ');
            writer.println(version);

            writer.println();

            return writer;
        })) {
            if (printWriter == null) {
                final var template = "[{}] Skipping reporting, there is nothing to report";
                this.logger.info(template, VslConstants.NAME);
                return;
            } else {
                this.logger.info("[{}] Successfully wrote the temporary report", VslConstants.NAME);
                printWriter.println("Submit this to the dedicated Google Forms:");
                printWriter.println();
                for (var i = 0; i < 3; i++) {
                    printWriter.println("> https://forms.gle/UiwbEqhkTnrBC97C7");
                }
            }
        } catch (final IOException cause) {
            this.logger.error("[{}] Unable to write the temporary report", VslConstants.NAME, cause);
            return;
        }

        final var path = this
            .fabricLoader
            .getGameDir()
            .resolve("logs")
            .resolve(VslConstants.MODID)
            .resolve("reports.txt");

        try {
            final var loggingDirectory = path.getParent();
            Files.createDirectory(loggingDirectory);
            this.logger.info("[{}] Successfully created logging subdirectory", VslConstants.NAME);

            final var template = "[{}] Successfully created logging subdirectory at {}";
            this.logger.info(template, VslConstants.NAME, loggingDirectory);
        } catch (final FileAlreadyExistsException ignored) {
            this.logger.info("[{}] Logging subdirectory already exists", VslConstants.NAME);
        } catch (final IOException cause) {
            final var template = "[{}] Unable to create logging subdirectory";
            this.logger.error(template, VslConstants.NAME, cause);
            return;
        }

        try {
            assert tempFile.inner != null;
            Files.move(tempFile.inner, path, StandardCopyOption.REPLACE_EXISTING);
            this.logger.info("[{}] Successfully committed report file changes", VslConstants.NAME);
        } catch (final IOException cause) {
            this.logger.error("[{}] Unable to commit changes to report file", VslConstants.NAME, cause);
            return;
        }

        this.reportFile = path;
    }

    public boolean sendReportNotice() {
        final var reportFile = this.reportFile;
        if (reportFile == null) {
            return false;
        }

        final var player = this.minecraft.player;
        if (player == null) {
            return false;
        }

        final var underlined = Component.literal("Click this message to view reports.txt")
            .withStyle(arg -> arg.withUnderlined(true));
        final var message = Component.translatable(
            "["
                + VslConstants.NAME
                + "] Detected unsupported custom classes."
                + " Some models may fail to be snowy. "
        ).append(underlined)
            .withStyle(arg -> arg
                .applyFormat(ChatFormatting.RED)
                .withClickEvent(new ClickEvent.OpenFile(reportFile))
            );
        player.displayClientMessage(message, false);

        this.reportFile = null;

        return true;
    }

    public KeyMapping getKeyOpenModConfig() {
        return this.keyOpenModConfig;
    }

    public void registerKeyMappings() {
        if (this.fabricLoader.isModLoaded("fabric-key-mapping-api-v1")
            && this.fabricLoader.isModLoaded("fabric-lifecycle-events-v1")
        ) {
            KeyMappingHelper.registerKeyMapping(this.keyOpenModConfig);
            ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
                if (this.keyOpenModConfig.consumeClick()) {
                    // Consume pending clicks
                    while (this.keyOpenModConfig.consumeClick()) {}

                    this.openModConfigScreen(minecraft);
                }
            });
        }
    }

    public void openModConfigScreen(final Minecraft minecraft) {
        minecraft.setScreen(new ConfigScreen(
            minecraft.screen,
            this.getConfig(),
            config -> {
                this.setConfig(config);
                this.saveConfig();
            }
        ));
    }
}
