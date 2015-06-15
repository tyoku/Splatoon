package com.github.kotake545.splatoon.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.IkaPlayerInfo;
import com.github.kotake545.splatoon.Splatoon;
import com.github.kotake545.splatoon.util.BlockUtil;

public class ikaListener implements Listener{
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		Splatoon.ikaManager.setIka(player);
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		Splatoon.ikaManager.removeIka(player);
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
		if(ika.isIka()&&player.isOnGround()&&event.getTo().getY()>event.getFrom().getY()){
			ika.onJump();
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
		IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
		if(ika.inkGauge>0&&!ika.isIka&&ika.getPlayer().getItemInHand().getTypeId()==359){
			Integer[] a = ika.getTeamBlock();
			ika.inkGauge --;

			/**
			 * ローラー用
			 */
//			Location loc = player.getLocation();
//			double dir = -loc.getYaw() - 90.0F;
//			double pitch = -loc.getPitch();
//			double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
//			double yd = Math.sin(Math.toRadians(pitch));
//			double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
//			Vector vector = new Vector(xd, yd, zd);
//			Location start = player.getLocation().clone().add(0,-0.1,0);
//			start.add(vector);

			//とりあえずここでgetCircleBlocks確認してるだけ。
			Location start = player.getLocation().clone().add(0,-0.1,0);
			for(Block block:BlockUtil.getCircleBlocks(start,3)){
				Location set = block.getLocation();
//				}
				Splatoon.blockUtil.setBlock(set,a);
			}
//			Splatoon.blockUtil.setBlock(player.getLocation().clone().add(0,-0.5,0),a);
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event){
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			event.setCancelled(true);
		}
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
