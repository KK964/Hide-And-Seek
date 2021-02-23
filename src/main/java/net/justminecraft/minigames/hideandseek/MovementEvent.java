package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementEvent implements Listener {
    @EventHandler
    public void movementEvent(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Game g = MG.core().getGame(player);
        if(g == null || g.minigame != HideAndSeek.getPlugin()) return;
        HideAndSeekGame game = (HideAndSeekGame) g;
        Location from = e.getFrom();
        Location to = e.getTo();
        if(from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            if(game.playerBlocks.containsKey(player)) {
                game.solidTime.replace(player, 0);
                if(game.solidBlock.get(player)) {
                    for(Player p : game.players)
                        p.sendBlockChange(from, Material.AIR, (byte) 0);
                    game.solidBlock.replace(player, false);
                }
            }
        }
    }
}
