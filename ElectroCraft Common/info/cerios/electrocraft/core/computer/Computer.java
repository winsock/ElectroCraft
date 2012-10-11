package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.commands.ComputerCommands;
import info.cerios.electrocraft.core.computer.luaapi.ComputerFile;
import info.cerios.electrocraft.core.computer.luaapi.ComputerSocket;
import info.cerios.electrocraft.core.computer.luaapi.LuaAPI;
import info.cerios.electrocraft.core.computer.luaapi.MinecraftIOInterface;
import info.cerios.electrocraft.core.network.ComputerServerClient;
import com.naef.jnlua.DefaultJavaReflector;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaSyntaxException;
import com.naef.jnlua.LuaType;
import com.naef.jnlua.LuaValueProxy;
import com.naef.jnlua.NamedJavaFunction;
import com.naef.jnlua.JavaReflector.Metamethod;
import com.naef.jnlua.LuaState.Library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
	private MinecraftIOInterface mcIO;
	private boolean graphicsMode = false;
	private ComputerServerClient client;
	private File baseDirectory;
	/**
	 * The current directory of the computer relative to the baseDirectory
	 */
	private String currentDirectory = "";
	private String currentProgram = null;
	private int openFileHandles = 0;
	private LuaState luaState;
	private Set<LuaAPI> apis = new HashSet<LuaAPI>();
	public static final Object luaStateLock = new Object();
	private List<String> previousCommands = new ArrayList<String>();
	private volatile boolean wasResumed = false;

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
	public void setOpenFileHandles(int value) {
		openFileHandles = value;
	}

	@ExposedToLua(value = false)
	public LuaState getLuaState() {
		return luaState;
	}

	@ExposedToLua(value = false)
	public void setRunningProgram(String program) {
		currentProgram = program;
	}

	@ExposedToLua(value = false)
	public String getRunningProgram() {
		return currentProgram;
	}

	@ExposedToLua
	public void dumpStack() {
		int top = luaState.getTop();
		for (int i = 1; i <= top; i++) {
			LuaType type = luaState.type(i);
			switch (type) {
			case STRING:  /* strings */
				System.out.print(luaState.toString(i) + " | ");
				break;

			case BOOLEAN:  /* booleans */
				System.out.print((luaState.toBoolean(i) ? "true" : "false") + " | ");
				break;

			case NUMBER:  /* numbers */
				System.out.print(luaState.toNumber(i) + " | ");
				break;

			default:  /* other values */
				System.out.print(luaState.typeName(i) + " | ");
				break;
			}
		}
		System.out.println();
	}

	/**
	 * Expects the table to parse to be at the top of the stack
	 * Expects a table to store the permanents at position 2
	 */
	@ExposedToLua(value = false)
	public synchronized void createPermanentsTable(String tableName, boolean flip, int recurseLevel, List<Long> tablePointers) {
		luaState.pushNil(); // key table permanents thread
		while (luaState.next(-2)) { // value key table permanents thread
			if (luaState.isCFunction(-1)) {
				if (flip) {
					luaState.pushValue(-1);
					luaState.pushValue(-3);
				} else {
					luaState.pushValue(-2);
					luaState.pushValue(-2);
				}
				luaState.setTable(-6 - (recurseLevel * 2));
			} else if (luaState.isJavaFunction(-1)) {
				if (flip) {
					luaState.pushValue(-1);
					luaState.pushValue(-3);
				} else {
					luaState.pushValue(-2);
					luaState.pushValue(-2);
				}
				luaState.setTable(-6 - (recurseLevel * 2));
			} else if (luaState.isTable(-1)) {
				if (!tablePointers.contains(luaState.toPointer(-1))) {
					tablePointers.add(luaState.toPointer(-1));
					createPermanentsTable(tableName + "." + luaState.toString(-2), flip, recurseLevel + 1, tablePointers);
				} else {
					if (flip) {
						luaState.pushValue(-1);
						luaState.pushValue(-3);
					} else {
						luaState.pushValue(-2);
						luaState.pushValue(-2);
					}
					luaState.setTable(-6 - (recurseLevel * 2));
				}
			} else if (luaState.isThread(-1)) {
				luaState.getFEnv(-1);
				if (!tablePointers.contains(luaState.toPointer(-1))) {
					tablePointers.add(luaState.toPointer(-1));
					createPermanentsTable(tableName + "." + luaState.toString(-3), flip, recurseLevel + 1, tablePointers);
				}
				luaState.pop(1);
			}
			luaState.pop(1); // key table permanents thread
		}

	}

	@ExposedToLua(value = false)
	public void loadState() {
		// Register the Lua thread with the security manager
		ElectroCraft.instance.getSecurityManager().registerThread(this);
		wasResumed = true;
		synchronized(luaStateLock) {
			luaState.newTable();
			luaState.getGlobal("_G");
			createPermanentsTable("_G", false, 0, new ArrayList<Long>());
			luaState.pop(1);
			// unpersistMap
			try {
				File persistFile = new File(baseDirectory.getAbsolutePath() + File.separator + ".persist");
				if (!persistFile.exists()) {
					luaState.pop(1);
					return;
				}
				FileInputStream fis = new FileInputStream(persistFile);
				luaState.unpersist(fis);
				fis.close();
				persistFile.delete();

				luaState.resume(-1, 0);
				luaState.reset_kill();
				while (running && luaState.isOpen()) {
					if (luaState.status(-1) == LuaState.YIELD) {
						luaState.resume(-1, 0);
						luaState.reset_kill();
					}
					else
						break;
				}
				luaState.pop(2);
			} catch (LuaSyntaxException e) {
				getTerminal().print("Error running lua script: Syntax Error!");
				getTerminal().print(e.getLocalizedMessage());
			} catch (LuaRuntimeException e) {
				getTerminal().print("Error running lua script: Runtime Error!");
				getTerminal().print(e.getLocalizedMessage());
			} catch (FileNotFoundException e) {
				ElectroCraft.instance.getLogger().severe("Unable to open the persist file for loading!");
			} catch (IOException e1) {
				// Unable to close input stream, oh well
			}
			// Make sure that the kill switch is reset
			luaState.reset_kill();
		}
	}

	@ExposedToLua(value = false)
	public void saveCurrentState() {
		synchronized(luaStateLock) {
			dumpStack();
			luaState.getField(LuaState.REGISTRYINDEX, "electrocraft_program_coroutine");
			if (getLuaState().isNoneOrNil(-1)) {
				luaState.pop(1);
				return;
			}
			luaState.newTable();
			luaState.getGlobal("_G");
			createPermanentsTable("_G", true, 0, new ArrayList<Long>());
			luaState.pop(1);
			// permanents thread
			try {
				luaState.pushValue(-2);
				luaState.remove(-3);
				luaState.pushValue(-1);

				File persistFile = new File(baseDirectory.getAbsolutePath() + File.separator + ".persist");
				if (!persistFile.exists())
					persistFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(persistFile);
				luaState.persist(fos);
				luaState.pop(3);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				ElectroCraft.instance.getLogger().severe("Unable to open the persist file for saving!");
			} catch (IOException e) {
				ElectroCraft.instance.getLogger().severe("Unable to save a comptuers state!");
			}
		}
	}

	@ExposedToLua(value = false)
	public void loadAPI(LuaAPI api) {
		apis.add(api);
		for (NamedJavaFunction f : api.getGlobalFunctions(this))
			getLuaState().register(f);
	}

	@ExposedToLua(value = false)
	public void loadLuaDefaults() {
		// Create a new state
		luaState = new LuaState();
		// Load the allowed libraries
		getLuaState().openLib(Library.BASE);
		getLuaState().openLib(Library.DEBUG);
		getLuaState().openLib(Library.JAVA);
		getLuaState().openLib(Library.MATH);
		getLuaState().openLib(Library.PACKAGE);
		getLuaState().openLib(Library.PLUTO);
		getLuaState().openLib(Library.STRING);
		getLuaState().openLib(Library.TABLE);
		
		getLuaState().install_kill_hook();

		// Load base ElectroCraft functions
		NamedJavaFunction getTerminal = new NamedJavaFunction() {
			Computer computer;

			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}

			@Override
			public int invoke(LuaState luaState) {
				getLuaState().pushJavaObject(computer.getTerminal());
				return 1;
			}

			@Override
			public String getName() {
				return "getTerminal";
			}
		}.init(this);

		getLuaState().register(getTerminal);

		NamedJavaFunction sleep = new NamedJavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				try {
					Thread.sleep((long) getLuaState().checkNumber(0));
				} catch (InterruptedException e) { }
				return 0;
			}

			@Override
			public String getName() {
				return "sleep";
			}
		};

		getLuaState().register(sleep);

		getLuaState().register(new NamedJavaFunction() {
			Computer computer;

			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}

			@Override
			public int invoke(LuaState luaState) {
				getLuaState().pushJavaObject(computer);
				return 1;
			}

			@Override
			public String getName() {
				return "getComputer";
			}
		}.init(this));

		getLuaState().register(new NamedJavaFunction() {
			Computer computer;

			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}

			@Override
			public int invoke(LuaState luaState) {
				getLuaState().pushJavaObject(computer.getKeyboard());
				return 1;
			}

			@Override
			public String getName() {
				return "getKeyboard";
			}
		}.init(this));

		getLuaState().register(new NamedJavaFunction() {
			Computer computer;

			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}

			@Override
			public int invoke(LuaState luaState) {
				getLuaState().pushJavaObject(computer.getVideoCard());
				return 1;
			}

			@Override
			public String getName() {
				return "getVideoCard";
			}
		}.init(this));

		// Load ElectroCraft default libraries
		loadAPI(new ComputerFile());
		loadAPI(new ComputerSocket());
		loadAPI(mcIO = new MinecraftIOInterface());
	}

	@ExposedToLua
	public int getNumberOfOpenFileHandles() {
		return this.openFileHandles;
	}

	@ExposedToLua
	public int getMaxFileHandles() {
		return ConfigHandler.getCurrentConfig().get("computer", "maxFileHandlesPerUser", 20).getInt(20);
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
	public void setCurrentDirectory(String directory) {
		this.currentDirectory = directory;
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
		if (!luaState.isOpen())
			loadLuaDefaults();
		// Register the main Lua thread with the security manager
		ElectroCraft.instance.getSecurityManager().registerThread(this);
		try {
			if (!wasResumed) {
				terminal.write("Booting Cerios");
				for (int i = 0; i < 20; i++) {
					terminal.write(".");
					Thread.sleep(50);
				}
				terminal.clear();
				terminal.writeLine("Welcome to Cerios!");
				terminal.writeLine("Cerios - A Minecraft OS written by Andrew Querol");
				terminal.writeLine("To get started type help!");
			}
			while (running) {
				// Write the terminal prompt
				if (!wasResumed || currentProgram != null) {
					terminal.write(currentDirectory + "> ");
				} else {
					wasResumed = false;
					currentProgram = null;
				}

				String line = "";
				int chr = '\0';
				int upTimes = 0;
				while (true) {
					if (chr == '\n' || chr == '\r') {
						break;
					} else if (chr == Keyboard.upScanCode) {
						if (upTimes < previousCommands.size()) {
							line = previousCommands.get((previousCommands.size() - 1) - upTimes);
							terminal.clearLine();
							terminal.write(currentDirectory + "> " + line);
							upTimes++;
						}
					} else if (chr == Keyboard.downScanCode) {
						if (upTimes > 0) {
							upTimes--;
							line = previousCommands.get((previousCommands.size() - 1) - upTimes);
							terminal.clearLine();
							terminal.write(currentDirectory + "> " + line);
						}
					} else if (chr == '\b') {
						if (line.length() > 0) {
							line = line.substring(0, line.length() - 1);
							terminal.deleteChar(false);
						}
					} else if (chr != '\0') {
						if (Character.isDefined(chr))
							line += (char)chr; // Add the old char
					}
					chr = keyboard.waitForKey(); // Pop the new one
				}

				previousCommands.add(line);

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
			luaState.close();
		} catch (Exception e) {
			luaState.close();
			e.printStackTrace();
		}
	}
	
	@ExposedToLua(value = false)
	public List<String> getPreviousCommands() {
		return previousCommands;
	}
	
	@ExposedToLua(value = false)
	public void setPreviousCommands(List<String> commands) {
		this.previousCommands = commands;
	}
	
	@ExposedToLua(value = false)
	public void registerNetworkBlock(NetworkBlock block) {
		if (mcIO != null)
			mcIO.register(block);
	}
}
