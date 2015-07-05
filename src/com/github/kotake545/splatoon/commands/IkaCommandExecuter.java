package com.github.kotake545.splatoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class IkaCommandExecuter implements CommandExecutor {
	@Override
	abstract public boolean onCommand(CommandSender sender, Command command, String paramString,String[] args);

}