package eu.pb4.playerdata.api.storage;

import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.impl.PMI;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public record NbtDataStorage(String path) implements PlayerDataStorage<NbtCompound> {

    @Override
    public boolean save(MinecraftServer server, UUID player, NbtCompound settings) {
        Path path = PlayerDataApi.getPathFor(server, player);

        if (settings == null) {
            try {
                return Files.deleteIfExists(path.resolve(this.path + ".dat"));
            } catch (Throwable ignored) {
                return false;
            }
        }

        try {
            Files.createDirectories(path);
            NbtIo.writeCompressed(settings, path.resolve(this.path + ".dat"));
            return true;
        } catch (Exception e) {
            PMI.LOGGER.error(String.format("Couldn't save player data of %s for path %s", player, this.path));
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public NbtCompound load(MinecraftServer server, UUID player) {
        try {
            Path path = PlayerDataApi.getPathFor(server, player).resolve(this.path + ".dat");
            if (!Files.exists(path)) {
                return null;
            }

            return NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
        } catch (Exception e) {
            PMI.LOGGER.error(String.format("Couldn't load player data of %s for path %s", player, this.path));
            e.printStackTrace();
            return null;
        }
    }
}
