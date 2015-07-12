package com.github.kotake545.splatoon.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.IkaPlayerInfo;
import com.github.kotake545.splatoon.Splatoon;
import com.github.kotake545.splatoon.Packet.ParticleAPI;
import com.github.kotake545.splatoon.Packet.ParticleAPI.EnumParticle;
import com.github.kotake545.splatoon.util.BlockUtil;
import com.github.kotake545.splatoon.util.RespawnTask;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;
import com.github.kotake545.splatoon.util.Utils;

public class ikaListener implements Listener{
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		ScoreBoardUtil.leavePlayerTeam(player);
		Splatoon.ikaManager.setIka(player);
		ScoreBoardUtil.SpawnTeleport(player);
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
		if(Splatoon.MainTask.getGameStatus().equals("countdown")&&ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),player)!=null){
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
	public void onBlockBreakEvent(BlockBreakEvent event){
		if(!event.getPlayer().isOp()){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if(event.getPlayer().getGameMode()!=GameMode.CREATIVE){
			event.setCancelled(true);
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDamage(EntityDamageEvent event){
		if (!(event.getEntity() instanceof Player)) {
//			if(event.getEntity() instanceof Squid){
//				event.setCancelled(true);
//			}
			return;
		}
		if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			event.setCancelled(true);
		}
		if(event.isCancelled()||((Damageable)event.getEntity()).getHealth()<=0.001){
			return;
		}
		if (ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),(Player) event.getEntity())!=null&&event.getEntity() instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e=(EntityDamageByEntityEvent)event;
			Player damager = null;
			if(e.getDamager() instanceof Player){
				damager=(Player) e.getDamager();
			}else
			if(e.getDamager() instanceof Projectile&&((Projectile)e.getDamager()).getShooter() instanceof Player){
				damager = (Player) ((Projectile)e.getDamager()).getShooter();
			}
			if(damager!=null&&ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),damager)!=null){
				if(ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),(Player) event.getEntity()).getName().equals(ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),damager).getName())){
					event.setCancelled(true);
					return;
				}else if(Splatoon.ikaConfig.damageEffect){
					IkaPlayerInfo dka = Splatoon.ikaManager.getIka(damager);
					ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_DUST.setItemIDandData(dka.type[0],dka.type[1]),event.getEntity().getLocation().clone().add(0,1.3,0),0.0F,0F,0.0F,0,5);
				}
			}
		}
		Player player = (Player)event.getEntity();
		IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
		if(ika.isMuteki()){
			event.setCancelled(true);
			return;
		}
		if(ika.isSpectator()){
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

	@EventHandler
	public void onPlayerCommadPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String[] commands = event.getMessage().trim().split(" ");
		if ( commands.length <= 0 ){
			return;
		}
		String command = commands[0];
		if ( command.equalsIgnoreCase("/kill")){
			if (player.hasPermission("bukkit.command.kill")){
				player.damage(1000);
				event.setCancelled(true);
			}
		}
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(((Damageable)player).getHealth()>0){
			return;
		}
		//インクぶちまけ
		Player killer = null;
		if(Splatoon.MainTask.getGameStatus().equals("gametime")){
			EntityDamageEvent cause = event.getEntity().getLastDamageCause();
			if ( cause != null && cause instanceof EntityDamageByEntityEvent ) {
				killer = player.getKiller();
				Entity damager = ((EntityDamageByEntityEvent)cause).getDamager();
				if ( damager instanceof Projectile ) {
					LivingEntity shooter = (LivingEntity) ((Projectile) damager).getShooter();
					if ( shooter instanceof Player ) {
						killer = (Player)shooter;
					}
				}
			}
			if(killer!=null){
				IkaPlayerInfo pi = Splatoon.ikaManager.getIka(killer);
				if(pi!=null){
					Integer[] bIntegers = pi.getTeamBlock();
					if(bIntegers!=null){
						onDeadInkBom(player.getLocation().clone(), bIntegers,killer);
					}
				}
			}
		}
		if(Splatoon.ikaConfig.spectatorTime>0){
			Utils.heal(player);
			Splatoon.ikaManager.getIka(player).setSpectator(true);
			Location teleport = player.getLocation().clone().add(0,1,0);
			if(killer!=null){
				teleport=killer.getLocation().clone().add(0,1,0);
			}
			player.teleport(teleport);
        	new RespawnTask(player,Splatoon.ikaConfig.spectatorTime*20).runTaskTimer(Splatoon.instance, 1,1);
		}else{
			Utils.Respawn(player);
		}
		event.setKeepInventory(true);
	}

	public void onDeadInkBom(Location location,Integer[] b,Player killer){
		Location from = location.clone().add(0,-0.1,0);
		if(from.getBlock().getTypeId() == 0){
			for(double i = 0.2D; i < 4.0D; i += 0.2D){
				if(from.getBlock().getTypeId() == 0){
					from = from.add(new Vector(0,-1,0).multiply(i));
				}
			}
		}
		double radius = Splatoon.ikaConfig.deadInkRadius;
		ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_CRACK.setItemIDandData(b[0], b[1]),location.clone().add(0,1,0),(float)(radius/2),0F,(float)(radius/2),0,(int)(radius*4));
		ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_DUST.setItemIDandData(b[0], b[1]),location.clone().add(0,2,0),(float)radius,0F,(float)radius,0,(int)(radius*4));
		for (Location bloc : BlockUtil.getPaintSphere(from,radius)){
			//killerにポイント追加。
			if(Splatoon.blockUtil.setBlock(bloc,b)){
				Splatoon.ikaManager.getIka(killer).point+=1;
			}
		}
	}

	/**
	 * 使用してない
	 * @author uikota
	 */
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