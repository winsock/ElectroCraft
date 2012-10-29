package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.api.computer.ExposedToLua;
import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.api.computer.luaapi.LuaAPI;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.luaapi.ComputerFile;
import info.cerios.electrocraft.core.computer.luaapi.EndNet;
import info.cerios.electrocraft.core.computer.luaapi.Network;
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
import java.io.PrintStream;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NBTTagCompound;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

@ExposedToLua
public class Computer implements Runnable {

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
	protected LuaState luaState;
	private Set<LuaAPI> apis = new HashSet<LuaAPI>();
	protected final ReentrantLock luaStateLock = new ReentrantLock();
	private List<String> previousCommands = new ArrayList<String>();
	private volatile boolean wasResumed = false;
	private NBTTagCompound programStorage;
	private List<String> onSaveMethods = new ArrayList<String>();
	private Object sleepLock = new Object();
	private volatile boolean finishedSleeping = true;
	private boolean killYielded = false;
	private Timer sleepTimer = new Timer();
	private List<Event> eventQueue = new ArrayList<Event>();
	private Object eventLock = new Object();
	private Object finalizeGuardian;
	private Thread thisThread;
	protected final ReentrantLock stateLock = new ReentrantLock();
	private volatile boolean ticked = false;

	@ExposedToLua(value = false)
	public Computer(List<EntityPlayer> clients, String script, String baseDirectory, boolean isInternal, int width, int height, int rows, int columns) {
		this.isInternal = isInternal;
		this.soundCard = new SoundCard();
		this.videoCard = new VideoCard(width, height);
		this.terminal = new Terminal(rows, columns, this);
		this.keyboard = new Keyboard(terminal);
		this.clients = clients;
		this.bootScript = script;
		this.baseDirectory = new File(baseDirectory);
		if (!this.baseDirectory.exists()) {
			this.baseDirectory.mkdirs();
		}
		// Create a finalize guardian
		finalizeGuardian = new Object() {
			@Override
			public void finalize() {
				thisThread.interrupt();
			}
		};
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
		int[] rows = new int[terminal.getRows()];
		for (int i = 0; i < terminal.getRows(); i++)
			rows[i] = i;
		terminal.sendUpdate(client, rows);
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

	@ExposedToLua
	public void dumpTable(List<Long> tablePointers) {
		luaState.pushNil(); // key table permanents thread
		while (luaState.next(-2)) { // value key table permanents thread
			LuaType type = luaState.type(-1);
			switch (type) {
			case STRING:  /* strings */
				System.out.print(luaState.toString(-1) + " | ");
				break;

			case BOOLEAN:  /* booleans */
				System.out.print((luaState.toBoolean(-1) ? "true" : "false") + " | ");
				break;

			case NUMBER:  /* numbers */
				System.out.print(luaState.toNumber(-1) + " | ");
				break;
			case TABLE:
				if (!tablePointers.contains(luaState.toPointer(-1))) {
					tablePointers.add(luaState.toPointer(-1));
					System.out.println();
					dumpTable(tablePointers);
				}
				break;
			default:  /* other values */
				System.out.print(luaState.typeName(-1) + " | ");
				break;
			}
			type = luaState.type(-2);
			switch (type) {
			case STRING:  /* strings */
				System.out.print(luaState.toString(-2) + " | ");
				break;

			case BOOLEAN:  /* booleans */
				System.out.print((luaState.toBoolean(-2) ? "true" : "false") + " | ");
				break;

			case NUMBER:  /* numbers */
				System.out.print(luaState.toNumber(-2) + " | ");
				break;

			default:  /* other values */
				System.out.print(luaState.typeName(-2) + " | ");
				break;
			}
			luaState.pop(1);
			System.out.println();
		}
	}

	/**
	 * Expects the table to parse to be at the top of the stack
	 * Expects a table to store the permanents at position 2
	 */
	@ExposedToLua(value = false)
	public void addToPermanentsTable(int base, boolean flip, List<Long> tablePointers) {
		base -= 4;
		luaState.pushNil(); // key table permanents thread
		while (luaState.next(-2)) { // value key table permanents thread
			if (luaState.isCFunction(-1)) {
				if (flip) {
					luaState.pushValue(-2);
					luaState.pushValue(-2);
				} else {
					luaState.pushValue(-1);
					luaState.pushValue(-3);
				}
				luaState.setTable(base);
			} else if (luaState.isJavaFunction(-1)) {
				if (flip) {
					luaState.pushValue(-2);
					luaState.pushValue(-2);
				} else {
					luaState.pushValue(-1);
					luaState.pushValue(-3);
				}
				luaState.setTable(base);
			} else if (luaState.type(-1) == LuaType.USERDATA || luaState.isJavaObjectRaw(-1) || luaState.type(-1) == LuaType.LIGHTUSERDATA) {
				if (flip) {
					luaState.pushValue(-2);
					luaState.pushValue(-2);
				} else {
					luaState.pushValue(-1);
					luaState.pushValue(-3);
				}
				luaState.setTable(base);
			} else if (luaState.isTable(-1)) {
				if (!tablePointers.contains(luaState.toPointer(-1))) {
					tablePointers.add(luaState.toPointer(-1));
					addToPermanentsTable((base + 2), flip, tablePointers);
					if (luaState.getMetatable(-1)) {
						if (!tablePointers.contains(luaState.toPointer(-1))) {
							tablePointers.add(luaState.toPointer(-1));
							addToPermanentsTable(base + 1, flip, tablePointers);
						}
						luaState.pop(1);
					}
				}
			} else if (luaState.isThread(-1)) {
				if (!tablePointers.contains(luaState.toPointer(-1))) {
					tablePointers.add(luaState.toPointer(-1));
					luaState.getFEnv(-1);
					addToPermanentsTable((base + 1), flip, tablePointers);
					if (luaState.getMetatable(-1)) {
						if (!tablePointers.contains(luaState.toPointer(-1))) {
							tablePointers.add(luaState.toPointer(-1));
							addToPermanentsTable(base, flip, tablePointers);
						}
						luaState.pop(1);
					}
					luaState.pop(1);
					parseLocals((base + 2), flip, -1, tablePointers);
				}
			} else if (luaState.isFunction(-1)) {
				if (!tablePointers.contains(luaState.toPointer(-1))) {
					tablePointers.add(luaState.toPointer(-1));
					luaState.getFEnv(-1);
					addToPermanentsTable((base + 1), flip, tablePointers);
					if (luaState.getMetatable(-1)) {
						if (!tablePointers.contains(luaState.toPointer(-1))) {
							tablePointers.add(luaState.toPointer(-1));
							addToPermanentsTable(base, flip, tablePointers);
						}
						luaState.pop(1);
					}
					luaState.pop(1);
				}
			}
			if (luaState.getMetatable(-1)) {
				if (!tablePointers.contains(luaState.toPointer(-1))) {
					tablePointers.add(luaState.toPointer(-1));
					addToPermanentsTable((base + 1), flip, tablePointers);
				}
				luaState.pop(1);
			}
			luaState.pop(1); // key table permanents thread
		}
	}

	public void parseLocals(int base, boolean flip, int threadIndex, List<Long> tablePointers) {
		int stack = 1;
		int tries = 0;
		base -= 3;
		while (tries < 10) {
			int count = 1;
			String name = "";
			boolean end = true;
			while ((name = luaState.getLocal(stack, count++, threadIndex)) != null) {
				end = false;
				if (luaState.isCFunction(-1)) {
					if (flip) {
						luaState.pushString(name);
						luaState.pushValue(-2);
					} else {
						luaState.pushValue(-1);
						luaState.pushString(name);
					}
					luaState.setTable(base);
				} else if (luaState.isJavaFunction(-1)) {
					if (flip) {
						luaState.pushString(name);
						luaState.pushValue(-2);
					} else {
						luaState.pushValue(-1);
						luaState.pushString(name);
					}
					luaState.setTable(base);
				} else if (luaState.type(-1) == LuaType.USERDATA || luaState.isJavaObjectRaw(-1) || luaState.type(-1) == LuaType.LIGHTUSERDATA) {
					if (flip) {
						luaState.pushString(name);
						luaState.pushValue(-2);
					} else {
						luaState.pushValue(-1);
						luaState.pushString(name);
					}
					luaState.setTable(base);
				} else if (luaState.isTable(-1)) {
					if (!tablePointers.contains(luaState.toPointer(-1))) {
						tablePointers.add(luaState.toPointer(-1));
						addToPermanentsTable(base + 2, flip, tablePointers);
						if (luaState.getMetatable(-1)) {
							if (!tablePointers.contains(luaState.toPointer(-1))) {
								tablePointers.add(luaState.toPointer(-1));
								addToPermanentsTable(base + 1, flip, tablePointers);
							}
							luaState.pop(1);
						}
					}
				} else if (luaState.isThread(-1)) {
					if (!tablePointers.contains(luaState.toPointer(-1))) {
						tablePointers.add(luaState.toPointer(-1));
						luaState.getFEnv(-1);
						addToPermanentsTable(base + 1, flip, tablePointers);
						if (luaState.getMetatable(-1)) {
							if (!tablePointers.contains(luaState.toPointer(-1))) {
								tablePointers.add(luaState.toPointer(-1));
								addToPermanentsTable(base, flip, tablePointers);
							}
							luaState.pop(1);
						}
						luaState.pop(1);
						parseLocals(base + 2, flip, -1, tablePointers);
					}
				} else if (luaState.isFunction(-1)) {
					if (!tablePointers.contains(luaState.toPointer(-1))) {
						tablePointers.add(luaState.toPointer(-1));
						luaState.getFEnv(-1);
						addToPermanentsTable(base + 1, flip, tablePointers);
						if (luaState.getMetatable(-1)) {
							if (!tablePointers.contains(luaState.toPointer(-1))) {
								tablePointers.add(luaState.toPointer(-1));
								addToPermanentsTable(base, flip, tablePointers);
							}
							luaState.pop(1);
						}
						luaState.pop(1);
					}
				}
				if (luaState.getMetatable(-1)) {
					if (!tablePointers.contains(luaState.toPointer(-1))) {
						tablePointers.add(luaState.toPointer(-1));
						addToPermanentsTable(base + 1, flip, tablePointers);
					}
					luaState.pop(1);
				}
				luaState.pop(1);
			}
			if (end) {
				tries++;
			}
			stack++;
		}
	}

	@ExposedToLua(value = false)
	public void loadPlutoState() {
		if (luaStateLock.tryLock()) {
			luaState.getField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
			if (luaState.isNoneOrNil(-1) || !luaState.isOpen()) {
				luaState.pop(1);
				return;
			}
			luaState.newTable();
			luaState.getGlobal("_G");
			addToPermanentsTable(-2, true, new ArrayList<Long>());
			luaState.pop(1);
			luaState.getFEnv(-2);
			if (luaState.isNil(-1)) {
				luaState.pop(2);
				return;
			}
			addToPermanentsTable(-2, true, new ArrayList<Long>());
			parseLocals(-2, true, -3, new ArrayList<Long>());
			luaState.pop(1);
			luaState.remove(-2);
			dumpTable(new ArrayList<Long>());
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
				luaState.getField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
				luaState.getFEnv(-2);
				luaState.newTable();
				luaState.getFEnv(-3);
				luaState.setField(-2, "__index");
				luaState.setMetatable(-2);
				luaState.setFEnv(-3);
				luaState.pop(1);
				luaState.setField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
				luaState.pop(1);
				luaState.install_kill_hook(100);
			} catch (FileNotFoundException e) {
				ElectroCraft.instance.getLogger().severe("Unable to open the persist file for loading!");
			} catch (IOException e) {
				// Unable to close input stream, oh well
			}
			if (luaState.getTop() > 0)
				luaState.pop(luaState.getTop());
		}
	}

	@ExposedToLua(value = false)
	public void savePlutoState() {
		if (luaStateLock.tryLock()) {
			luaState.getField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
			if (luaState.isNoneOrNil(-1) || !luaState.isOpen()) {
				luaState.pop(1);
				return;
			}
			if (luaState.status(-1) != LuaState.YIELD) {
				return;
			}
			luaState.newTable();
			luaState.getGlobal("_G");
			addToPermanentsTable(-2, false, new ArrayList<Long>());
			luaState.pop(1);
			luaState.getFEnv(-2);
			if (luaState.isNil(-1)) {
				luaState.pop(3);
				return;
			}
			addToPermanentsTable(-2, false, new ArrayList<Long>());
			if (luaState.getMetatable(-1)) {
				addToPermanentsTable(-3, false, new ArrayList<Long>());
				luaState.pop(1);
			}
			luaState.pop(1);
			parseLocals(-1, false, -2, new ArrayList<Long>());
			// permanents thread
			try {
				luaState.pushValue(-2); // thread perm thread
				luaState.remove(-3); // thread perm

				File persistFile = new File(baseDirectory, ".persist");
				if (!persistFile.exists())
					persistFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(persistFile);
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
	}

	@ExposedToLua(value = false)
	public LuaState getAndLockLuaState() {
		luaStateLock.lock();
		return luaState;
	}

	public void tryUnlockLuaState() {
		if (luaStateLock.isHeldByCurrentThread())
			luaStateLock.unlock();
	}

	@ExposedToLua(value = false)
	public void tick() {
		if (getKeyboard().getKeysInBuffer() > 0) {
			if (getKeyboard().peak() > Character.MAX_VALUE)
				postEvent("code", getKeyboard().popKey());
			else
				postEvent("key", getKeyboard().popChar());
		}

		if (luaStateLock.tryLock()) {
			for (LuaAPI api : apis) {
				api.tick(this);
			}
			luaStateLock.unlock();
		}

		if (!ticked)
			ticked = true;
	}

	@ExposedToLua(value = false)
	public void postEvent(String eventName, Object... args) {
		synchronized (eventLock) {
			Event e = new Event();
			e.eventName = eventName;
			e.args = args;
			eventQueue.add(e);
		}
	}

	@ExposedToLua(value = false)
	protected void loadBios() {
		luaStateLock.lock();
		try {
			luaState.load(Computer.class.getResourceAsStream("/info/cerios/electrocraft/rom/bios.lua"), "bios_" + baseDirectory.getName());
			luaState.newThread();
			luaState.setField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
		} catch (IOException e) {
			getTerminal().print("Unable to load the BIOS check that you have installed ElectroCraft correctly");
		}
		luaStateLock.unlock();
	}

	@ExposedToLua(value = false)
	public void callSave() {
		if (running && luaState.isOpen()) {
			if (luaStateLock.tryLock()) {
//				savePlutoState();
				programStorage = new NBTTagCompound(currentProgram);
				for (String m : onSaveMethods) {
					try {
						luaState.getGlobal(m);
						if (luaState.type(-1) != LuaType.FUNCTION) {
							onSaveMethods.remove(m);
							luaState.pop(1);
							getTerminal().print("Invalid save method: " + m);
							continue;
						}
						luaState.newThread();
						luaState.setField(LuaState.REGISTRYINDEX, "save_thread");

						try {
							luaState.getField(LuaState.GLOBALSINDEX, "coroutine");
							luaState.getField(-1, "resume");
							luaState.remove(-2);
							luaState.getField(LuaState.REGISTRYINDEX, "save_thread");
							luaState.pushJavaObject(programStorage);
							luaState.call(2, 0);
							luaState.reset_kill(1000);
							if (luaState.isBoolean(1))
								if (!luaState.checkBoolean(1, true))
									throw new LuaRuntimeException("Runtime error!");
							luaState.pop(luaState.getTop());

							luaState.getField(LuaState.REGISTRYINDEX, "save_thread");
							while (luaState.status(-1) == LuaState.YIELD) {
								luaState.getField(LuaState.GLOBALSINDEX, "coroutine");
								luaState.getField(-1, "resume");
								luaState.remove(-2);
								luaState.getField(LuaState.REGISTRYINDEX, "save_thread");
								luaState.call(1, 0);
								luaState.reset_kill(1000);
								if (luaState.isBoolean(1))
									if (!luaState.checkBoolean(1, true))
										throw new LuaRuntimeException("Runtime error!");
								if (luaState.getTop() == 1) {
									break;
								}
								luaState.pop(luaState.getTop());
								luaState.getField(LuaState.REGISTRYINDEX, "save_thread");
							}
							luaState.pop(luaState.getTop());
						} catch (LuaSyntaxException e) {
							getTerminal().print("Syntax Error!");
							e.printStackTrace(new PrintWriter(getTerminal()));
						} catch (LuaRuntimeException e) {
							getTerminal().print("Runtime Error!");
							if (luaState.isString(-1))
								getTerminal().print(luaState.checkString(-1));
							else {
								e.printLuaStackTrace(new PrintWriter(getTerminal()));
							}
							e.printLuaStackTrace();
							this.setRunning(false);
						}
					} catch (LuaSyntaxException e) {
						System.out.println("Error running lua script: Syntax Error!");
						System.out.println(e.getLocalizedMessage());
					} catch (LuaRuntimeException e) {
						System.out.println("Error running lua script: Runtime Error!");
						System.out.println("Possibly went to long without yielding?");
						e.printLuaStackTrace();
					}
					luaState.reset_kill(100);
					luaStateLock.unlock();
				}
			}
		}
	}

	@ExposedToLua(value = false)
	public void callLoad() {
		if (running) {
			stateLock.lock();
			wasResumed = true;
			stateLock.unlock();
			thisThread = new Thread(this);
			thisThread.start();
			postEvent("resume", programStorage);
		}
	}

	@ExposedToLua(value = false)
	public void loadAPI(LuaAPI api) {
		apis.add(api);
		luaStateLock.lock();
		luaState.register(api.getNamespace(), api.getGlobalFunctions(this));
		luaState.setGlobal(api.getNamespace());
		luaStateLock.unlock();
	}

	@ExposedToLua(value = false)
	protected void loadLuaDefaults() {
		luaStateLock.lock();
		// Create a new state
		if (luaState != null && luaState.isOpen())
			luaState.close();
		luaState = new LuaState();
		// Load the allowed libraries
		luaState.openLib(Library.BASE);
		luaState.openLib(Library.DEBUG);
		luaState.openLib(Library.MATH);
		luaState.openLib(Library.PLUTO);
		luaState.openLib(Library.STRING);
		luaState.openLib(Library.TABLE);

		// Reseting it, if not enabled will enable it
		luaState.install_kill_hook(100);

		// Load base ElectroCraft functions
		NamedJavaFunction[] osFunctions = new NamedJavaFunction[] {
				new NamedJavaFunction() {
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
				}.init(this),

				new NamedJavaFunction() {
					@Override
					public int invoke(LuaState luaState) {
						return luaState.yield(1);
					}

					@Override
					public String getName() {
						return "sleep";
					}
				},

				new NamedJavaFunction() {
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
				}.init(this),

				new NamedJavaFunction() {
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
				}.init(this),

				new NamedJavaFunction() {
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
				}.init(this),

				new NamedJavaFunction() {
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
				}.init(this)
		};
		luaState.register("os", osFunctions);
		luaState.pop(1);

		// Load ElectroCraft default libraries
		loadAPI(new ComputerFile());
		loadAPI(new Network());
		loadAPI(new EndNet());
		loadAPI(mcIO = new MinecraftInterface());

		luaStateLock.unlock();
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
		if (sleepTimer != null)
			sleepTimer.cancel();
		sleepTimer = null;
		thisThread.interrupt();
	}

	@ExposedToLua
	public void start() {
		if (!running) {
			stateLock.lock();
			running = true;
			stateLock.unlock();
			thisThread = new Thread(this);
			thisThread.start();
			postEvent("start");
		}
	}

	@ExposedToLua(value = false)
	public void setRunning(boolean value) {
		stateLock.lock();
		running = value;
		if (value == false && luaState != null && luaState.isOpen())
			luaState.close();
		if (value == false && thisThread != null)
			thisThread.interrupt();
		stateLock.unlock();
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
	public synchronized boolean isRunning() {
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

	@Override
	public void run() {
		boolean first = true;
		// Register the Lua thread with the security manager
		ElectroCraft.instance.getSecurityManager().registerThread(this);

		int ticksSinceLastSave = 0;

		while (isRunning() && ElectroCraft.instance.isRunning()) {
			if (luaStateLock.tryLock() && first) {
				loadLuaDefaults();
				loadBios();
				first = false;
				luaStateLock.unlock();
			} else if (first) {
				if (luaStateLock.isHeldByCurrentThread())
					luaStateLock.unlock();
				continue;
			}
			if (luaStateLock.isHeldByCurrentThread())
				luaStateLock.unlock();
			stateLock.lock();
			if (!isRunning() && ElectroCraft.instance.isRunning()) {
				stateLock.unlock();
				break;
			}
			// Update the terminal to send any pending terminal packets
			FutureTask<Boolean> termTask = new FutureTask<Boolean>(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					terminal.updateTick();
					return true;
				}
			});
			ElectroCraft.instance.registerRunnable(termTask);
			try {
				termTask.get();
			} catch (InterruptedException e1) {
			} catch (ExecutionException e1) {
				e1.printStackTrace();
			} catch (CancellationException e1) {
			}

			if (!ticked) {
				stateLock.unlock();
				continue;
			}

			if (killYielded)
				postEvent("killyield");

			if (eventQueue.size() > 0) {
				List<Event> copy;
				synchronized (eventLock) {
					copy = new ArrayList<Event>(eventQueue);
					eventQueue.clear();
				}
				for (Event event : copy) {
					String eventName = event.eventName;
					Object[] args = event.args;

					if (!finishedSleeping)
						return;
					if (killYielded)
						killYielded = false;
					if (!running)
						return;
					try {
						if (luaStateLock.tryLock()) {
							if (!luaState.isOpen())
								return;

							luaState.getField(LuaState.GLOBALSINDEX, "coroutine");
							luaState.getField(-1, "resume");
							luaState.remove(-2);
							int argSize = 0;

							luaState.getField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
							if (luaState.type(-1) != LuaType.THREAD || (luaState.status(-1) != LuaState.YIELD && luaState.status(-1) != 0)) {
								// Oops we must of shutdown or something
								luaState.pop(luaState.getTop());
								return;
							} else {
								// Its a valid thread
								argSize += 1;
							}

							if (argSize <= 0) {
								// the thread is non existent
								luaState.pop(luaState.getTop());
								return;
							}
							// Lets check if we should push arguments
							if (!eventName.equalsIgnoreCase("resume") && !eventName.equalsIgnoreCase("killyield")) {
								// Its not a blacklisted event name lets push the arguments
								luaState.pushString(eventName);
								for (Object arg : args) {
									luaState.pushJavaObject(arg);
								}
								argSize += 1 + args.length;
							}

							// Lets call the coroutine
							luaState.call(argSize, LuaState.MULTRET);
							// Reset the yield kill line counter to 100 lines
							luaState.reset_kill(100);
							if (killYielded)
								killYielded = false;

							// Check the results
							if (luaState.isBoolean(1))
								if (!luaState.checkBoolean(1, true))
									throw new LuaRuntimeException("Runtime error!");
							if (luaState.isNumber(-1)) {
								finishedSleeping = false;
								if (sleepTimer != null)
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

							// Lets make sure the stack is clean
							luaState.pop(luaState.getTop());

							// Load the pluto state
//							if (eventName.equalsIgnoreCase("resume")) {
//								loadPlutoState();
//							}

							// System memory check
							if (luaState.gc(GcAction.COUNT, 0) > ConfigHandler.getCurrentConfig().get("computer", "MaxMemPerUser", 16).getInt(16) * 1024) {
								setRunning(false);
								getTerminal().print("ERROR: Ran out of memory! Max memory is: " + String.valueOf(ConfigHandler.getCurrentConfig().get("computer", "MaxMemPerUser", 16).getInt(16)) + "M");
							}

							// Extra backup check in case my wrapped file manager doesn't catch it
							if (getBaseDirectory().length() > ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10) * 1024 * 1024) {
								setRunning(false);
								getTerminal().print("ERROR: Ran out of storage! Max storage space is: " + String.valueOf(ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10)) + "M");
							}

							if (ticksSinceLastSave % 900 == 0) {
								callSave();
								ticksSinceLastSave = 0;
							} else {
								ticksSinceLastSave++;
							}

							luaStateLock.unlock();
						}
					} catch (LuaSyntaxException e) {
						getTerminal().print("Syntax Error!");
						e.printStackTrace(new PrintWriter(getTerminal()));
						// Lets make sure the stack is clean
						luaState.pop(luaState.getTop());
						if (luaStateLock.isHeldByCurrentThread())
							luaStateLock.unlock();
						return;
					} catch (LuaRuntimeException e) {
						getTerminal().print("Runtime Error!");
						if (luaState.isString(-1))
							getTerminal().print(luaState.checkString(-1));
						else {
							e.printLuaStackTrace(new PrintWriter(getTerminal()));
							e.printLuaStackTrace();
						}
						// Lets make sure the stack is clean
						luaState.pop(luaState.getTop());
						if (luaStateLock.isHeldByCurrentThread())
							luaStateLock.unlock();
						stateLock.unlock();
						this.shutdown();
						return;
					}
				}
			}
			stateLock.unlock();
		}
		this.running = false;
	}

	private class Event {
		public String eventName;
		public Object[] args;
	}
}
