package com.github.kotake545.splatoon.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.IkaStage;
import com.github.kotake545.splatoon.Splatoon;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;

public class IkaSplatoonCommand extends IkaCommandExecuter {
	@SuppressWarnings({ "deprecation" })
	@Override
	public boolean onCommand(CommandSender sender, Command command, String paramString, String[] args) {
		if(args.length!=1&&args.length!=2&&args.length!=3){
			return unknown(sender, paramString);
		}
		if(args[0].toLowerCase().equals("setspawn")){
			if(sender instanceof Player){
				//スポーンセット
				Location location = ((Player)sender).getLocation();
				Splatoon.ikaConfig.spawnLocation = location;
				try {
					Splatoon.ikaConfig.createSpawnYML(location);
					sender.sendMessage(Splatoon.format+"現在位置をロビースポーンポイントの設定しました。");
				} catch (IOException e) {
					sender.sendMessage(Splatoon.format+"なにかがおかしいよ。");
					e.printStackTrace();
				}
			}else{
				sender.sendMessage(Splatoon.format+"プレイヤーのみ使用可能です。");
			}
			return true;
		}
		if(args[0].toLowerCase().equals("end")){
			if(Splatoon.MainTask.gameEnd()){
				sender.sendMessage(Splatoon.format+"正常にゲームを終了しました。");
			}else{
				sender.sendMessage(Splatoon.format+"ゲームが始まっていませんでした。");
			}
			return true;
		}
		if(args[0].toLowerCase().equals("return")){
			int a = Splatoon.blockUtil.returnBeforeBlocks();
			sender.sendMessage(Splatoon.format+a+"つ のブロックの修復に成功しました。");
			return true;
		}
		if(2<=args.length){
			if(args[0].toLowerCase().equals("start")){
				IkaStage stage = Splatoon.ikaStageManager.getStage(args[1]);
				if(stage!=null){
					if(Splatoon.MainTask.gameStart(stage.clone())){
						sender.sendMessage(Splatoon.format+stage.getName()+"でゲームを開始します。");
					}else{
						sender.sendMessage(Splatoon.format+"既にゲームが始まっているため開始出来ませんでした。");
					}
				}else{
					sender.sendMessage(Splatoon.format+args[1]+"というステージがみつかりませんでした。");
				}
				return true;
			}
			if(args[0].toLowerCase().equals("join")){
				String team = args[1];
				if(3<=args.length){
					if(args[2].equals("@a")){
						for(Player player:Splatoon.getOnlinePlayers()){
							ScoreBoardUtil.addPlayerTeam(player,team);
						}
						sender.sendMessage(Splatoon.format+"全てのプレイヤーを"+team+"チームに参加させました。");
						return true;
					}
					Player player=Bukkit.getPlayer(args[2]);
					if( player!=null&& player.isOnline()){
						ScoreBoardUtil.addPlayerTeam(player,team);
						sender.sendMessage(Splatoon.format+args[2]+"を"+team+"チームに参加させました。");
					}else{
						sender.sendMessage(Splatoon.format+args[2]+"というプレイヤーが存在しませんでした。");
					}
				}else if(sender instanceof Player){
					ScoreBoardUtil.addPlayerTeam((Player)sender,team);
					sender.sendMessage(Splatoon.format+team+"チームに参加しました。");
				}else{
					sender.sendMessage(Splatoon.format+"プレイヤーのみ使用可能です。");
				}
				return true;
			}
			if(args[0].toLowerCase().equals("leave")){
				if(args[1].equals("@a")){
					for(Player player:Splatoon.getOnlinePlayers()){
						ScoreBoardUtil.leavePlayerTeam(player);
					}
					sender.sendMessage(Splatoon.format+"全てのプレイヤーをチームから脱退させました。");
					return true;
				}
				Player player=Bukkit.getPlayer(args[1]);
				if( player!=null&& player.isOnline()){
					ScoreBoardUtil.leavePlayerTeam(player);
					sender.sendMessage(Splatoon.format+args[1]+"をチームから脱退させました。");
				}else{
					sender.sendMessage(Splatoon.format+args[1]+"というプレイヤーが存在しませんでした。");
				}
				return true;
			}
		}else{
			if(args[0].toLowerCase().equals("leave")){
				if(sender instanceof Player){
					ScoreBoardUtil.leavePlayerTeam((Player)sender);
					sender.sendMessage(Splatoon.format+"チームから抜けました。");
				}else{
					sender.sendMessage(Splatoon.format+"プレイヤーのみ使用可能です。");
				}
				return true;
			}
		}
		return unknown(sender, paramString);
	}
	private Boolean unknown(CommandSender sender,String paramString){
		sender.sendMessage(Splatoon.format+"/"+paramString+" setspawn");
		sender.sendMessage(Splatoon.format+"/"+paramString+" start [stagename]");
		sender.sendMessage(Splatoon.format+"/"+paramString+" end");
		sender.sendMessage(Splatoon.format+"/"+paramString+" return");
		sender.sendMessage(Splatoon.format+"/"+paramString+" join [teamname]");
		sender.sendMessage(Splatoon.format+"/"+paramString+" join [teamname] [playername]");
		sender.sendMessage(Splatoon.format+"/"+paramString+" leave");
		sender.sendMessage(Splatoon.format+"/"+paramString+" leave [playername]");
		return true;
	}
}