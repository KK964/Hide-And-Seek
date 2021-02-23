package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetPlayerBlock {
    private final Player player;
    private final HideAndSeek plugin;

    public SetPlayerBlock(Player player) {
        this.player = player;
        this.plugin = HideAndSeek.getPlugin();
        set();
    }

    private void set() {
        Game g = MG.core().getGame(player);
        HideAndSeekGame game = (HideAndSeekGame) g;
        game.playerBlocks.put(player, getBlock());
        game.solidBlock.put(player, false);
        game.solidTime.put(player, 0);
    }

    private Material getBlock() {
        List blocks = new ArrayList(plugin.getConfig().getStringList("materials"));
        Material block = Material.valueOf((String) blocks.get((int) Math.random() * blocks.size()));
        return block;
    }
}
