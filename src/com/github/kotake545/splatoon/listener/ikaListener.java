package com.github.kotake545.splatoon.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.IkaPlayerInfo;
import com.github.kotake545.splatoon.Splatoon;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;
import com.github.kotake545.splatoon.util.Utils;

public class ikaListener implements Listener{
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		ScoreBoardUtil.leavePlayerTeam(player);
		Splatoon.ikaManager.setIka(player);
	}
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event){
		event.setCancelled(true);
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		ScoreBoardUtil.leavePlayerTeam(player);
		Splatoon.ikaManager.removeIka(player);
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
		if(ika.isIka()&&player.isOnGround()&&event.getTo().getY()>event.getFrom().getY()){
			ika.onJump();
		}
		if(Splatoon.MainTask.getGameStatus().equals("countdown")&&ScoreBoardUtil.getPlayerTeam(player)!=null){
			ScoreBoardUtil.SpawnTeleport(player);
		}
		/**
		 * イカジャンプ用
		 */
//		if(ika.isIka()&&player.isOnGround()&&event.getTo().getY()>event.getFrom().getY()){
//			player.teleport(player.getLocation().clone().add(0,1,0));
//			Location to = event.getTo().clone();
//			to.setY(0.1);
//			Location from = event.getFrom().clone();
//			from.setY(0);
//			player.setVelocity(to.subtract(from).toVector().multiply(2.0D));
//		}
	}
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Action action = event.getAction();
		IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
		if(action==Action.LEFT_CLICK_AIR||action==Action.LEFT_CLICK_BLOCK){
			ika.onClick("left");
		}
		if(action==Action.RIGHT_CLICK_AIR||action==Action.RIGHT_CLICK_BLOCK){
			ika.onClick("right");
		}
	}

	@EventHandler
	public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event){
		Player player = event.getPlayer();
		if(Splatoon.ikaConfig.ikaChangeforSneak){
			IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
			ika.onIka();
		}
	}

	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent event){
		Player player = event.getPlayer();
		if(player.getGameMode()==GameMode.CREATIVE){
			return;
		}
		IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
		ika.onIka();
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDamage(EntityDamageEvent event){
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			event.setCancelled(true);
		}
		if(event.isCancelled()||((Damageable)event.getEntity()).getHealth()<=0.001){
			return;
		}
		if (ScoreBoardUtil.getPlayerTeam((Player) event.getEntity())!=null&&event.getEntity() instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e=(EntityDamageByEntityEvent)event;
			Player damager = null;
			if(e.getDamager() instanceof Player){
				damager=(Player) e.getDamager();
			}else
			if(e.getDamager() instanceof Projectile&&((Projectile)e.getDamager()).getShooter() instanceof Player){
				damager = (Player) ((Projectile)e.getDamager()).getShooter();
			}
			if(damager!=null&&ScoreBoardUtil.getPlayerTeam(damager)!=null){
				if(ScoreBoardUtil.getPlayerTeam((Player) event.getEntity()).getName().equals(ScoreBoardUtil.getPlayerTeam(damager).getName())){
					event.setCancelled(true);
					return;
				}
			}
		}
		Player player = (Player)event.getEntity();
		IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
		if(ika.isMuteki()){
			event.setCancelled(true);
			return;
		}
		player.setMaximumNoDamageTicks(0);

		ika.healDelay=Splatoon.ikaConfig.healDelay;
		if(ika.isNoDamage()){
			event.setCancelled(true);
			if(event instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent)event;
				Entity damager = ede.getDamager();
				if(damager instanceof Projectile||event.getEntity().hasMetadata("explode")){
					event.getEntity().setLastDamageCause((EntityDamageByEntityEvent)event);
					Utils.damage(event,0);
				}
			}
		}else{
			ika.setNoDamageTick(10);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = ScoreBoardUtil.getSpawnLocation(player);
        if(((Damageable)player).getHealth()>0){
        	return;
        }
    	Utils.heal(player);
    	Splatoon.ikaManager.getIka(player).Reset();
    	Splatoon.ikaManager.getIka(player).setMuteki(20*5);
		player.teleport(loc,TeleportCause.PLUGIN);
		event.setKeepInventory(true);
	}

	public class onIkaJump extends BukkitRunnable {
		Player player;
		Vector vec;
		public onIkaJump(Player player,Vector vec) {
			this.player = player;
			this.vec = vec;
		}
		@Override
	    public void run() {
		    Vector new_vec = new Vector(vec.getX(), 0.0D, vec.getZ()).multiply(1.0D);
		    player.setVelocity(new_vec);
		    player.sendMessage(new_vec.toString());
	    }
	}
}