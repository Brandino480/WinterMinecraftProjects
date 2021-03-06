package me.winterguardian.duel.game;

import me.winterguardian.core.util.AchievementUtil;
import me.winterguardian.core.util.ActionBarUtil;
import me.winterguardian.core.util.CombatUtil;
import me.winterguardian.duel.Duel;
import me.winterguardian.duel.DuelMessage;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;


public class GameListener implements Listener
{
	private HashMap<Player, Integer> headshots = new HashMap<Player, Integer>();
	
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if(event.getEntity() == Duel.getInstance().getGame().getPlayer1() && Duel.getInstance().getGame().getPlayer2() != null)
			Duel.getInstance().getGame().win(Duel.getInstance().getGame().getPlayer2());
		
		else if(event.getEntity() == Duel.getInstance().getGame().getPlayer2() && Duel.getInstance().getGame().getPlayer1() != null)
			Duel.getInstance().getGame().win(Duel.getInstance().getGame().getPlayer1());
		
		else if(Duel.getInstance().gameContains(event.getEntity()))
			Duel.getInstance().leave(event.getEntity());
		
		else if(!Duel.getInstance().contains(event.getEntity()))
			return;
		
		event.setKeepInventory(false);
		event.setKeepLevel(false);
		event.getDrops().clear();
		event.setDroppedExp(0);
		event.setDeathMessage(null);
		event.setNewLevel(0);
		event.setNewExp(0);
		event.setNewTotalExp(0);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		if(Duel.getInstance().contains(e.getPlayer()))
			Duel.getInstance().leave(e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
	{
		if(e.getPlayer().hasPermission(Duel.ADMINISTRATION))
			return;
		
		if(Duel.getInstance().gameContains(e.getPlayer()))
		{
			String cmd = "";
			if(e.getMessage().contains(" "))
				cmd = e.getMessage().split(" ")[0].replaceFirst("/", "");
			else
				cmd = e.getMessage().replaceFirst("/", "");
				
			if(!Duel.getInstance().getSettings().getAllowedCommands().contains(cmd.toLowerCase()))
			{
				e.setCancelled(true);
				DuelMessage.DUEL_CMDBLOCK.say(e.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e)
	{
		if(Duel.getInstance().getGame().getToTeleport().contains(e.getPlayer()))
		{
			e.setRespawnLocation(Duel.getInstance().getSettings().getLobby());
			Duel.getInstance().getGame().getToTeleport().remove(e.getPlayer());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerHeadshot(final EntityDamageByEntityEvent event)
	{
		if(!CombatUtil.isHeadshot(event))
			return;

		if(!(event.getDamager() instanceof Projectile) || !(((Projectile)event.getDamager()).getShooter() instanceof Player))
			return;

		final Player shooter = (Player)((Projectile)event.getDamager()).getShooter();

		if(Duel.getInstance().getGame().canBattle(shooter))
		{
			if(headshots.containsKey(shooter))
			{
				headshots.put(shooter, headshots.get(shooter) + 1);
				ActionBarUtil.sendActionMessage(shooter, "§e§lTir dans la tête ! x" + (headshots.get(shooter)));
				if(headshots.get(shooter) == 5)
					AchievementUtil.flashShow(shooter, Achievement.SNIPE_SKELETON, Duel.getInstance());
			}
			else
			{
				headshots.put(shooter, 1);
				ActionBarUtil.sendActionMessage(shooter, "§e§lTir dans la tête !");
			}

			Bukkit.getScheduler().runTaskLater(Duel.getInstance(), new Runnable()
			{
				private int i = headshots.get(shooter);

				@Override
				public void run()
				{
					if(headshots != null)
						if(headshots.get(shooter) == i)
							headshots.remove(shooter);
				}
			}, 40);
		}
	}
}
