package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlayerSolidBlock implements Runnable {
    private final Player player;

    public PlayerSolidBlock(Player player) {
        this.player = player;
        run();
    }

    @Override
    public void run() {
        Game g = MG.core().getGame(player);
        if(g == null || g.minigame != HideAndSeek.getPlugin()) return;
        HideAndSeekGame game = (HideAndSeekGame) g;
        if(game.hunters.contains(player)) return;
        if(!game.solidTime.containsKey(player)) game.solidTime.put(player, 0);
        if(game.solidTime.get(player) == 5 && !game.solidBlock.get(player)) {
            setBlock(game);
        } else {
            if(game.solidTime.get(player) > 2)
                player.playSound(player.getLocation(), Sound.CLICK, 100, 1);
            if(game.solidTime.get(player) != 5)
                game.solidTime.replace(player, game.solidTime.get(player) + 1);
        }
        HideAndSeek.getPlugin().getServer().getScheduler().runTaskLater(HideAndSeek.getPlugin(), this, 20);
    }

    private void setBlock(HideAndSeekGame game) {
        Location location = player.getLocation();
        Material material = game.playerBlocks.get(player);
        game.solidBlock.replace(player, true);
        if(location.getBlock().getType() != Material.AIR)  {
            player.sendMessage(ChatColor.DARK_RED + "The location you are in already contains a block!");
            return;
        }
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 100, 1);
        for(Player p : game.players) {
            if(p != player) {
                p.sendBlockChange(location, material, (byte) 0);
            }
        }
    }
}
