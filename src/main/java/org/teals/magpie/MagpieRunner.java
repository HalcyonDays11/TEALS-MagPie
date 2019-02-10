package org.teals.magpie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.teals.magpie.discord.DiscordBot;

import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.CommandHandler.SimpleCommand;

public class MagpieRunner {

	public static int MAGPIE_VERSION = 2; // This variable tells the Magpie Runner which version of your code to run.  Valid versions are 2-5.
	public static String DISCORD_TOKEN = ""; // Replace with your own, unique discord token.
	public static boolean COMMAND_LINE_MODE = true; //Set to "false" to connect to Discord, "true" to test in your command line
	
	// this is called from main() if running in command-line mode
	public void watchCommandLine(Magpie maggie) {
		
		System.out.println (maggie.getGreeting());
		Scanner in = new Scanner (System.in);
		String statement = in.nextLine();
		
		while (!statement.equals("Bye")) {
			System.out.println(maggie.getResponse(statement));
			statement = in.nextLine();
		}
		System.exit(0);
	}
	
	// STUDENTS - do not modify anything below this comment.  You do not need to understand what's going on here.
	// this uses some advanced Java code that we won't cover in this class to connect to Discord and register to respond to messages
	
	private static MagpieRunner instance;
	
	private DiscordBot bot;
	private Map<String, SimpleCommand> commandLineModeMap = new HashMap<>();
	
	private boolean setup() throws Exception {
		if(COMMAND_LINE_MODE) {
			return setupInCommandLine();
		}
		
		if(DISCORD_TOKEN.isEmpty()) {
			return false;
		}
		
		bot = new DiscordBot();
		bot.connectToServer(DISCORD_TOKEN);
		
		Reflections reflect = new Reflections("org.teals.magpie.commands");
		Class<CommandExecutor> exec = (Class<CommandExecutor>)Class.forName("de.btobastian.sdcf4j.CommandExecutor");
		Set<Class<? extends CommandExecutor>> subTypesOf = reflect.getSubTypesOf(exec);
		for (Class <? extends CommandExecutor> klass : subTypesOf){
			bot.registerCommand(klass.getConstructor().newInstance());
		}
		return true;
	}
	
	private boolean setupInCommandLine() throws Exception{
		Reflections reflect = new Reflections("org.teals.magpie.commands");
		Class<CommandExecutor> exec = (Class<CommandExecutor>)Class.forName("de.btobastian.sdcf4j.CommandExecutor");
		Set<Class<? extends CommandExecutor>> subTypesOf = reflect.getSubTypesOf(exec);
		CommandHandler commandhandler = new CommandHandler() {
			@Override
			public void registerCommand(CommandExecutor arg0) {
				super.registerCommand(arg0);
			}
		};
		for (Class <? extends CommandExecutor> klass : subTypesOf){
			CommandExecutor commandExecutor = klass.getConstructor().newInstance();
			commandhandler.registerCommand(commandExecutor);
		}
		
		List<SimpleCommand> commands = commandhandler.getCommands();
		commandLineModeMap = commands.stream().collect(Collectors.toMap(cmd -> cmd.getCommandAnnotation().aliases()[0], Function.identity()));
		return true;
	}
	
	
	public void shutdown() {
		bot.disconnect();
	}
	
	public static DiscordBot getDiscordBot() {
		return instance.bot;
	}
	
	public static void main(String[] args) throws Exception {
		instance = new MagpieRunner();
		
		if(COMMAND_LINE_MODE) {
			Magpie maggie = createMagpie();
			if (maggie == null) {
				System.err.println("invalid magpie version!  Valid versions are 2, 3, 4, or 5");
				System.exit(-1);
			}
			instance.watchCommandLine(maggie);
		}else {			
			boolean setup = instance.setup();
			if(!setup) {
				System.exit(-1);
			}
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					instance.shutdown();
				}
			});
		}
	}
	
	public static Magpie createMagpie() {
		switch(MAGPIE_VERSION) {
			case 2: return new Magpie2();
			case 3: return new Magpie3();
			case 4: return new Magpie4();
			case 5: return new Magpie5();
			default: return null;
		}
	}
}
