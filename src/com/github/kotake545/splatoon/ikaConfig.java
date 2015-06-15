package com.github.kotake545.splatoon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.github.kotake545.splatoon.util.Utils;

public class ikaConfig {
	public boolean blockset = false; //ワールドには書き込まない
	public boolean particle = true; //パーティクルのON/OFF

	public float moveSpeed = 0.2F; //デフォルト移動速度
	public float ikaSpeed = 0.4F; //イカ移動速度 スニークで移動速度下がるから チェック時に2倍にして
	public float downSpeed = 0.1F; //敵のインクにいる時のスピード

	public int inkGauge = 50; //デフォルトのインクゲージ
	public int inkHealSpeed = 1; //インクの回復速度 低いほうが速いです
	public int inkHeal = 1; //インクの回復量

	public int healDelay = 50; //ダメージを喰らってから回復し始めるまでの待機時間
	public int healSpeed = 1; //体力の回復速度
	public float heal = 0.5F;//体力の回復量

	public boolean inkWool = true; //インクのタイプ 羊毛がいやならCLAYにしてください
	public List<Integer[]> inkCanselBlocks = new ArrayList<Integer[]>(); //インクを受け付けないブロック
	public List<Integer[]> fenceBlocks = new ArrayList<Integer[]>(); //フェンス

	public boolean downSteps = true;

	public ikaConfig(){
        File configFile = new File(Splatoon.getInstance().getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
        	Splatoon.getInstance().saveDefaultConfig();
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        blockset = config.getBoolean("blockSet");
        particle = config.getBoolean("particle");
        moveSpeed = (float) config.getDouble("moveSpeed");
        ikaSpeed = (float) config.getDouble("ikaSpeed");
        downSpeed = (float) config.getDouble("downSpeed");
        inkGauge = config.getInt("inkGauge");
        inkHealSpeed = config.getInt("inkHealSpeed");
        inkHeal = config.getInt("inkHeal");
        healDelay = config.getInt("healDelay");
        healSpeed = config.getInt("healSpeed");
        heal = (float) config.getDouble("heal");
        if(config.getString("inkType")!=null){
            if(config.getString("inkType").toUpperCase().equals("WOOL")){
            	inkWool = true;
            }else{
            	inkWool = false;
            }
        }
        if(config.getString("inkCanselBlocks")!=null){
            inkCanselBlocks = getInkCanselBlocks(config.getString("inkCanselBlocks"));
        }else{
        	inkCanselBlocks.add(new Integer[]{0,0});
        }
        downSteps = config.getBoolean("downSteps");
	}

	public List<Integer[]> getInkCanselBlocks(String string){
		List<Integer[]> blocks = new ArrayList<Integer[]>();
		blocks.add(new Integer[]{0,0});
		for(String i:string.split(",")){
			Integer[] ids = new Integer[]{0,0};
			if(Utils.isNumber(i)){
				ids[0]=Integer.valueOf(i);
				blocks.add(ids);
				continue;
			}else{
				if(i.split(":").length >= 2){
					if(Utils.isNumber(i.split(":")[0])){
						ids[0]=Integer.valueOf(i.split(":")[0]);
					}else{
						Splatoon.logger.info("[Config読み込みエラー] "+"inkcanselblocks: "+i.split(":")[0]+" が数字ではありません。");
						continue;
					}
					if(Utils.isNumber(i.split(":")[1])){
						ids[1]=Integer.valueOf(i.split(":")[1]);
					}else{
						Splatoon.logger.info("[Config読み込みエラー] "+"inkcanselblocks: "+i.split(":")[1]+" が数字ではありません。");
						continue;
					}
					blocks.add(ids);
					continue;
				}else{
					Splatoon.logger.info("[Config読み込みエラー] "+"inkcanselblocks: "+i+" の指定が間違えています。ID:subID");
					continue;
				}
			}
		}
		return blocks;
	}

}
