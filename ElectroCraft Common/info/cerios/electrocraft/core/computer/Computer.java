package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.commands.ComputerCommands;
import info.cerios.electrocraft.core.computer.luajavaapi.ComputerFile;
import info.cerios.electrocraft.core.computer.luajavaapi.ComputerSocket;
import info.cerios.electrocraft.core.network.ComputerServerClient;
import com.naef.jnlua.DefaultJavaReflector;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;
import com.naef.jnlua.JavaReflector.Metamethod;
import com.naef.jnlua.LuaState.Library;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import cpw.mods.fml.common.FMLCommonHandler;

@ExposedToLua
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
	private String currentDirectory = "";
	private int openFileHandles = 0;
	private LuaState luaState;
	
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
		if (!this.baseDirectory.exists()) {
			this.baseDirectory.mkdirs();
		}
		// Lua Stuff
		luaState = new LuaState();
		loadLuaDefaults();
	}
	
	@ExposedToLua(value = false)
	public ComputerServerClient getClient() {
		return client;
	}
	
	@ExposedToLua(value = false)
	public void incrementOpenFileHandles() {
		this.openFileHandles++;
	}
	
	@ExposedToLua(value = false)
	public void deincrementOpenFileHandles() {
		this.openFileHandles--;
	}
	
	@ExposedToLua(value = false)
	public LuaState getLuaState() {
		return luaState;
	}
	
	@ExposedToLua(value = false)
	public void serverShutdown() {
	}
	
	@ExposedToLua(value = false)
	private void loadLuaDefaults() {
		// Load the allowed libraries
		luaState.openLib(Library.BASE);
		luaState.openLib(Library.DEBUG);
		luaState.openLib(Library.JAVA);
		luaState.openLib(Library.MATH);
		luaState.openLib(Library.PACKAGE);
		luaState.openLib(Library.PLUTO);
		luaState.openLib(Library.STRING);
		luaState.openLib(Library.TABLE);

		luaState.register(new NamedJavaFunction() {
			Computer computer;
			
			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public int invoke(LuaState luaState) {
				luaState.pushJavaObject(computer.getTerminal());
				return 1;
			}

			@Override
			public String getName() {
				return "getTerminal";
			}
		}.init(this));
		
		luaState.register(new NamedJavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				try {
					Thread.sleep((long) luaState.checkNumber(0));
				} catch (InterruptedException e) { }
				return 0;
			}

			@Override
			public String getName() {
				return "sleep";
			}
		});
		
		luaState.register(new NamedJavaFunction() {
			Computer computer;
			
			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public int invoke(LuaState luaState) {
				luaState.pushJavaObject(computer);
				return 1;
			}

			@Override
			public String getName() {
				return "getComputer";
			}
		}.init(this));
		
		luaState.register(new NamedJavaFunction() {
			Computer computer;
			
			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public int invoke(LuaState luaState) {
				luaState.pushJavaObject(computer.getKeyboard());
				return 1;
			}

			@Override
			public String getName() {
				return "getKeyboard";
			}
		}.init(this));
		
		luaState.register(new NamedJavaFunction() {
			Computer computer;
			
			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public int invoke(LuaState luaState) {
				luaState.pushJavaObject(computer.getVideoCard());
				return 1;
			}

			@Override
			public String getName() {
				return "getVideoCard";
			}
		}.init(this));
		
		luaState.register(new NamedJavaFunction() {
			Computer computer;
			
			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public int invoke(LuaState luaState) {
				luaState.pushJavaObject(new ComputerSocket());
				return 1;
			}

			@Override
			public String getName() {
				return "createNewSocket";
			}
		}.init(this));
		
		luaState.register(new NamedJavaFunction() {
			Computer computer;
			
			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public int invoke(LuaState luaState) {
				luaState.pushJavaObject(new ComputerFile(luaState.checkString(0), computer));
				return 1;
			}

			@Override
			public String getName() {
				return "createNewFileHandle";
			}
		}.init(this));
	}
	
	@ExposedToLua
	public int getNumberOfOpenFileHandles() {
		return this.openFileHandles;
	}
	
	@ExposedToLua
	public int getMaxFileHandles() {
		return ConfigHandler.getCurrentConfig().getOrCreateIntProperty("maxFileHandlesPerUser", "computer", 20).getInt(20);
	}
	
	@ExposedToLua
	public void setGraphicsMode(boolean graphicsMode) {
		if (this.graphicsMode != graphicsMode)
			client.changeModes(!graphicsMode);
		this.graphicsMode = graphicsMode;
	}
	
	@ExposedToLua
	public void setRunning(boolean value) {
		running = value;
	}
	
	@ExposedToLua(value = false)
	public File getBaseDirectory() {
		return baseDirectory;
	}
	
	@ExposedToLua
	public String getCurrentDirectory() {
		return currentDirectory;
	}
	
	@ExposedToLua
	public boolean isRunning() {
		return running;
	}
	
	@ExposedToLua(value = false)
	public SoundCard getSoundCard() {
		return soundCard;
	}
	
	@ExposedToLua(value = false)
	public Terminal getTerminal() {
		return terminal;
	}
	
	@ExposedToLua(value = false)
	public VideoCard getVideoCard() {
		return videoCard;
	}
	
	@ExposedToLua(value = false)
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
			terminal.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
