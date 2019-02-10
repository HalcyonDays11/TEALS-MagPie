package org.teals.magpie.commands;

import org.teals.magpie.MagpieRunner;
import org.teals.magpie.Magpie;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

// STUDENTS - no need to worry about this code.  This is the Discord code to pass the message received to your Magpie code.
public class MagpieCommand implements CommandExecutor{
	
	private static Magpie instance = MagpieRunner.createMagpie();

	@Command(aliases = {"!magpie"})
	public String processCommand(IChannel channel, IUser user, IDiscordClient apiClient, String command, String[] args) {
		return process(String.join(" ", args));
	}
	
	public String process(String command) {
		return instance.getResponse(command);
	}
}
