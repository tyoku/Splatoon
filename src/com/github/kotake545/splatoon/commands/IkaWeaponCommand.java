package com.github.kotake545.splatoon.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.IkaWeapon;
import com.github.kotake545.splatoon.Splatoon;


public class IkaWeaponCommand extends IkaCommandExecuter {
	@SuppressWarnings({ "deprecation" })
	@Override
	public boolean onCommand(CommandSender sender, Command command, String paramString, String[] args) {
		if(args.length!=1&&args.length!=2&&args.length!=3){
			return unknown(sender, paramString);
		}
		if(args[0].toLowerCase().equals("list")){
			List<String> list=new ArrayList<String>();
			for(IkaWeapon weapons:Splatoon.ikaWeaponManager.getAllWeapons()){
				list.add(ChatColor.stripColor(weapons.getName()));
			}
			StringBuffer a = new StringBuffer();
			for (int i = 0; i < list.size();i++) {
				String name = list.get(i);
				a.append(name);
				if(i+1 < list.size()){
					a.append(" | ");
				}
			}
			if(list.size()<=0){
				sender.sendMessage(Splatoon.format+"武器が登録されていません。");
			}else{
				sender.sendMessage(Splatoon.format+"Weapons:"+list.size());
				sender.sendMessage(a.toString());
			}
			return true;
		}
		if(2<=args.length){
			if(args[0].toLowerCase().equals("give")){
				if(3<=args.length){
					Player give=Bukkit.getPlayer(args[1]);
					IkaWeapon iw = Splatoon.ikaWeaponManager.getWeapon(args[2]);
					if(iw==null){
						sender.sendMessage(Splatoon.format+args[2]+"という武器が存在しませんでした。");
						return true;
					}
					if(give!=null&&give.isOnline()){
						give.getInventory().addItem(iw.getItemStack());
						sender.sendMessage(Splatoon.format+args[1]+"に"+args[2]+"を配布しました。");
						return true;
					}else{
						sender.sendMessage(Splatoon.format+args[1]+"というプレイヤーが存在しませんでした。");
						return true;
					}
				}
			}

			if(sender instanceof Player){
				Player player = (Player)sender;
				if(args[0].toLowerCase().equals("get")){
					IkaWeapon iw = Splatoon.ikaWeaponManager.getWeapon(args[1]);
					if(iw==null){
						sender.sendMessage(Splatoon.format+args[1]+"という武器が存在しませんでした。");
						return true;
					}
					player.getInventory().addItem(iw.getItemStack());
					sender.sendMessage(Splatoon.format+"インベントリに"+args[1]+"を追加しました。");
					return true;
				}
			}
		}
		return unknown(sender, paramString);
	}
	private Boolean unknown(CommandSender sender,String paramString){
		sender.sendMessage(Splatoon.format+"/"+paramString+" list");
		sender.sendMessage(Splatoon.format+"/"+paramString+" give [playername] [weaponname]");
		sender.sendMessage(Splatoon.format+"/"+paramString+" get [weaponname]");
		return true;
	}
}
