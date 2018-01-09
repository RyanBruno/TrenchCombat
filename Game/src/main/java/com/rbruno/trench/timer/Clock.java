package com.rbruno.trench.timer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitScheduler;

import com.rbruno.trench.Game;
import com.rbruno.trench.game.EngineGame;
import com.rbruno.trench.game.GameType;

public class Clock {

	private int gameClock = 5;

	public Clock() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Game.getPlugin(), new Runnable() {
			@Override
			public void run() {
				tick();
			}
		}, 0L, 20L);
	}

	private void tick() {
		switch (Game.getPlugin().getGameState()) {
		case LOADING:
			if (gameClock <= 0) {
				Game.getPlugin().setGameState(GameState.PLAYING);
				startGame();
				gameClock = Game.trenchConfig.getGameClock() * 60;
			}
			if (Bukkit.getOnlinePlayers().size() > 0) gameClock--;
			break;		
		case PLAYING:
			if (Game.game == null) startGame();
			spawnFirework();
			if (gameClock > 0) gameClock--;
			String second = gameClock % 60 + "";
			if (gameClock % 60 <= 9) second = "0" + second;
			String time = (gameClock - (gameClock % 60)) / 60 + ":" + second;
			if (Game.getGame() != null) Game.getGame().objective.setDisplayName(ChatColor.YELLOW + "Time: " + ChatColor.WHITE + time);
			if (gameClock % 60 == 0 && gameClock != 0) Game.broadcast(ChatColor.YELLOW + "" + (gameClock / 60) + ChatColor.WHITE + " minutes left in game!");
			if (gameClock <= 0) endGame();
			break;
		case ENDED:
			if (Bukkit.getOnlinePlayers().isEmpty()) {
				Game.broadcast("Players moved successfully!");
				Bukkit.shutdown();
			} else {
				if (gameClock > 10) {
					Game.broadcast("There was an error moving you to the game server!");
				}
				gameClock++;
			}
			break;
		default:
			break;
		}
	}

	public void startGame() {
		gameClock = Game.trenchConfig.getGameClock() * 60;
		Game.game = new EngineGame(GameType.CTF);
		Game.getGame().pickTeams();
		Game.getGame().tpPlayers();
		Game.getGame().giveItems();
		Game.broadcast("The war has begun!");
		Game.getPlugin().setGameState(GameState.PLAYING);
		for (Player player : Bukkit.getOnlinePlayers()) {
			Game.game.kills.put(player, 0);
		}

	}

	public void endGame() {
		gameClock = Game.trenchConfig.getGameClock() * 60;
		if (Game.getGame().getBlueTeam().getScore() == Game.getGame().getRedTeam().getScore()) {
			Game.broadcast("The game ended in a " + ChatColor.YELLOW + "Tie" + ChatColor.WHITE + "!");
		} else if (Game.getGame().getBlueTeam().getScore() > Game.getGame().getRedTeam().getScore()) {
			Game.broadcast("The game had ended and the  " + ChatColor.BLUE + "Blue " + ChatColor.WHITE + "team won!");
		} else {
			Game.broadcast("The game had ended and the  " + ChatColor.RED + "Red " + ChatColor.WHITE + "team won!");
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
			    out.write("Connect".getBytes("UTF-8"));
			    out.write("lobby".getBytes("UTF-8"));
			}catch(IOException e) {
			    e.printStackTrace();
			}
			player.sendPluginMessage(Game.getPlugin(), "BungeeCord", out.toByteArray());
		}
	}

	private void spawnFirework() {
		if (!(Game.getGame().getRedTeam().getFlagHolder() == null)) {
			Firework firework = (Firework) Game.getGame().getRedTeam().getFlagHolder().getWorld().spawnEntity(Game.getGame().getRedTeam().getFlagHolder().getLocation(), EntityType.FIREWORK);
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.RED).with(Type.BALL_LARGE).trail(true).build();
			fireworkMeta.addEffect(effect);
			fireworkMeta.setPower(1);
			firework.setFireworkMeta(fireworkMeta);

		}
		if (!(Game.getGame().getBlueTeam().getFlagHolder() == null)) {
			Firework firework = (Firework) Game.getGame().getBlueTeam().getFlagHolder().getWorld().spawnEntity(Game.getGame().getBlueTeam().getFlagHolder().getLocation(), EntityType.FIREWORK);
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.BLUE).with(Type.BALL_LARGE).trail(true).build();
			fireworkMeta.addEffect(effect);
			fireworkMeta.setPower(1);
			firework.setFireworkMeta(fireworkMeta);

		}
	}
	

	public int getGameClock() {
		return gameClock;
	}


}