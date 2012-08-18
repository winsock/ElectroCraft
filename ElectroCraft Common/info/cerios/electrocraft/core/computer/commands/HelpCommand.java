package info.cerios.electrocraft.core.computer.commands;

import java.io.IOException;

import info.cerios.electrocraft.core.computer.Computer;

public class HelpCommand implements IComputerCommand {
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		try {
			computer.getTerminal().writeLine("Cerios Help:\n Command - Help Text");
			for (ComputerCommands command : ComputerCommands.values()) {
				computer.getTerminal().writeLine(command.getCommandText() + " - " + command.getHelpText());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
