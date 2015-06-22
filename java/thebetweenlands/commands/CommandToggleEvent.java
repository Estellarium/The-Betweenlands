package thebetweenlands.commands;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import thebetweenlands.world.WorldProviderBetweenlands;
import thebetweenlands.world.events.EnvironmentEvent;
import thebetweenlands.world.events.EnvironmentEventRegistry;

public class CommandToggleEvent extends CommandBase {
	private List<String> childCommands = Arrays.asList("toggle", "on", "off", "list");
	private List<String> modifyCommands = Arrays.asList("toggle", "on", "off");

	@Override
	public String getCommandName() {
		return "blevent";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.blevent.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1) {
			throw new CommandException("commands.blevent.usage");
		}
		switch (args[0]) {
		case "toggle":
			processToggle(sender, args);
			break;
		case "on":
			processOn(sender, args);
			break;
		case "off":
			processOff(sender, args);
			break;
		case "list":
			processList(sender);
			break;
		default:
			throw new CommandException("commands.blevent.usage");
		}
	}

	private void checkArg(String[] args, int length, String usage) {
		if (args.length < length) {
			throw new CommandException("commands.blevent.usage." + usage);
		}
	}

	private void processToggle(ICommandSender sender, String[] args) {
		checkArg(args, 2, "toggle");
		String eventName = func_82360_a(sender, args, 1);
		EnvironmentEvent event = getEnvironentEvent(sender, eventName);
		boolean isActive = event.isActive();
		event.setActive(!isActive, true);
		func_152373_a(sender, this, "commands.blevent.success." + (isActive ? "off" : "on"), eventName);
	}

	private void processOn(ICommandSender sender, String[] args) {
		checkArg(args, 2, "on");
		processEventState(sender, func_82360_a(sender, args, 1), true);
	}

	private void processOff(ICommandSender sender, String[] args) {
		if (args.length < 2) {
			EnvironmentEventRegistry environmentEventRegistry = getEnvironmentEventRegistry(sender);
			for (EnvironmentEvent event : environmentEventRegistry.getActiveEvents()) {
				event.setActive(false, true);
			}
			func_152373_a(sender, this, "commands.blevent.success.alloff");
		} else {
			processEventState(sender, func_82360_a(sender, args, 1), false);
		}
	}

	private void processList(ICommandSender sender) {
		EnvironmentEventRegistry environmentEventRegistry = getEnvironmentEventRegistry(sender);
		sender.addChatMessage(new ChatComponentText(environmentEventRegistry.getGrammaticalActiveEventNameList()));
	}

	private void processEventState(ICommandSender sender, String eventName, boolean isActive) {
		EnvironmentEvent event = getEnvironentEvent(sender, eventName);
		if (event.isActive() == isActive) {
			throw new CommandException("commands.blevent.failure.already" + (isActive ? "on" : "off"), eventName);
		} else {
			event.setActive(isActive, true);
			func_152373_a(sender, this, "commands.blevent.success." + (isActive ? "on" : "off"), eventName);
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		List<String> completions = null;
		if (args.length == 1) {
			completions = childCommands;
		} else if (args.length == 2) {
			switch (args[0]) {
			case "toggle":
				completions = getEnvironmentEventRegistry(sender).getEventNames();
				break;
			case "on":
				completions = getEnvironmentEventRegistry(sender).getEventNamesOfState(false);
				break;
			case "off":
				completions = getEnvironmentEventRegistry(sender).getEventNamesOfState(true);
				break;
			}
		}
		return completions == null ? null : getListOfStringsMatchingLastWord(args, completions.toArray(new String[0]));
	}

	private EnvironmentEventRegistry getEnvironmentEventRegistry(ICommandSender sender) {
		World world = sender.getEntityWorld();
		if (world.provider instanceof WorldProviderBetweenlands) {
			return ((WorldProviderBetweenlands) world.provider).getWorldData().getEnvironmentEventRegistry();
		} else {
			throw new CommandException("commands.blevent.failure.wrongdimension");
		}
	}

	private EnvironmentEvent getEnvironentEvent(ICommandSender sender, String eventName) {
		EnvironmentEventRegistry environmentEventRegistry = getEnvironmentEventRegistry(sender);
		EnvironmentEvent event = environmentEventRegistry.forName(eventName);
		if (event == null) {
			throw new CommandException("commands.blevent.failure.unknown", eventName);
		}
		return event;
	}
}
