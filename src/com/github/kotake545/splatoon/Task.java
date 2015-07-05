package com.github.kotake545.splatoon;

public class Task {
	private int ticks;
	public IkaStage gameStage = null;

	public Task() {
		this.ticks = 0;
		this.gameStage = null;
	}

	public void timer(){
		ticks++;
		if(ticks%20==0){
			if(gameStage!=null){
				gameStage.timer();
			}
		}
	}

	public String getGameStatus(){
		if(gameStage!=null){
			if(gameStage.gameStartActive)return "gamestart";
			if(gameStage.countDownActive)return "countdown";
			if(gameStage.gameTimeActive)return "gametime";
		}
		return "false";
	}
	public boolean gameStart(IkaStage stage){
		if(gameStage!=null){
			return false;
		}
		stage.onGameStart();
		this.gameStage=stage;
		return true;
	}

	public boolean gameEnd() {
		if(gameStage!=null){
			gameStage.onFinish();
			return true;
		}
		return false;
	}
}
