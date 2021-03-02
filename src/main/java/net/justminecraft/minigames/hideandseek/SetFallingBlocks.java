package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class SetFallingBlocks implements Runnable {
    private final Player player;

    public SetFallingBlocks(Player player) {
        this.player = player;
        run();
    }

    @Override
    public void run() {
        Game g = MG.core().getGame(player);
        if(g == null || g.minigame != HideAndSeek.getPlugin()) return;
        HideAndSeekGame game = (HideAndSeekGame) g;
        if(game.hunters.contains(player)) return;
        if(!game.solidBlock.get(player)) {
            spawnFalling(game);
        }
        HideAndSeek.getPlugin().getServer().getScheduler().runTaskLater(HideAndSeek.getPlugin(), this, 1);
    }

    private void spawnFalling(HideAndSeekGame game) {
        Location l = player.getLocation();
        org.bukkit.Material material = game.playerBlocks.get(player);
        byte data = 0;
        EntityFallingBlock b = sendPacket(l, material, data, game.players);
        PacketPlayOutEntityDestroy pd = new PacketPlayOutEntityDestroy(b.getId());
        HideAndSeek.getPlugin().getServer().getScheduler().runTaskLater(HideAndSeek.getPlugin(), () -> {
            for(Player pl : game.players)
                ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(pd);
        }, 1);
    }

    private EntityFallingBlock sendPacket(Location loc, org.bukkit.Material mat, byte data, List<Player> players) {
        World world = ((CraftWorld)loc.getWorld()).getHandle();
        EntityFallingBlock entityFallingBlock = new EntityFallingBlock(world);
        entityFallingBlock.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(entityFallingBlock, 70, mat.getId() + (data << 12));
        for(Player p : players) {
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
        }
        return entityFallingBlock;
    }
}
