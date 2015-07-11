package com.github.kotake545.splatoon;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.kotake545.splatoon.util.Utils;

public class ikaConfig {
	public String Version = null;

	public boolean ikaChangeforSneak = false; //イカチェンジ Drop か Sneak か
	public int spectatorTime = 5; //spectatorTime
	public boolean  spectatorMode = false;//

	public boolean changeSwimLength = true;
	public float isSwimLength = 0.1F;

	public boolean blockset = false; //ワールドには書き込まない
	public boolean particle = true; //パーティクルのON/OFF
	public boolean damageEffect = true; //エフェクト

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

	public Hashtable<String,Integer[]> shootPaintSound=new Hashtable<String,Integer[]>();
	public Hashtable<String,Integer[]> usePaintSound=new Hashtable<String,Integer[]>();

	public boolean downSteps = true;

	public String defaultClass = "";

	public double deadInkRadius = 3.0D;

	public boolean changeColorGlass = false;

	public ikaConfig(){
		try {
			Object obj = Bukkit.getServer().getClass().getDeclaredMethod("getServer", new Class[0]).invoke(Bukkit.getServer(), new Object[0]);
			Object propertyManager = obj.getClass().getDeclaredMethod("getPropertyManager", new Class[0]).invoke(obj, new Object[0]);
			Version = propertyManager.getClass().getPackage().getName();
		} catch (Exception e){
			e.printStackTrace();
		}
        File configFile = new File(Splatoon.getInstance().getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
        	Splatoon.getInstance().saveDefaultConfig();
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        spectatorMode = config.getBoolean("spectatorMode");
        spectatorTime = config.getInt("spectatorTime");
        deadInkRadius = config.getDouble("deadInkRadius");
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
        defaultClass = config.getString("defaultClass");
        damageEffect = config.getBoolean("damageEffect");
        changeColorGlass = config.getBoolean("changeColorGlass");
        if(config.getString("inkType")!=null){
            if(config.getString("inkType").toUpperCase().equals("WOOL")){
            	inkWool = true;
            }else{
            	inkWool = false;
            }
        }
        if(config.getString("ikaChangeEvent")!=null){
            if(config.getString("ikaChangeEvent").toUpperCase().equals("SNEAK")){
            	ikaChangeforSneak = true;
            }else{
            	ikaChangeforSneak = false;
            }
        }
        if(config.getString("inkCanselBlocks")!=null){
            inkCanselBlocks = getInkCanselBlocks(config.getString("inkCanselBlocks"));
        }else{
        	inkCanselBlocks.add(new Integer[]{0,0});
        }
        shootPaintSound = IkaWeapon.getSoundData(config.getString("shootPaintSounds"));
        usePaintSound = IkaWeapon.getSoundData(config.getString("usePaintSounds"));
        downSteps = config.getBoolean("downSteps");
	}

	public List<Integer[]> getInkCanselBlocks(String string){
		List<Integer[]> blocks = new ArrayList<Integer[]>();
		blocks.add(new Integer[]{0,0});
		for(String i:string.split(",")){
			Integer[] ids = new Integer[]{0,0};
			if(Utils.isNumber(i)){
				ids[0]=Integer.valueOf(i);
				ids[1]=-1;
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
