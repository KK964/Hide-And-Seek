package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        Material m = getBlock();
        game.playerBlocks.put(player, m);
        game.solidBlock.put(player, false);
        game.solidTime.put(player, 0);
        player.sendMessage(ChatColor.GREEN + "You are \"" + m.toString() + "\"!");
    }

    private Material getBlock() {
        List blocks = new ArrayList(plugin.getConfig().getStringList("materials"));
        Random rand = new Random();
        Material block = Material.valueOf((String) blocks.get(rand.nextInt(blocks.size())));
        return block;
    }
}
