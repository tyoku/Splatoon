package com.github.kotake545.splatoon.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.IkaClass;
import com.github.kotake545.splatoon.Splatoon;

public class IkaClassCommand extends IkaCommandExecuter {

	private List<String> classList;
	private StringBuffer rollClass() {
		classList = new ArrayList<String>();
		StringBuffer buffer = new StringBuffer();
		for(int i = 0;i < this.classList.size();i++) {
			buffer.append(this.classList.get(i));
			if(!(i==this.classList.size())) {
				buffer.append(" | ");
			}
		}
		return buffer;
	}
	private void showClassListMessage(CommandSender sender,StringBuffer ClassList) {
		if(classList.isEmpty()){
			sender.sendMessage(Splatoon.format+"クラスが存在しませんでした。");
		}else{
			sender.sendMessage(Splatoon.format+"Class:"+classList.size());
			sender.sendMessage(ClassList.toString());
		}
	}
	private void setEquipment(CommandSender sender,Player id,String className) {
		if(Splatoon.ikaClassManager.getClass(className)!=null){
			if(id!=null&&id.isOnline()){
				Splatoon.ikaManager.getIka(id).setClass(Splatoon.ikaClassManager.getClass(className));
				sender.sendMessage(Splatoon.format+id.getName()+"にクラス名:"+className+"を装備させました。");
			}else{
				sender.sendMessage(Splatoon.format+id.getName()+"プレイヤーが存在しませんでした。");
			}
		}else{
			sender.sendMessage(Splatoon.format+className+"が存在しませんでした。");
		}
	}
	private void setClass(CommandSender sender,String className) {
		if(Splatoon.ikaClassManager.getClass(className)!=null){
			Splatoon.ikaManager.getIka((Player) sender).setClass(Splatoon.ikaClassManager.getClass(className));
			sender.sendMessage(Splatoon.format+className+"を装備しました");
		}else{
			sender.sendMessage(Splatoon.format+className+"クラスが見つかりませんでした。");
		}
	}
	private Boolean showClassList(final String args[]) {
		if(args.length==1&&args[0].toLowerCase().equals("list")){
			return true;
		}
		return false;
	}
	private Boolean setEquiment(final String args[]){
		if(3<=args.length&&args[1].toLowerCase().equals("set")){
			return true;
		}
		return false;
	}
	private Boolean setClass(final String args[],CommandSender sender) {
		if(2<=args.length&&sender instanceof Player){
			return true;
		}
		return false;
	}
	public IkaClassCommand()
	{
		super();
		this.classList = new ArrayList<String>();
	}

	@SuppressWarnings({ "deprecation" })
	@Override
	public boolean onCommand(CommandSender sender, Command command, String paramString, String[] args) {
		if(showClassList(args)){
			for(IkaClass ikaclass:Splatoon.ikaClassManager.getAllClass()){
				classList.add(ikaclass.className);
			}
			showClassListMessage(sender,rollClass());
			return true;
		}
		else if(setEquiment(args)){
			//プレイヤーに職業に応じた装備を装着させる場合
			setEquipment(sender,Bukkit.getPlayer(args[2]),args[0]);
			return true;
		}
		else if(setClass(args,sender)){
			setClass(sender,args[0]);
			return true;
		}
		return unknown(sender, paramString);
	}

	private Boolean unknown(CommandSender sender,String paramString){
		sender.sendMessage(Splatoon.format+"/"+paramString+" classList");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [classname] set");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [classname] set [playername]");
		return true;
	}
}
