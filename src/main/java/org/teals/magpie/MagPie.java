package org.teals.magpie;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.teals.magpie.discord.DiscordBot;

import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.CommandHandler.SimpleCommand;

public class MagPie {

	public static String DISCORD_TOKEN = ""; //Replace with your own, unique discord token.
	public static boolean COMMAND_LINE_MODE = true; //Set to "false" to connect to Discord, "true" to test in your command line
	
	private static MagPie instance;
	
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
	
	public void watchCommandLine() {
		Scanner scanner = new Scanner(System.in);
		boolean running = true;
		while(running) {
			String nextLine = scanner.nextLine();
			if(nextLine.startsWith("!exit")) {
				running = false;
				continue;
			}
			
			int firstSpace = nextLine.indexOf(" ");
			String commandPrefix;
			if(firstSpace > 0) {				
				commandPrefix = nextLine.substring(0, firstSpace);
			}else {
				commandPrefix = nextLine;
			}
			Optional<Entry<String, SimpleCommand>> found = commandLineModeMap.entrySet().stream().filter((entry) -> entry.getKey().equals(commandPrefix)).findAny();
			if(found.isPresent()) {
				SimpleCommand actualCommand = found.get().getValue();
				try {
					String response = (String)actualCommand.getMethod().invoke(actualCommand.getExecutor(), null, null, null, nextLine, null);
					System.out.println(response);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}else {
				System.out.println("Unknown command!");
			}
		}
		scanner.close();
		System.exit(0);
	}
	
	public void shutdown() {
		bot.disconnect();
	}
	
	public static DiscordBot getDiscordBot() {
		return instance.bot;
	}
	
	public static void main(String[] args) throws Exception {
		instance = new MagPie();
		boolean setup = instance.setup();
		if(!setup) {
			System.exit(-1);
		}
		
		if(COMMAND_LINE_MODE) {
			instance.watchCommandLine();
		}else {			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					instance.shutdown();
				}
			});
		}
	}
}
