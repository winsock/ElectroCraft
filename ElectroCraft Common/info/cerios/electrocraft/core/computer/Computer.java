package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.computer.commands.ComputerCommands;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import cpw.mods.fml.common.FMLCommonHandler;

public class Computer implements Runnable {
	
	private boolean isInternal = true;
	private volatile boolean running = false;
	private final String bootScript;
	private SoundCard soundCard;
	private Terminal terminal;
	private VideoCard videoCard;
	private Keyboard keyboard;
	
	public Computer(String script, boolean isInternal, int width, int height, int rows, int columns) {
		this.isInternal = isInternal;
		this.soundCard = new SoundCard();
		this.videoCard = new VideoCard();

		this.terminal = new Terminal(rows, columns);
		this.keyboard = new Keyboard(terminal);

		bootScript = script;
	}
	
	public void setRunning(boolean value) {
		running = value;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public SoundCard getSoundCard() {
		return soundCard;
	}
	
	public Terminal getTerminal() {
		return terminal;
	}
	
	public VideoCard getVideoCard() {
		return videoCard;
	}
	
	public Keyboard getKeyboard() {
		return keyboard;
	}
	
	@Override
	public void run() {
		try {
			terminal.write("Booting Cerios");
			for (int i = 0; i < 20; i++) {
				terminal.write(".");
				Thread.sleep(50);
			}
			terminal.clear();
			terminal.writeLine("Welcome to Cerios!");
			terminal.writeLine("Cerios - A Minecraft written by Andrew Querol");
			terminal.writeLine("To get started type help!");
			while (running) {
				// Write the terminal prompt
				terminal.write("> ");
				
				String line = "";
				char chr = '\0';
				while (true) {
					if (chr == '\n' || chr == '\r')
						break;
					if (chr == '\b') {
						if (line.length() > 0) {
							line = line.substring(0, line.length() - 1);
							terminal.deleteChar(false);
						}
					} else if (chr != '\0') {
						line += chr; // Add the old char
					}
					chr = keyboard.waitForKey(); // Pop the new one
				}
				
				String[] commandArgs = line.split(" ");
				if (commandArgs.length <= 0)
					continue;
				
				for (ComputerCommands command : ComputerCommands.values()) {
					if (command.getCommandText().equalsIgnoreCase(commandArgs[0])) {
						if ((commandArgs.length - 1) >= command.getNumberOfArgs()) {
							command.getCommand().onCommand(this, commandArgs.length - 1, Arrays.copyOfRange(commandArgs, 1, commandArgs.length));
						} else {
							terminal.writeLine(command.getCommandText() + " - " + command.getHelpText());
						}
					}
				}
			}
			
			terminal.write("Shutting down");
			for (int i = 0; i < 20; i++) {
				terminal.write(".");
				Thread.sleep(50);
			}
			terminal.writeLine("Goodbye!");
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
