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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
	
	@ExposedToLua(value = false)
	public void loadState() {
		luaState.newTable();
		luaState.pushNil(); // key unpersistMap
		while (luaState.next(LuaState.GLOBALSINDEX)) { // value key unpersistMap
			if (luaState.isCFunction(-1)) {
				luaState.pushValue(-2);
				luaState.setTable(-4);
			} else if (luaState.isJavaFunction(-1)) {
				luaState.pushValue(-2);
				luaState.setTable(-4);
			} else if (luaState.isTable(-1)) {
				luaState.pushNil(); // key value key unpersistMap
				while (luaState.next(-2)) { // value key value key unpersistMap
					if (luaState.isCFunction(-1)) {
						luaState.pushValue(-2);
						luaState.setTable(-6);
					} else if (luaState.isJavaFunction(-1)) {
						luaState.pushValue(-2);
						luaState.setTable(-6);
					} else if (luaState.isTable(-1)) {
						luaState.pushValue(-2);
						luaState.setTable(-6);
					} else if (!luaState.isNoneOrNil(-1)) {
						luaState.pop(1); // key value key unpersistMap
					}
				}
				luaState.pop(1);
			} else if (!luaState.isNoneOrNil(-1)) {
				luaState.pop(1); // key unpersistMap
			}
		}
		// unpersistMap
		try {
			File persistFile = new File(baseDirectory.getAbsolutePath() + File.separator + ".persist");
			if (!persistFile.exists())
				return;
			FileInputStream fis = new FileInputStream(persistFile);
			luaState.unpersist(fis);
			luaState.pushNil();
			while (luaState.next(-2)) {
				luaState.pushValue(-2);
				luaState.pushValue(-2);
				luaState.setTable(LuaState.GLOBALSINDEX);
				luaState.pop(1);
			}
			luaState.pop(2);
		} catch (FileNotFoundException e) {
			ElectroCraft.instance.getLogger().severe("Unable to open the persist file for loading!");
		}
	}
	
	@ExposedToLua(value = false)
	public void saveCurrentState() {
		Map<String, Integer> persistIgnoreMap = new HashMap<String, Integer>();
		luaState.newTable(); // persist
		luaState.pushNil(); // key persist
		while (luaState.next(LuaState.GLOBALSINDEX)) { // value key persist
			if (luaState.isCFunction(-1)) {
				persistIgnoreMap.put(luaState.toString(-2), persistIgnoreMap.size());
			} else if (luaState.isJavaFunction(-1)) {
				persistIgnoreMap.put(luaState.toString(-2), persistIgnoreMap.size());
			} else if (luaState.isTable(-1)) {
				luaState.pushNil(); // key value key persist
				while (luaState.next(-2)) { // value key value key persist
					if (luaState.isCFunction(-1)) {
						persistIgnoreMap.put(luaState.toString(-2), persistIgnoreMap.size());
					} else if (luaState.isJavaFunction(-1)) {
						persistIgnoreMap.put(luaState.toString(-2), persistIgnoreMap.size());
					} else if (luaState.isTable(-1)) {
						persistIgnoreMap.put(luaState.toString(-2), persistIgnoreMap.size());
					} else {
						luaState.pushValue(-2); // keyCopy value key value key persist
						luaState.pushValue(-2); // valueCopy keyCopy value key value key persist
					    luaState.setTable(-7); // value key value key persist
					}
					luaState.pop(1); // key value key persist
				}
			} else {
				luaState.pushValue(-2); // keyCopy value key persist
				luaState.pushValue(-2); // valueCopy keyCopy value key persist
				luaState.setTable(-5); // value key persist
			}
			luaState.pop(1); // key persist
		}
		try {
			File persistFile = new File(baseDirectory.getAbsolutePath() + File.separator + ".persist");
			if (!persistFile.exists())
				persistFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(persistFile);
			luaState.newTable(); // permanents persist  
		    for (String s : persistIgnoreMap.keySet()) {
		    	luaState.getGlobal(s);
		    	if (luaState.isNoneOrNil(-1)) {
		    		luaState.pop(1);
		    		continue;
		    	}
		    	luaState.pushNumber(persistIgnoreMap.get(s));
		    	if (luaState.isNoneOrNil(-1)) {
		    		luaState.pop(1);
		    		continue;
		    	}
		    	luaState.setTable(-3);
		    }
		    luaState.pushValue(-2); // persist permanents persist  
		    luaState.remove(-3); // persist permanents
		    luaState.persist(fos);
			luaState.pop(2);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			ElectroCraft.instance.getLogger().severe("Unable to open the persist file for saving!");
		} catch (IOException e) {
			ElectroCraft.instance.getLogger().severe("Unable to save a comptuers state!");
		}
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
		
		NamedJavaFunction getTerminal = new NamedJavaFunction() {
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
		}.init(this);
		
		luaState.register(getTerminal);
		
		NamedJavaFunction sleep = new NamedJavaFunction() {
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
		};
		
		luaState.register(sleep);
		
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
		
		loadState();
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
