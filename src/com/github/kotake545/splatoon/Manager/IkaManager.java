package com.github.kotake545.splatoon.Manager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.IkaPlayerInfo;
import com.github.kotake545.splatoon.Splatoon;

public class IkaManager {
	private static List<IkaPlayerInfo> ikaPlayers;
	public IkaManager(){
		ikaPlayers = new ArrayList<IkaPlayerInfo>();
	}

	public void tick(){
		for(int i = ikaPlayers.size() - 1; i >= 0; i--){
			IkaPlayerInfo ipi = ikaPlayers.get(i);
			if(ipi!=null){
				ipi.tick();
			}
		}
	}

	public void setIka(Player player){
		if(getIka(player)==null){
			IkaPlayerInfo ipi = new IkaPlayerInfo(player);
			ikaPlayers.add(ipi);
		}
	}

	public void removeIka(Player player){
		for(int i = ikaPlayers.size() - 1; i >= 0; i--){
			if(ikaPlayers.get(i).getPlayer().getName().equals(player.getName())){
				ikaPlayers.remove(i);
			}
		}
	}

	public IkaPlayerInfo getIka(Player player){
		for(int i = ikaPlayers.size() - 1; i >= 0; i--){
			if(ikaPlayers.get(i).getPlayer().equals(player)){
				return ikaPlayers.get(i);
			}
		}
		return null;
	}

	public void reload() {
		for(Player player:Splatoon.getOnlinePlayers()){
			setIka(player);
		}
	}
}