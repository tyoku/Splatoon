package com.github.kotake545.splatoon.util;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.kotake545.splatoon.Splatoon;


public class RespawnTask extends BukkitRunnable {
    private Player player;
    private double count;
    private int tick;
    public RespawnTask(Player player, int tick) {
        this.player = player;
        this.tick = tick;
        this.count = tick;
    }

	@Override
    public void run() {
    	if(0 <tick){
    		if(!Splatoon.MainTask.getGameStatus().equals("gametime")){
    			if(Splatoon.MainTask.getGameStatus().equals("result")){
    				this.cancel();
    				return;
    			}
    			Utils.Respawn(player);
    			this.cancel();
    		}
        	tick--;
	    	if(player == null || !player.isOnline()){
	    		this.cancel();
	    	}
//			double pa = 100-((count-tick)/count) *100;
//        	setMessage(player,"復活まであと "+tick/20+"秒");
	    	if(tick%20==0){
	    		player.sendMessage(Splatoon.format+"復活まで"+((int)tick/20)+"秒");
	    	}
//	        setHealth(player, (float) pa);
			player.getInventory().clear();
			player.updateInventory();
    	}else{
    		Utils.Respawn(player);
        	this.cancel();
    	}
    }
}