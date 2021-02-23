package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SetFallingBlocks implements Runnable {
    private final Player player;
    private Entity falling;

    public SetFallingBlocks(Player player) {
        this.player = player;
        run();
    }

    @Override
    public void run() {
        if(falling != null) falling.remove();
        Game g = MG.core().getGame(player);
        if(g == null || g.minigame != HideAndSeek.getPlugin()) return;
        HideAndSeekGame game = (HideAndSeekGame) g;
        if(game.hunters.contains(player)) return;
        if(!game.solidBlock.get(player)) {
            Location l = player.getLocation();
            falling = l.getWorld().spawnFallingBlock(l, game.playerBlocks.get(player), (byte) 0);
            falling.setVelocity(new Vector(0,0,0));
            if(game.falling.containsKey(player)) {
                game.falling.replace(player, falling);
            } else
                game.falling.put(player, falling);
        }
        HideAndSeek.getPlugin().getServer().getScheduler().runTaskLater(HideAndSeek.getPlugin(), this, 1);
    }
}
