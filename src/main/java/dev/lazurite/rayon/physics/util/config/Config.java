package dev.lazurite.rayon.physics.util.config;

import dev.lazurite.rayon.physics.Rayon;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;

import java.io.IOException;
import java.nio.file.Files;

@Settings(onlyAnnotated = true)
public class Config {
    public static final Config INSTANCE = new Config();
    public static final String CONFIG_NAME = "rayon.json";

    @Setting
    @Setting.Constrain.Range(min = 2, max = 6)
    public int blockDistance;

    @Setting
    @Setting.Constrain.Range(min = 5, max = 25)
    public int entityDistance;

    @Setting
    @Setting.Constrain.Range(max = 0.0f)
    public float gravity;

    @Setting(name = "tickRate")
    @Setting.Constrain.Range(min = 20, max = 260, step = 1.0f)
    public int stepRate;

    @Setting(name = "airDensity")
    @Setting.Constrain.Range(min = 0.0f)
    public float airDensity;

    private Config() {
        this.blockDistance = 2;
        this.entityDistance = 5;
        this.gravity = -9.81f;
        this.stepRate = 60;
        this.airDensity = 1.2f;
    }

    public void send(PacketByteBuf buf) {
        buf.writeInt(blockDistance);
        buf.writeInt(entityDistance);
        buf.writeFloat(gravity);
        buf.writeInt(stepRate);
        buf.writeFloat(airDensity);
    }

    @Environment(EnvType.CLIENT)
    public void receive(PacketByteBuf buf) {
        this.blockDistance = buf.readInt();
        this.entityDistance = buf.readInt();
        this.gravity = buf.readFloat();
        this.stepRate = buf.readInt();
        this.airDensity = buf.readFloat();
    }

    public void load() {
        if (Files.exists(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME))) {
            try {
                FiberSerialization.deserialize(
                        ConfigTree.builder().applyFromPojo(INSTANCE, AnnotatedSettings.builder().build()).build(),
                        Files.newInputStream(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME)),
                        new JanksonValueSerializer(false)
                );
            } catch (IOException | FiberException e) {
                Rayon.LOGGER.error("Error loading Rayon config.");
                e.printStackTrace();
            }
        } else {
            Rayon.LOGGER.info("Creating Rayon config.");
            this.save();
        }
    }

    public void save() {
        try {
            FiberSerialization.serialize(
                    ConfigTree.builder().applyFromPojo(INSTANCE, AnnotatedSettings.builder().build()).build(),
                    Files.newOutputStream(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME)),
                    new JanksonValueSerializer(false)
            );
        } catch (IOException e) {
            Rayon.LOGGER.error("Error saving Rayon config.");
            e.printStackTrace();
        }
    }
}