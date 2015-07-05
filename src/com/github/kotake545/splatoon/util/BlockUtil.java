package com.github.kotake545.splatoon.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.Splatoon;

public class BlockUtil {
	public Hashtable<Block,Integer[]> Blocks;

	public BlockUtil() {
		Blocks = new Hashtable<Block,Integer[]>();
	}

	/**
	 * int X Y Z world でチェックするようにしてる過程でどんどん汚くなったのであとで整理
	 */

	@SuppressWarnings("deprecation")
	public void setBlock(Location loc,Integer[] block){
		if(block!=null){
			if(containsCanselBlocks(getIntegerBlock(loc))){
				return;
			}
			if(Splatoon.ikaConfig.blockset){
				setBeforeBlock(loc.getBlock());
				loc.getBlock().setTypeIdAndData(block[0],(byte)(int)block[1],true);
			}else{
				setPacketBlock(loc.getBlock(), block);
			}
		}
	}

	public static List<Location> getPaintVerticalLine(final Location loc,final Vector vector,int add,float distance){
		List<Location> locations = new ArrayList<Location>();
		vector.setY(0);
		Location location = loc.clone();
		for(int i=0;i<add;i++){
			if(vector!=null){
				location.add(vector);
			}
			float radius = distance/2;

			Location[] sub = new Location[]{null,null};
	    	double check = 2;
	    	for (double i1 = 0.0; i1 < check; i1 ++) {
	            double angle = (double) 2 * Math.PI * i1 / check;
	        	float radian = (float) (Math.PI / 180);
	            Vector v = new Vector(Math.cos(angle) * radius,0, Math.sin(angle) * radius);

	            rotateAroundAxisX(v, (location.getPitch() + 90) * radian);
	            rotateAroundAxisY(v, -location.getYaw() * radian);

	            location.add(v);
	            sub[(int) i1]=location.clone();
	            location.subtract(v);
	    	}
	    	Location start = sub[0];
	    	Location end = sub[1];
	    	//2点感を繋いでlocationsに入れる
	    	locations.addAll(getLine(start,end));
		}
    	return locations;
	}

	public static List<Location> getLine(Location from,Location to){
		List<Location> locations = new ArrayList<Location>();
		Location location = from.clone();
		int count=(int) to.distance(from)*2;
		to.subtract(from.getX(),from.getY(),from.getZ());
		double d=1D/count;
		double x=to.getX()*d;
		double z=to.getZ()*d;
		for(int i=0;i<=count;i++){
			location.setX(from.getX()+x*i);
			location.setZ(from.getZ()+z*i);
			locations.add(location.clone());
		}
		return locations;
	}

	public static List<Location> getPaintSphere(Location loc,double radius){
		List<Location> locations = new ArrayList<Location>();
		double maxHeight = 999.0D;
		int a = (int) radius;
//		if(vector!=null){
//			loc.add(vector);
//		}
		for (int x = -a; x <= a; ++x) {
			for (int z = -a; z <= a; ++z){
				for (int y = -a; y <= a; ++y){
					if (Math.abs(y) > maxHeight) {
						continue;
					}
					Location location = new Location(loc.getWorld(),(int)(loc.getX() + x), (int)(loc.getY() + y), (int)(loc.getZ() + z));
					double offset = getOffset(loc,location.add(0.5D, 0.5D, 0.5D));
					if (offset > radius){
						continue;
					}
					if(locations.contains(location)){
						continue;
					}
					locations.add(location);
				}
			}
		}
		return locations;
	}

	public static double getOffset(Location loc, Location b){
		return loc.toVector().subtract(b.toVector()).length();
	}
	public static boolean containsCanselBlocks(Integer[] block){
		if(block[0]==0){
			return true;
		}
		for(Integer[] blocks:Splatoon.ikaConfig.inkCanselBlocks){
			if(block[0].equals(blocks[0])){
				if(block[1].equals(blocks[1])||blocks[1].equals(-1)){
					return true;
				}
			}
		}
		return false;
	}
	public void setPacketBlock(Block block,Integer[] set){
		for(Block b:Blocks.keySet()){
			if(b.getX()==block.getX()&&b.getY()==block.getY()&&b.getZ()==block.getZ()&&b.getWorld()==block.getWorld()){
				Blocks.put(b,set);
			}
		}
		Splatoon.blockUtil.Blocks.put(block,set);
	}

	public boolean setBeforeBlock(Block block){
		if(!containsBlock(block)){
			Integer[] b = new Integer[]{0,0};
			b[0] = block.getTypeId();
			b[1] = (int)block.getData();
			Blocks.put(block,b);
			return true;
		}
		return false;
	}
	public boolean containsBlock(Block block){
		for(Block b:Blocks.keySet()){
			if(b.getX()==block.getX()&&b.getY()==block.getY()&&b.getZ()==block.getZ()&&b.getWorld()==block.getWorld()){
				return true;
			}
		}
		return false;
	}

	public Integer[] getIntegerBlock(Location loc) {
		Integer[] block = new Integer[]{0,0};
		block[0] =loc.getBlock().getTypeId();
		block[1] =(int) loc.getBlock().getData();
		return block;
	}

	public Integer[] getBlock(Block block) {
		for(Block b:Blocks.keySet()){
			if(b.getX()==block.getX()&&b.getY()==block.getY()&&b.getZ()==block.getZ()&&b.getWorld()==block.getWorld()){
				return Blocks.get(b);
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public int returnBeforeBlocks(){
		int ba = 0;
		if(Blocks.size()>0){
			if(Splatoon.ikaConfig.blockset){
				int i = 0;
				for(Block block:Blocks.keySet()){
					Integer[] b = Blocks.get(block);
					block.setTypeIdAndData(b[0],(byte)(int)b[1],true);
					i++;
				}
				Blocks = new Hashtable<Block, Integer[]>();
				Splatoon.logger.info(i+"つ のブロックの修復に成功しました。");
				ba=i;
			}else{
				for(Player player:Splatoon.getOnlinePlayers()){
					for(Block block:Blocks.keySet()){
						Block a = block.getLocation().getBlock();
						player.sendBlockChange(block.getLocation(),a.getTypeId(),a.getData());
					}
				}
				Blocks = new Hashtable<Block, Integer[]>();
			}
			return ba;
		}
		return ba;
	}
	/**
	 * フェンス抜け
	 * 抜けるプレイヤー以外にはフェンスが存在するパケットを送り続ける。
	 * 抜けるプレイヤーにはフェンスが映らない Worldの方も0 にしないとチート検知に引っかかるので
	 * Blocksに登録しておくこと
	 */
//	public static boolean isFence(Block block){
//		if(Splatoon.ikaConfig.fenceBlocks.size()<=0){
//			return false;
//		}
//		Integer[] a = new Integer[]{0,0};
//		a[0]=block.getTypeId();
//		a[1]=(int) block.getData();
//		if(Splatoon.ikaConfig.fenceBlocks.contains(a)){
//			return true;
//		}else{
//			return false;
//		}
//	}


//	public static List<Block> getCircleBlocks(Location loc,int radius){
//		List<Block> blocks = new ArrayList<Block>();
//		Block block = loc.getBlock();
//		Location location = new Location(block.getWorld(),block.getX()-0.5,block.getY(),block.getZ()+0.5);
//		Location first = location.clone();
//        int x = 0;
//        int y = location.getBlockY();
//        int z = 0;
//        for(int r = 0; r < radius; r++){
//        	/**
//        	 * 円を一周描く
//        	 */
//        	double check = 0;
//        	if(0==r)check=1;
//        	if(1==r)check=4;
//        	//最小限の数で処理したいので7までifでチェック。法則性あれば解明したい
//        	if(2==r)check=16;
//        	if(3==r)check=70;
//        	if(4<=r&&r<=5)check=32;
//        	if(6==r)check=64;
//        	if(7==r)check=64;
//        	if(8<=r)check=360;
//        	for (double i = 0.0; i < check; i ++) {
//            	double angle = (double) 2 * Math.PI * i / check;
//                x = (int)(location.getX() + r * Math.cos(angle));
//                z = (int)(location.getZ() + r * Math.sin(angle));
//
//                Block bloc2 = location.getWorld().getBlockAt(x,y,z);
//                if(!blocks.contains(bloc2)){
//                	blocks.add(bloc2);
//                }
//            }
//        }
//        return blocks;
//    }
	/**
//	 * Test
//	 * Vector vector に垂直な円を Location lastloc を中心に 半径 radius 生成するロジック考察テスト
//	 */
//	public static void Test(Location lastloc,Vector vector,float radius){
//		Location location = lastloc.clone();
//		location.add(vector);
//
//    	double check = 2;
//    	for (double i = 0.0; i < check; i ++) {
//            double angle = (double) 2 * Math.PI * i / check;
//        	float radian = (float) (Math.PI / 180);
//            Vector v = new Vector(Math.cos(angle) * radius, i, Math.sin(angle) * radius);
//
//            rotateAroundAxisX(v, (location.getPitch() + 90) * radian);
//            rotateAroundAxisY(v, -location.getYaw() * radian);
//
//            location.add(v);
//            ParticleAPI.sendAllPlayer(EnumParticle.VILLAGER_HAPPY,location, 0, 0, 0, 0, 1);
//            location.subtract(v);
//    	}
//	}
	public static final Vector rotateAroundAxisX(Vector v, double angle) {
		double y, z, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		y = v.getY() * cos - v.getZ() * sin;
		z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}

	public static final Vector rotateAroundAxisY(Vector v, double angle) {
		double x, z, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		x = v.getX() * cos + v.getZ() * sin;
		z = v.getX() * -sin + v.getZ() * cos;
		return v.setX(x).setZ(z);
	}

	public static final Vector rotateAroundAxisZ(Vector v, double angle) {
		double x, y, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		x = v.getX() * cos - v.getY() * sin;
		y = v.getX() * sin + v.getY() * cos;
		return v.setX(x).setY(y);
	}
	public static final Vector rotateVector(Vector v, double angleX, double angleY, double angleZ) {
		rotateAroundAxisX(v, angleX);
		rotateAroundAxisY(v, angleY);
		rotateAroundAxisZ(v, angleZ);
		return v;
	}
}
