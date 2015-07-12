package com.github.kotake545.splatoon;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.Packet.ParticleAPI;
import com.github.kotake545.splatoon.Packet.ParticleAPI.EnumParticle;
import com.github.kotake545.splatoon.util.BlockUtil;
import com.github.kotake545.splatoon.util.Utils;

public class ProjectileInfo {
	private Projectile projectile;
	private Vector vector;
	private Location startloc;
	private Location lastloc;
	private IkaPlayerInfo shooter;
	private IkaWeapon weapon;
	private Integer[] block;
	private double damage;

	private int tick;
	private boolean dead = false;
	private boolean canmove = true;
	private boolean removed = false;

	private boolean destroyNextTick = false;

	public ProjectileInfo(IkaPlayerInfo shooter,IkaWeapon weapon,Vector vec) {
		this.shooter = shooter;
		this.weapon = weapon;
		this.vector = vec;
		this.block = shooter.getTeamBlock();
		this.damage = weapon.getShootDamage();
		this.projectile = shooter.getPlayer().launchProjectile(Snowball.class);
		projectile.setShooter(shooter.getPlayer());
		startloc = projectile.getLocation();
	}

	public void tick(){
		if(!dead&&!projectile.isDead()){
			tick++;
			if(projectile != null){
				lastloc = projectile.getLocation();
				if(block!=null){
					ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_DUST.setItemIDandData(block[0],block[1]),lastloc,0,0,0,0,1);
				}
				if(canmove){
					if(startloc.distance(lastloc)>weapon.getShootDistance()){
						canmove = false;
						vector.multiply(weapon.moveFinishMultiply);
					}
					projectile.setVelocity(vector);
				}
			}else{
				dead = true;
			}
		}else{
			dead=true;
		}
		if(dead){
			remove();
		}
		if(destroyNextTick){
			dead = true;
		}
	}
	public void remove(){
		dead = true;
		Splatoon.projectileManager.remove(this);
		projectile.remove();
		onHit(projectile.getLocation());
		onDestroy();
	}
	public void onHit(Location hit){
		if(removed){
			return;
		}
		removed = true;
		Integer[] a = shooter.getTeamBlock();
		if(a==null){
			return;
		}
		Location from = hit;
		if(from.getBlock().getTypeId() == 0){
			for(double i = 0.2D; i < 4.0D; i += 0.2D){
				if(from.getBlock().getTypeId() == 0){
					from = from.add(new Vector(0,-1,0).normalize().multiply(i));
				}
			}
		}
//		Splatoon.blockUtil.setBlock(from,a);
		double radius = weapon.getHitRadius();
		ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_DUST.setItemIDandData(a[0], a[1]),from,1F,0.2F,1F,0,5);
		for(String name:Splatoon.ikaConfig.shootPaintSound.keySet()){
			Utils.playSound(name,Splatoon.ikaConfig.shootPaintSound.get(name),from);
		}

		for (Location bloc : BlockUtil.getPaintSphere(from,radius)){
			//ポイント追加
			if(Splatoon.blockUtil.setBlock(bloc,a)){
				shooter.point+=1;
			}
		}
	}
	public void onDestroy(){
		projectile.remove();
		projectile = null;
		vector = null;
		weapon = null;
		shooter = null;
	}

	public void setDestroyNextTick(Boolean destroy){
		destroyNextTick = destroy;
	}

	public Projectile getProjectile() {
		return projectile;
	}

	public double getDamage(){
		return damage;
	}

	public void onKnockBack(Entity entity) {
		if(weapon.shootKnockback > 0.0D){
			Vector speed = vector;
			speed.normalize().setY(0.6D).multiply(weapon.shootKnockback / 4.0D);
			entity.setVelocity(speed);
		}
	}
}
