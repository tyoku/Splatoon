package com.github.kotake545.splatoon.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.IkaStage;
import com.github.kotake545.splatoon.Splatoon;
import com.github.kotake545.splatoon.Manager.IkaStageManager;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;
import com.github.kotake545.splatoon.util.Utils;

public class IkaStageCommand extends IkaCommandExecuter {
	@SuppressWarnings({ "deprecation" })
	@Override
	public boolean onCommand(CommandSender sender, Command command, String paramString, String[] args) {
		if(args.length==1&&args[0].toLowerCase().equals("list")){
			List<String> list=new ArrayList<String>();
			for(IkaStage stages:Splatoon.ikaStageManager.getAllStages()){
				list.add(ChatColor.stripColor(stages.getName()));
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
				sender.sendMessage(Splatoon.format+"ステージが登録されていません。");
			}else{
				sender.sendMessage(Splatoon.format+"Stages:"+list.size());
				sender.sendMessage(a.toString());
			}
			return true;
		}
		if(2<=args.length){
			String stageName = args[0];
			IkaStage ikaStage = Splatoon.ikaStageManager.getStage(stageName);
			if(args[1].toLowerCase().equals("create")){
				if(ikaStage==null){
					IkaStage newStage= new IkaStage();
					newStage.stageName = stageName;
					Splatoon.ikaStageManager.addStage(newStage);
					//ファイルに出力
					try {
						IkaStageManager.createNewStageYml(stageName);
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
					sender.sendMessage(Splatoon.format+stageName+" ステージ を作成しました。");
				}else{
					sender.sendMessage(Splatoon.format+stageName+" このステージ名は既に登録されています。");
				}
				return true;
			}
			if(ikaStage==null){
				sender.sendMessage(Splatoon.format+stageName+" ステージが存在しません。");
				sender.sendMessage(Splatoon.format+"/"+paramString+" [stagename] create で新規作成して下さい。");
				return true;
			}
			if(3<=args.length){
				if(args[1].toLowerCase().equals("spawn")){
					if(sender instanceof Player){
						String teamName=args[2].toLowerCase();
						boolean check = false;
						for(String a:ScoreBoardUtil.colors){
							if(a.equals(teamName)){
								check=true;
								break;
							}
						}
						if(check){
							Location location = ((Player)sender).getLocation();
							ikaStage.saveSpawn(teamName,location);
							sender.sendMessage(Splatoon.format+stageName+" ステージ:"+teamName+" チームのスポーン座標を現在の位置で設定しました。");
						}else{
							List<String> list=new ArrayList<String>();
							for(String a:ScoreBoardUtil.colors){
								list.add(a);
							}
							StringBuffer a = new StringBuffer();
							for (int i = 0; i < list.size();i++) {
								String name = list.get(i);
								a.append(name);
								if(i+1 < list.size()){
									a.append(" | ");
								}
							}
							sender.sendMessage(Splatoon.format+"チーム名:"+teamName+" は使用できません。");
							sender.sendMessage(Splatoon.format+"設定可能チーム名 : "+a.toString());
						}
					}else{
						sender.sendMessage(Splatoon.format+"プレイヤーのみ使用可能です。");
					}
					return true;
				}
				if(Utils.isNumber(args[2])){
					int secconds = Integer.parseInt(args[2]);
					if(args[1].toLowerCase().equals("gamestart")){
						ikaStage.saveGameStart(secconds);
						sender.sendMessage(Splatoon.format+stageName+" ステージ:"+"待機時間を"+secconds+"秒で設定しました。");
						return true;
					}
					if(args[1].toLowerCase().equals("countdown")){
						ikaStage.saveCountDown(secconds);
						sender.sendMessage(Splatoon.format+stageName+" ステージ:"+"カウントダウンを"+secconds+"秒で設定しました。");
						return true;
					}
					if(args[1].toLowerCase().equals("gametime")){
						ikaStage.saveGameTime(secconds);
						sender.sendMessage(Splatoon.format+stageName+" ステージ:"+"ゲーム時間を"+secconds+"秒で設定しました。");
						return true;
					}
				}
			}
		}
		return unknown(sender, paramString);
	}

	private Boolean unknown(CommandSender sender,String paramString){
		sender.sendMessage(Splatoon.format+"/"+paramString+" list");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [stagename] create");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [stagename] spawn [team]");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [stagename] gamestart [secconds]");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [stagename] countdown [secconds]");
		sender.sendMessage(Splatoon.format+"/"+paramString+" [stagename] gametime [secconds]");
		return true;
	}
}
