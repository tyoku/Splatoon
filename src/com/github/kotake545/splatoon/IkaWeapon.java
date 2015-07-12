package com.github.kotake545.splatoon;

import java.util.Hashtable;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.Packet.ParticleAPI;
import com.github.kotake545.splatoon.Packet.ParticleAPI.EnumParticle;
import com.github.kotake545.splatoon.util.BlockUtil;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;
import com.github.kotake545.splatoon.util.Utils;


public class IkaWeapon {
	public String weaponName;
	public Integer[] weaponID;
	public String weaponType;

	public String rightClick = "use";
	public String leftClick = "shoot";
	public boolean DisableFirstUse=false; //shootを使ってからでないとuseが使えなくなります。

	public int miningFatigue;
	public int Mdelay;
	public boolean miningDelay;
	public int needInk;
	public int distance; //塗る範囲 長さ
	public int useDelay; //使える間隔
	public float addMoveSpeed;
	public double damage;
	public Hashtable<String,Integer[]> sound = new Hashtable<String, Integer[]>();

	public double shootReaction;
	public double shootRecoil;
	public double shootKnockback;
	public double shootSpeed;
	public int shootNeedInk;
	public int shootDistance;
	public double moveFinishMultiply = 0.25;
	public double hitRadius;
	public int shootDelay;
	public double shootDamage;
	public double accuracy;
	public int canScope;
	public int shootBurst;
	public Hashtable<String,Integer[]> shootSound = new Hashtable<String, Integer[]>();
	public boolean fullAuto;

	//##############################################

	private ItemStack itemStack;
	private int ticks;
	public IkaPlayerInfo ika;
	private String lastClickType = "";

	private int lastShoot = 0;
	private boolean shoot = false;
	private int Scheck =0;
	private int Sdelay;

	private int DisableFirstUseDelay = 0;

	private boolean use = false;
	private int Udelay;

	public void Sfinish(){
		Sdelay = shootDelay;
		shoot = false;
		//打ち終わった後しばらくの時間 DisableFirstShoot の処理を受け付ける
		if(DisableFirstUse){
			DisableFirstUseDelay = shootDelay+useDelay;
		}
	}

	public void Ufinish(){
		//
		Udelay = useDelay;
		use = false;
	}
	public void shoot(){
		Player ikaPlayer = ika.getPlayer();
		if(ika!=null&&ikaPlayer.isOnline()){
			if(DisableFirstUse){
				if(DisableFirstUseDelay>0){
					return;
				}
			}
			Location loc = ikaPlayer.getLocation();
			if(checkInk(shootNeedInk)){
				useInk(shootNeedInk);

				onRecoil(shootRecoil);
				onReaction(shootReaction);
				for(String name:shootSound.keySet()){
					Utils.playSound(name,shootSound.get(name), loc);
				}
				for(int i = 0; i<shootBurst; i++){
					int acc = (int)(accuracy * 1000.0D);
					if(acc <= 0){
						acc = 1;
					}
					Random rand = new Random();
					double dir = -loc.getYaw() - 90.0F;
					double pitch = -loc.getPitch();
					double xwep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
					double ywep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
					double zwep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
					double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + xwep;
					double yd = Math.sin(Math.toRadians(pitch)) + ywep;
					double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + zwep;
					Vector vec = new Vector(xd, yd, zd);
					vec.multiply(shootSpeed);
					ProjectileInfo pi = new ProjectileInfo(ika,this, vec);
					Splatoon.projectileManager.addProjectileInfo(pi);
				}
				Sfinish();
			}else{//インク不足
				ikaPlayer.playSound(loc, Sound.CLICK, 1.0F, 2.0F);
				ikaPlayer.getPlayer().sendMessage(ChatColor.RED+"インクが足りません");
				Sfinish();
				if(DisableFirstUse){
					DisableFirstUseDelay=0;
					use=false;
				}
				Sdelay = 20;
			}
		}
	}

	public void use(){
		Player ikaPlayer = ika.getPlayer();
		if(ika!=null&&ikaPlayer.isOnline()){
			Location loc = ikaPlayer.getLocation();
			if(checkInk(needInk)){
				//バッシャンしてからじゃないと使えない場合
				if(DisableFirstUse){
					if(DisableFirstUseDelay<=0){
						if(miningFatigue>0){
							onShootDelay();
						}else{
							onShoot();
						}
						return;
					}
					//使ってる間はずっと使えるようにしておく
					DisableFirstUseDelay = shootDelay+useDelay;
				}
				if(!ikaPlayer.isOnGround()){
					return;
				}
				useInk(needInk);
				for(String name:this.sound.keySet()){
					Utils.playSound(name,sound.get(name), loc);
				}
				//start を使って塗る
				Integer[] a = ika.getTeamBlock();
				if(a==null){
					return;
				}
				Hashtable<LivingEntity,Boolean> near=new Hashtable<LivingEntity, Boolean>();
				for(Entity n : ikaPlayer.getNearbyEntities(this.distance, this.distance, this.distance)){
					if(n instanceof LivingEntity){
						near.put((LivingEntity) n,false);
					}
				}
				for (Location bloc : BlockUtil.getPaintVerticalLine(loc,loc.getDirection(),2,this.distance)){
					for(LivingEntity entity:near.keySet()){
						if(near.get(entity)){
							continue;
						}
						if(bloc.distance(entity.getLocation())<=1){
							near.put(entity,true);
						}
					}
					ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_DUST.setItemIDandData(a[0], a[1]),bloc,0.1F,0.1F,0.1F,0,3);
					bloc.setY(loc.getY()-0.1);
					//ポイント追加
					if(Splatoon.blockUtil.setBlock(bloc,a)){
						ika.point+=1;
					}
				}
				for(LivingEntity entity:near.keySet()){
					if(near.get(entity)){
						onUseDamage(entity);
					}
				}
				Ufinish();
			}else{//インク不足
				ikaPlayer.playSound(loc, Sound.CLICK, 1.0F, 2.0F);
				ikaPlayer.getPlayer().sendMessage(ChatColor.RED+"インクが足りません");
				Ufinish();
				Udelay = 20;
			}
		}
	}

	public boolean onReaction(final double reaction){
		if(reaction != 0.0D){
			Bukkit.getScheduler().runTaskLater(Splatoon.instance, new Runnable() {
				@Override
				public void run() {
					Player player = ika.getPlayer();
					Vector v = player.getVelocity();
					Location pl = player.getLocation();
					pl.setPitch((float) (pl.getPitch()-reaction));
					player.teleport(pl);
					player.setVelocity(v);
					}
			},1);
		}
		return true;
	}

	public boolean onRecoil(double recoil){
		if(recoil != 0.0D){
			Player player = ika.getPlayer();
			Location ploc = player.getLocation();
			double dir = -ploc.getYaw() - 90.0F;
			double pitch = - ploc.getPitch() - 180.0F;
			double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
			double yd = Math.sin(Math.toRadians(pitch));
			double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
			Vector vec = new Vector(xd, yd, zd);
			vec.multiply(recoil / 2.0D).setY(0);
			player.setVelocity(player.getVelocity().add(vec));
		}
		return true;
	}

	public void onUseDamage(LivingEntity entity){
		entity.damage(damage,ika.getPlayer());
	}

	public double getRandomAccuracy(Random rand){
		double r = 1000.0D;
		int a = (int)(accuracy * r);
		if(a<=0){
			a = 1;
		}
		double randaccuracy = (rand.nextInt(a) - rand.nextInt() + 0.5D) / r;
		return randaccuracy;
	}

	public void onClick(String type) {
		if(type.equals("right")){
			if(rightClick.equals("shoot")){
				Scheck += 1;
				lastShoot = 0;
				onShoot();
			}else
			if(rightClick.equals("use")){
				onUse();
			}
		}else
		if(type.equals("left")){
			if(leftClick.equals("shoot")){
				Scheck = 0;
				if(miningFatigue>0){
					onShootDelay();
				}else{
					onShoot();
				}
			}else
			if(leftClick.equals("use")){
				onUse();
			}
		}
	}
	public void onShoot(){
		if(Sdelay<=0){
			shoot = true;
		}
	}
	public void onShootDelay(){
		if(miningDelay){
			//予約済みの時
		}else{
			Mdelay=miningFatigue;
			miningDelay=true;
		}
	}
	public void onUse(){
		if(Udelay<=0){
			use = true;
		}
	}
	public void tick() {
		if(ika!=null){
			if(ika.isIka()||ika.isSpectator()){
				use=false;
				shoot=false;
				miningDelay=false;
			}
			if(DisableFirstUse){
				DisableFirstUseDelay-=1;
			}
//			ika.getPlayer().sendMessage("active");
			//Delayの更新とかshootとかここで管理
			ticks += 1;
			Sdelay -= 1;
			Udelay -= 1;
			Mdelay -= 1;
			//予約されていた時
			if(miningDelay&&Mdelay<=0){
				shoot();
				miningDelay=false;
			}
			lastShoot += 1;
			if(lastShoot > 6){
				Scheck = 0;
			}
			if(fullAuto&&Scheck==1){
				Scheck = 2;
			}
			//撃つ
			if(shoot||Scheck >= 2 && Sdelay <= 0){
				shoot();
//				Sfinish();
				return;
			}
			//塗る
			if(use){
				use();
			}
			onReName();
		}
	}

	public IkaWeapon clone(){
		IkaWeapon clone = new IkaWeapon();
		clone.weaponName=this.weaponName;
		clone.weaponID=this.weaponID;
		clone.weaponType=this.weaponType;
		clone.rightClick=this.rightClick;
		clone.leftClick=this.leftClick;
		clone.miningFatigue=this.miningFatigue;
		clone.needInk=this.needInk;
		clone.distance=this.distance;
		clone.useDelay=this.useDelay;
		clone.addMoveSpeed=this.addMoveSpeed;
		clone.damage=this.damage;
		clone.sound=this.sound;
		clone.shootSpeed=this.shootSpeed;
		clone.shootNeedInk=this.shootNeedInk;
		clone.hitRadius=this.hitRadius;
		clone.shootDelay=this.shootDelay;
		clone.shootDistance=this.shootDistance;
		clone.shootDamage=this.shootDamage;
		clone.accuracy=this.accuracy;
		clone.canScope=this.canScope;
		clone.shootBurst=this.shootBurst;
		clone.shootSound=this.shootSound;
		clone.fullAuto=this.fullAuto;
		clone.moveFinishMultiply=this.moveFinishMultiply;
		clone.DisableFirstUse=this.DisableFirstUse;
		clone.shootReaction=this.shootReaction;
		clone.shootRecoil=this.shootRecoil;
		clone.shootKnockback=this.shootKnockback;
		return clone;
	}

	public boolean useInk(int need){
		if(checkInk(need)){
			ika.inkGauge-=need;
			return true;
		}else{
			return false;
		}
	}

	public boolean checkInk(int need){
		if(ika.inkGauge>=need){
			return true;
		}else{
			return false;
		}
	}

	public int getShootDistance(){
		return shootDistance;
	}

	public double getHitRadius() {
		return hitRadius;
	}

	public void setWeaponID(String set) {
		Integer[] id = new Integer[]{0,0};
		if(set.indexOf(":") <= 0){
			id[0]=Integer.parseInt(set);
			weaponID=id;
			return;
		}
		String[] args=set.split(":");
		id[0]=Integer.parseInt(args[0]);
		id[1]=Integer.parseInt(args[1]);
		weaponID=id;
		return;
	}

	public boolean onReName(){
		if(itemStack!=null){
			if(0<=Sdelay){
				setName(itemStack, this.weaponName+getDelay(Sdelay,shootDelay,ScoreBoardUtil.ColorReplace(ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),ika.getPlayer()).getName())));
				return true;
			}
			//何もせずに返す
			if(DisableFirstUseDelay>=0&&DisableFirstUse){
				setName(itemStack, this.weaponName);
				return true;
			}
			//予備動作時間
			if(0<=Mdelay&&miningDelay){
				setName(itemStack, this.weaponName+getDelay(Mdelay,miningFatigue,ScoreBoardUtil.ColorReplace(ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),ika.getPlayer()).getName())));
				return true;
			}
			//次に撃てるまでの時間
			if(checkInk(shootNeedInk)){
				if(0<=Sdelay){
					setName(itemStack, this.weaponName+getDelay(Sdelay,shootDelay,ScoreBoardUtil.ColorReplace(ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),ika.getPlayer()).getName())));
					return true;
				}
			}else{
				setName(itemStack, this.weaponName);
			}
		}
		return false;
	}

	private String getDelay(int timer,int time,ChatColor chatColor){
		double maxlength = 50;
		double a = timer;
		int b = (int) ((time-a)/time*maxlength);
		if(b<0){
			b=0;
		}
		int c = (int) (maxlength-b);
		String a1 = chatColor+"";
		String b1 = ChatColor.DARK_GRAY+"";
		for(int i=0;i<b;i++){
			a1+="│";
		}
		for(int i=0;i<c;i++){
			b1+="│";
		}
		String c1 =ChatColor.RESET+"";
		return  c1+" ["+a1+b1+c1+"]";
	}

	public void setSound(String set) {
		this.sound=getSoundData(set);
	}

	public static Hashtable<String,Integer[]> getSoundData(String sound){
		Hashtable<String,Integer[]> Sounds=new Hashtable<String, Integer[]>();
		for(String deta:sound.split(",")){
			String name;
			Integer[] integers = new Integer[]{0,0};
			if(deta.indexOf("/") <= 0){
				name=deta;
			}else{
				name=deta.split("/")[0];
				integers[0]=Integer.parseInt(deta.split("/")[1]);
				integers[1]=Integer.parseInt(deta.split("/")[2]);
			}
			Sounds.put(name, integers);
		}
		return Sounds;
	}

	public void setShootSound(String set) {
		this.shootSound=getSoundData(set);
	}

	public String getName() {
		return weaponName;
	}

	public double getDamage(){
		return damage;
	}

	public double getShootDamage() {
		return this.shootDamage;
	}

	@SuppressWarnings("deprecation")
	public ItemStack getItemStack(){
		ItemStack itemStack = new ItemStack(weaponID[0]);
		itemStack.setDurability((byte)(int)weaponID[1]);
		itemStack = setName(itemStack,weaponName/*,wrapWords(description,40)*/);

		return itemStack;
	}

	  public static ItemStack setName(ItemStack is, String name/*, List<String> descripList*/)
	  {
	    ItemMeta m = is.getItemMeta();
	    m.setDisplayName(name);
//	    if ((descripList != null) && (descripList.size() > 0)) {
//	      m.setLore(descripList);
//	    }
	    is.setItemMeta(m);
	    return is;
	  }

	public void setItemStack(ItemStack hand) {
		this.itemStack = hand;
	}
}
