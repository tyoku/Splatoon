package com.github.kotake545.splatoon.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.Splatoon;

public class Utils {

	public static boolean isNumber(String val) {
		if(val.matches("^[0-9]{1,}$")){
			return true;
		}else{
			return false;
		}
	}
	public static Integer[] getBlock(Location loc){
		Integer[] b = new Integer[]{0,0};
		Block block = loc.getBlock();
		b[0] = block.getTypeId();
		b[1] = (int) block.getData();
		if(Splatoon.ikaConfig.blockset){
			return b;
		}else{
			if(Splatoon.blockUtil.getBlock(block)!=null){
				return Splatoon.blockUtil.getBlock(block);
			}else{
				return b;
			}
		}
	}

	public static void setEXPBar(Player player,double max,double ink){
		player.setLevel((int) ink);
		double pa = 1/max;
		player.setExp((float) (ink*pa));
	}
	public static Sound getSound(String soundName){
		Sound sound = Sound.valueOf(soundName.toUpperCase().replace(" ", "_"));
		return sound;
	}
}
