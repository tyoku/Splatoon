package com.github.kotake545.splatoon.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.Splatoon;

public class IkaClassCommand extends IkaCommandExecuter {
	@SuppressWarnings({ "deprecation" })
	@Override
	public boolean onCommand(CommandSender sender, Command command, String paramString, String[] args) {
		if(args.length==1&&args[0].toLowerCase().equals("list")){
			List<String> list=new ArrayList<String>();
			for(String ikaclass:Splatoon.ikaClassManager.getAllClass()){
				list.add(ikaclass);
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
				sender.sendMessage(Splatoon.format+"クラスが登録されていません。");
			}else{
				sender.sendMessage(Splatoon.format+"Class:"+list.size());
				sender.sendMessage(a.toString());
			}
			return true;
		}
		if(3<=args.length&&args[1].toLowerCase().equals("set")){
			String className = args[0];
			Player give=Bukkit.getPlayer(args[2]);
			if(Splatoon.ikaClassManager.getClass(className)!=null){
				if(give!=null&&give.isOnline()){
					Splatoon.ikaManager.getIka(give).setClass(className);
					sender.sendMessage(Splatoon.format+args[2]+"に"+className+"を装備させました。");
				}else{
					sender.sendMessage(Splatoon.format+args[2]+"というプレイヤーが存在しませんでした。");
				}
			}else{
				sender.sendMessage(Splatoon.format+className+" クラスが登録されていません。");
			}
			return true;
		}
		if(2<=args.length&&sender instanceof Player){
			String className = args[0];
			if(args[1].toLowerCase().equals("add")){
				if(Splatoon.ikaClassManager.addClass(className,((Player)sender).getInventory())){
					sender.sendMessage(Splatoon.format+"現在のインベントリを "+className+" クラスとして登録しました。");
				}else{
					sender.sendMessage(Splatoon.format+className+" クラスは既に登録されています。");
				}
				return true;
			}
			if(args[1].toLowerCase().equals("set")){
				if(Splatoon.ikaClassManager.getClass(className)!=null){
					Splatoon.ikaManager.getIka((Player) sender).setClass(className);
					sender.sendMessage(Splatoon.format+className+" クラスを装備しました。");
				}else{
					sender.sendMessage(Splatoon.format+className+" クラスが登録されていません。");
				}
				return true;
			}
		}
		return unknown(sender, paramString);
	}

	private Boolean unknown(CommandSender sender,String paramString){
		sender.sendMessage(Splatoon.format+"/"+paramString+" list");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [classname] add");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [classname] set");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [classname] set [playername]");
		return true;
	}
}
