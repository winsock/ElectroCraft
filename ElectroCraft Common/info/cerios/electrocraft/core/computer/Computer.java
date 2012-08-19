package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.commands.ComputerCommands;
import info.cerios.electrocraft.core.network.ComputerServerClient;

import java.io.File;
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
	private boolean graphicsMode = false;
	private ComputerServerClient client;
	private File baseDirectory;
	/**
	 * The current directory of the computer realitve to the baseDirectory
	 */
	private String currentDirectory = ".";
	
	@ExposedToLua(value = false)
	public Computer(ComputerServerClient client, String script, String baseDirectory, boolean isInternal, int width, int height, int rows, int columns) {
		this.isInternal = isInternal;
		this.soundCard = new SoundCard();
		this.videoCard = new VideoCard(width, height);
		this.terminal = new Terminal(rows, columns);
		this.keyboard = new Keyboard(terminal);
		this.client = client;
		this.bootScript = script;
		this.baseDirectory = new File(baseDirectory);
	}
	
	@ExposedToLua(value = false)
	public ComputerServerClient getClient() {
		return client;
	}
	
	public void setGraphicsMode(boolean graphicsMode) {
		if (this.graphicsMode != graphicsMode)
			client.changeModes(!graphicsMode);
		this.graphicsMode = graphicsMode;
	}
	
	public void setRunning(boolean value) {
		running = value;
	}
	
	public File getBaseDirectory() {
		return baseDirectory;
	}
	
	public String getCurrentDirectory() {
		return currentDirectory;
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
	
	@ExposedToLua(value = false)
	@Override
	public void run() {
		// Register the main Lua thread with the security manager
		ElectroCraft.instance.getSecurityManager().registerThread(this);
		
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
