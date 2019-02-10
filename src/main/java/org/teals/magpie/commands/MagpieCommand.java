package org.teals.magpie.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class MagpieCommand implements CommandExecutor{

	@Command(aliases = {"!magpie"})
	public String processCommand(IChannel channel, IUser user, IDiscordClient apiClient, String command, String[] args) {
		command = command.replaceFirst("\\!magpie ", "");
		return process(command);
	}
	
	
	//Do your work here:
	public String process(String command) {
		
		return "command: " + command;
	}
}
