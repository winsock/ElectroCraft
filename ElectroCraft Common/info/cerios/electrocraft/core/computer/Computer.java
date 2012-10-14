package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.luaapi.ComputerFile;
import info.cerios.electrocraft.core.computer.luaapi.ComputerSocket;
import info.cerios.electrocraft.core.computer.luaapi.LuaAPI;
import info.cerios.electrocraft.core.computer.luaapi.MinecraftInterface;
import info.cerios.electrocraft.core.network.ComputerServerClient;
import info.cerios.electrocraft.core.network.CustomPacket;

import com.naef.jnlua.DefaultJavaReflector;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaError;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaStackTraceElement;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaSyntaxException;
import com.naef.jnlua.LuaType;
import com.naef.jnlua.LuaValueProxy;
import com.naef.jnlua.NamedJavaFunction;
import com.naef.jnlua.JavaReflector.Metamethod;
import com.naef.jnlua.LuaState.GcAction;
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

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NBTTagCompound;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

@ExposedToLua
public class Computer {

	private boolean isInternal = true;
	private volatile boolean running = false;
	private final String bootScript;
	private SoundCard soundCard;
	private Terminal terminal;
	private VideoCard videoCard;
	private Keyboard keyboard;
	private MinecraftInterface mcIO;
	private boolean graphicsMode = false;
	private List<EntityPlayer> clients;
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
	private NBTTagCompound programStorage;
	private List<String> onSaveMethods = new ArrayList<String>();
	private Object sleepLock = new Object();
	private volatile boolean finishedSleeping = true;
	private boolean killYielded = false;
	private Timer sleepTimer = new Timer();

	@ExposedToLua(value = false)
	public Computer(List<EntityPlayer> clients, String script, String baseDirectory, boolean isInternal, int width, int height, int rows, int columns) {
		this.isInternal = isInternal;
		this.soundCard = new SoundCard();
		this.videoCard = new VideoCard(width, height);
		this.terminal = new Terminal(rows, columns);
		this.keyboard = new Keyboard(terminal);
		this.clients = clients;
		this.bootScript = script;
		this.baseDirectory = new File(baseDirectory);
		if (!this.baseDirectory.exists()) {
			this.baseDirectory.mkdirs();
		}
		// Lua Stuff
		loadLuaDefaults();
	}

	@ExposedToLua(value = false)
	public List<EntityPlayer> getClients() {
		return clients;
	}

	@ExposedToLua(value = false)
	public void removeClient(EntityPlayer client) {
		clients.remove(client);
	}

	public void addClient(EntityPlayer client) {
		clients.add(client);
		if (!ConfigHandler.getCurrentConfig().get("general", "useMCServer", false).getBoolean(false))
			ElectroCraft.instance.getServer().getClient((EntityPlayerMP) client).changeModes(!graphicsMode);
		else {
			CustomPacket packet = new CustomPacket();
			packet.id = 0;
			packet.data = new byte[] { (byte) (!graphicsMode ? 1 : 0) };
			try {
				PacketDispatcher.sendPacketToPlayer(packet.getMCPacket(), (Player) client);
			} catch (IOException e) {
			}
		}
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

	/*
	 * Old code pertaining to persistence
	 */
	//	/**
	//	 * Expects the table to parse to be at the top of the stack
	//	 * Expects a table to store the permanents at position 2
	//	 */
	//	@ExposedToLua(value = false)
	//	public synchronized void createPermanentsTable(String tableName, boolean flip, int recurseLevel, List<Long> tablePointers) {
	//		luaState.pushNil(); // key table permanents thread
	//		while (luaState.next(-2)) { // value key table permanents thread
	//			if (luaState.isCFunction(-1)) {
	//				if (flip) {
	//					luaState.pushValue(-1);
	//					luaState.pushValue(-3);
	//				} else {
	//					luaState.pushValue(-2);
	//					luaState.pushValue(-2);
	//				}
	//				luaState.setTable(-6 - (recurseLevel * 2));
	//			} else if (luaState.isJavaFunction(-1)) {
	//				if (flip) {
	//					luaState.pushValue(-1);
	//					luaState.pushValue(-3);
	//				} else {
	//					luaState.pushValue(-2);
	//					luaState.pushValue(-2);
	//				}
	//				luaState.setTable(-6 - (recurseLevel * 2));
	//			} else if (luaState.type(-1) == LuaType.LIGHTUSERDATA || luaState.type(-1) == LuaType.USERDATA) {
	//				if (flip) {
	//					luaState.pushValue(-1);
	//					luaState.pushValue(-3);
	//				} else {
	//					luaState.pushValue(-2);
	//					luaState.pushValue(-2);
	//				}
	//				luaState.setTable(-6 - (recurseLevel * 2));
	//			} else if (luaState.isTable(-1)) {
	//				if (!tablePointers.contains(luaState.toPointer(-1))) {
	//					tablePointers.add(luaState.toPointer(-1));
	//					createPermanentsTable(tableName + "." + luaState.toString(-2), flip, recurseLevel + 1, tablePointers);
	//				} else {
	//					if (flip) {
	//						luaState.pushValue(-1);
	//						luaState.pushValue(-3);
	//					} else {
	//						luaState.pushValue(-2);
	//						luaState.pushValue(-2);
	//					}
	//					luaState.setTable(-6 - (recurseLevel * 2));
	//				}
	//			} else if (luaState.isThread(-1)) {
	//				if (flip) {
	//					luaState.pushValue(-1);
	//					luaState.pushValue(-3);
	//				} else {
	//					luaState.pushValue(-2);
	//					luaState.pushValue(-2);
	//				}
	//				luaState.setTable(-6 - (recurseLevel * 2));
	//			}
	//			luaState.pop(1); // key table permanents thread
	//		}
	//
	//	}
	//
	//	@ExposedToLua(value = false)
	//	public void loadState() {
	//		// Register the Lua thread with the security manager
	//		ElectroCraft.instance.getSecurityManager().registerThread(this);
	//		wasResumed = true;
	//		synchronized(luaStateLock) {
	//			luaState.newTable();
	//			luaState.getGlobal("_G");
	//			createPermanentsTable("_G", false, 0, new ArrayList<Long>());
	//			luaState.pop(1);
	//			// unpersistMap
	//			try {
	//				File persistFile = new File(baseDirectory.getAbsolutePath() + File.separator + ".persist");
	//				if (!persistFile.exists()) {
	//					luaState.pop(1);
	//					return;
	//				}
	//				FileInputStream fis = new FileInputStream(persistFile);
	//				luaState.unpersist(fis);
	//				fis.close();
	//				persistFile.delete();
	//
	//				luaState.resume(-1, 0);
	//				luaState.reset_kill();
	//				while (running && luaState.isOpen()) {
	//					if (luaState.status(-1) == LuaState.YIELD) {
	//						luaState.resume(-1, 0);
	//						luaState.reset_kill();
	//					}
	//					else
	//						break;
	//				}
	//				luaState.pop(2);
	//			} catch (LuaSyntaxException e) {
	//				getTerminal().print("Error running lua script: Syntax Error!");
	//				getTerminal().print(e.getLocalizedMessage());
	//			} catch (LuaRuntimeException e) {
	//				getTerminal().print("Error running lua script: Runtime Error!");
	//				getTerminal().print(e.getLocalizedMessage());
	//			} catch (FileNotFoundException e) {
	//				ElectroCraft.instance.getLogger().severe("Unable to open the persist file for loading!");
	//			} catch (IOException e1) {
	//				// Unable to close input stream, oh well
	//			}
	//			// Make sure that the kill switch is reset
	//			luaState.reset_kill();
	//			// Make sure that we are in terminal mode after running the program
	//			setGraphicsMode(false);
	//		}
	//	}
	//
	//	@ExposedToLua(value = false)
	//	public void saveCurrentState() {
	//		synchronized(luaStateLock) {
	//			luaState.getField(LuaState.REGISTRYINDEX, "electrocraft_program_coroutine");
	//			if (luaState.isNoneOrNil(-1) || getRunningProgram() == null || !luaState.isOpen()) {
	//				luaState.pop(1);
	//				return;
	//			}
	//			luaState.newTable();
	//			luaState.getGlobal("_G");
	//			createPermanentsTable("_G", true, 0, new ArrayList<Long>());
	//			luaState.pop(1);
	//			// permanents thread
	//			try {
	//				luaState.pushValue(-2);
	//				luaState.remove(-3);
	//				luaState.pushValue(-1);
	//
	//				File persistFile = new File(baseDirectory.getAbsolutePath() + File.separator + ".persist");
	//				if (!persistFile.exists())
	//					persistFile.createNewFile();
	//				FileOutputStream fos = new FileOutputStream(persistFile);
	//				luaState.persist(fos);
	//				luaState.pop(3);
	//				fos.flush();
	//				fos.close();
	//			} catch (FileNotFoundException e) {
	//				ElectroCraft.instance.getLogger().severe("Unable to open the persist file for saving!");
	//			} catch (IOException e) {
	//				ElectroCraft.instance.getLogger().severe("Unable to save a comptuers state!");
	//			}
	//		}
	//	}

	@ExposedToLua(value = false)
	public void tick() {
		if (killYielded)
			postEvent("resume");
		
		if (getKeyboard().getKeysInBuffer() > 0) {
			if (getKeyboard().peak() > Character.MAX_VALUE)
				postEvent("code", getKeyboard().popKey());
			else
				postEvent("key", getKeyboard().popChar());
		}

		for (LuaAPI api : apis) {
			api.tick(this);
		}
	}

	@ExposedToLua(value = false)
	public void postEvent(String eventName, Object... args) {
		if (!finishedSleeping)
			return;
		if (killYielded)
			killYielded = false;
		if (!running)
			return;
		try {
			synchronized(luaStateLock) {
				if (!luaState.isOpen())
					return;
				luaState.getField(LuaState.GLOBALSINDEX, "coroutine");
				luaState.getField(-1, "resume");
				luaState.remove(-2);
				luaState.getField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
				if (luaState.type(-1) != LuaType.THREAD || (luaState.status(-1) != LuaState.YIELD && luaState.status(-1) != 0)) {
					luaState.pop(luaState.getTop());
					return;
				}
				luaState.pushString(eventName);
				for (Object arg : args) {
					luaState.pushJavaObject(arg);
				}
				luaState.call(2 + args.length, LuaState.MULTRET);
				luaState.reset_kill(100);

				if (luaState.isBoolean(1))
					if (!luaState.checkBoolean(1, true))
						throw new LuaRuntimeException("Runtime error!");
				if (luaState.getTop() == 2 && luaState.isNumber(-1)) {
					finishedSleeping = false;
					sleepTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							synchronized(sleepLock) {
								finishedSleeping = true;
								killYielded = true;
							}
						}}, luaState.checkInteger(-1, 0));
				} else if (luaState.getTop() == 1) {
					killYielded = true;
				}
			}
		} catch (LuaSyntaxException e) {
			getTerminal().print("Syntax Error!");
			e.printStackTrace(new PrintWriter(getTerminal()));
		} catch (LuaRuntimeException e) {
			getTerminal().print("Runtime Error!");
			dumpStack();
			if (luaState.isString(-1))
				getTerminal().print(luaState.checkString(-1));
			else {
				e.printLuaStackTrace(new PrintWriter(getTerminal()));
				e.printLuaStackTrace();
			}
		}
		luaState.pop(luaState.getTop());
	}

	@ExposedToLua(value = false)
	public void loadBios() {
		synchronized(luaStateLock) {
			try {
				luaState.load(Computer.class.getResourceAsStream("/info/cerios/electrocraft/rom/bios.lua"), "bios_" + baseDirectory.getName());
				luaState.newThread();
				luaState.setField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
			} catch (IOException e) {
				getTerminal().print("Unable to load the BIOS check that you have installed ElectroCraft correctly");
			}
		}
	}

	@ExposedToLua(value = false)
	public void callSave() {
		if (running && currentProgram != null) {
			programStorage = new NBTTagCompound(currentProgram);
			for (String m : onSaveMethods) {
				synchronized(luaStateLock) {
					try {
						luaState.getGlobal(m);
						if (luaState.type(-1) != LuaType.FUNCTION) {
							onSaveMethods.remove(m);
							getTerminal().print("Invalid save method: " + m);
							continue;
						}
						luaState.pushJavaObject(programStorage);
						luaState.call(1, 0);
					} catch (LuaSyntaxException e) {
						System.out.println("Error running lua script: Syntax Error!");
						System.out.println(e.getLocalizedMessage());
					} catch (LuaRuntimeException e) {
						System.out.println("Error running lua script: Runtime Error!");
						System.out.println("Possibly went to long without yielding?");
						e.printLuaStackTrace();
					}
				}
			}
		}
	}

	@ExposedToLua(value = false)
	public void callLoad() {
		if (running) {
			wasResumed = true;
			loadBios();
			postEvent("resume", programStorage);
		}
	}

	@ExposedToLua(value = false)
	public void loadAPI(LuaAPI api) {
		apis.add(api);
		synchronized(luaStateLock) {
			getLuaState().register(api.getNamespace(), api.getGlobalFunctions(this));
			luaState.pop(1);
		}
	}

	@ExposedToLua(value = false)
	public void loadLuaDefaults() {
		// Create a new state
		if (luaState != null && luaState.isOpen())
			luaState.close();
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
		
		// Reseting it, if not enabled will enable it
		luaState.install_kill_hook(100);
		
		// Load base ElectroCraft functions
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

		getLuaState().register(getTerminal);

		NamedJavaFunction sleep = new NamedJavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				return luaState.yield(1);
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
				luaState.pushJavaObject(computer);
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
				luaState.pushJavaObject(computer.getKeyboard());
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
				luaState.pushJavaObject(computer.getVideoCard());
				return 1;
			}

			@Override
			public String getName() {
				return "getVideoCard";
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
				onSaveMethods.add(luaState.checkString(-1));
				return 0;
			}

			@Override
			public String getName() {
				return "saveCallback";
			}
		}.init(this));

		// Load ElectroCraft default libraries
		loadAPI(new ComputerFile());
		loadAPI(new ComputerSocket());
		loadAPI(mcIO = new MinecraftInterface());

		// Install additional checks
		new Thread(new Runnable() {
			Computer computer;

			public Runnable init(Computer computer) {
				this.computer = computer;
				return this;
			}

			@Override
			public void run() {
				while (computer.isRunning()) {
					synchronized(Computer.luaStateLock) {
						if (!computer.getLuaState().isOpen())
							break;
						// System memory check
						if (computer.getLuaState().gc(GcAction.COUNT, 0) > ConfigHandler.getCurrentConfig().get("computer", "MaxMemPerUser", 16).getInt(16) * 1024) {
							computer.setRunning(false);
							computer.getTerminal().print("ERROR: Ran out of memory! Max memory is: " + String.valueOf(ConfigHandler.getCurrentConfig().get("computer", "MaxMemPerUser", 16).getInt(16)) + "M");
						}

						// Extra backup check in case my wrapped file manager doesn't catch it
						if (computer.getBaseDirectory().length() > ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10) * 1024 * 1024) {
							computer.setRunning(false);
							computer.getTerminal().print("ERROR: Ran out of storage! Max storage space is: " + String.valueOf(ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10)) + "M");
						}
					}

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) { }
				}
			}
		}.init(this)).start();
	}

	@ExposedToLua(value = false)
	public void addSaveMethod(String method) {
		onSaveMethods.add(method);
	}

	@ExposedToLua(value = false)
	public List<String> getSaveMethods() {
		return onSaveMethods;
	}

	@ExposedToLua(value = false)
	public NBTTagCompound getProgramStorage() {
		return programStorage;
	}

	@ExposedToLua(value = false)
	public void setProgramStorage(NBTTagCompound storage) {
		this.programStorage = storage;
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
			for (EntityPlayer p : clients) {
				if (!ConfigHandler.getCurrentConfig().get("general", "useMCServer", false).getBoolean(false))
					ElectroCraft.instance.getServer().getClient((EntityPlayerMP) p).changeModes(!graphicsMode);
				else {
					CustomPacket packet = new CustomPacket();
					packet.id = 0;
					packet.data = new byte[] { (byte) (!graphicsMode ? 1 : 0) };
					try {
						PacketDispatcher.sendPacketToPlayer(packet.getMCPacket(), (Player) p);
					} catch (IOException e) {
					}
				}
			}
		this.graphicsMode = graphicsMode;
	}

	@ExposedToLua
	public boolean isInGraphicsMode() {
		return this.graphicsMode;
	}

	@ExposedToLua
	public void shutdown() {
		running = false;
	}
	
	@ExposedToLua(value = false)
	public void setRunning(boolean value) {
		running = value;
		if (value == false && luaState != null && luaState.isOpen())
			luaState.close();
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

	@ExposedToLua(value = false)
	public void removeNetworkBlock(NetworkBlock block) {
		if (mcIO != null)
			mcIO.remove(block);
	}
}
