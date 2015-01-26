package me.querol.electrocraft.core.computer;

import me.querol.com.naef.jnlua.LuaException;
import me.querol.com.naef.jnlua.LuaRuntimeException;
import me.querol.electrocraft.api.computer.ExposedToLua;
import me.querol.electrocraft.api.computer.NetworkBlock;
import me.querol.electrocraft.api.computer.luaapi.LuaAPI;
import me.querol.electrocraft.core.ConfigHandler;
import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.computer.luaapi.*;
import me.querol.electrocraft.core.computer.luaapi.SystemAPI;
import me.querol.electrocraft.core.network.CustomPacket;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.FutureTask;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import me.querol.com.naef.jnlua.LuaState;
import me.querol.com.naef.jnlua.LuaState.Library;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

@ExposedToLua
public class Computer implements Runnable {

    public enum State {
        STARTING,
        YIELD,
        RUNNING,
        MC_CALL,
        MC_RETURN,
        SLEEPING,
        PAUSED,
        STOPPED,
        STOPPING,
        REBOOTING
    }

    public class Message {
        public final String name;
        public final Object data;

        public Message(String name, Object data) {
            this.name = name;
            this.data = data;
        }
    }

    private Terminal terminal;
    private VideoCard videoCard;
    private Keyboard keyboard;
    private MinecraftInterface mcIO;
    private boolean graphicsMode = false;
    private List<EntityPlayer> clients;
    private File baseDirectory;
    private int openFileHandles = 0;
    protected LuaState luaState;
    private Set<LuaAPI> apis = new HashSet<LuaAPI>();
    private NBTTagCompound programStorage;
    private long sleepTicks = 0;
    private volatile boolean isMcPaused = false;
    private boolean started = false;

    private final Stack<State> stateStack = new Stack<State>();
    protected final Stack<FutureTask<?>> computerTasks = new Stack<FutureTask<?>>();
    protected final Stack<FutureTask<?>> minecraftTasks = new Stack<FutureTask<?>>();
    private final Stack<Message> messages = new Stack<Message>();

    private long startTime = 0l;

    @ExposedToLua(value = false)
    public Computer(List<EntityPlayer> clients, String baseDirectory, int width, int height, int rows, int columns) {
        this.videoCard = new VideoCard(width, height);
        this.terminal = new Terminal(rows, columns, this);
        this.keyboard = new Keyboard(terminal);
        this.clients = clients;
        this.baseDirectory = new File(baseDirectory);
        if (!this.baseDirectory.exists()) {
            this.baseDirectory.mkdirs();
        }
        synchronized (stateStack) {
            stateStack.push(State.STOPPED);
        }
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
        if (!ConfigHandler.getCurrentConfig().get("general", "useMCServer", false).getBoolean(false)) {
            ElectroCraft.instance.getServer().getClient((EntityPlayerMP) client).changeModes(!graphicsMode);
        } else {
            CustomPacket packet = new CustomPacket();
            packet.id = 0;
            packet.data = new byte[] { (byte) (!graphicsMode ? 1 : 0) };
            ElectroCraft.instance.getNetworkWrapper().sendTo(packet, (EntityPlayerMP) client);
        }
        int[] rows = new int[terminal.getRows()];
        for (int i = 0; i < terminal.getRows(); i++) {
            rows[i] = i;
        }
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
    public void initPermanentsTables() {
        luaState.newTable();
        luaState.newTable();

        int permLocation = luaState.getTop() - 1;
        int unpermLocation = luaState.getTop();
        luaState.pushString("_G");
        luaState.getGlobal("_G");

        storePermanents(permLocation, unpermLocation);

        luaState.setField(LuaState.REGISTRYINDEX, "unperms");
        luaState.setField(LuaState.REGISTRYINDEX, "perms");
    }

    private void storePermanents(int permTable, int upermTable) {
        if (luaState.isFunction(-1) || luaState.isTable(-1)) {
            luaState.pushValue(-2);
            luaState.getTable(upermTable);
            luaState.pop(1);

            luaState.pushValue(-1);
            luaState.getTable(permTable);

            boolean newValue = luaState.isNil(-1);
            luaState.pop(1);
            if (newValue) {
                luaState.pushValue(-1);
                luaState.pushValue(-3);
                luaState.rawSet(permTable);
                luaState.pushValue(-2);
                luaState.pushValue(-2);
                luaState.rawSet(upermTable);

                // Continue recusion if its a table
                if (luaState.isTable(-1)) {
                    String key = luaState.toString(-2);
                    ArrayList<String> childKeys = new ArrayList<String>();
                    luaState.pushNil();

                    while (luaState.next(-2)) {
                        luaState.pop(1);
                        childKeys.add(luaState.toString(-1));
                    }

                    childKeys.sort(new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return o1.compareTo(o2);
                        }
                    });

                    for (String childKey : childKeys) {
                        luaState.pushString(key + "." + childKey);
                        luaState.getField(-2, childKey);
                        storePermanents(permTable, upermTable);
                    }
                }
            }
        }
        luaState.pop(2);
    }

    @ExposedToLua(value = false)
    public void tick() {

        if (!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer() && FMLCommonHandler.instance().getMinecraftServerInstance() instanceof IntegratedServer)
            isMcPaused = Minecraft.getMinecraft().isGamePaused();

        // Execute anything needed on the main thread
        while (!minecraftTasks.empty()) {
            FutureTask<?> task = minecraftTasks.pop();
            task.run();
        }

        if (getKeyboard().getKeysInBuffer() > 0) {

        }
    }

    @ExposedToLua(value = false)
    protected void loadBios() {
        try {
            luaState.load(Computer.class.getResourceAsStream("/me/querol/electrocraft/rom/bios.lua"), "bios_" + baseDirectory.getName(), "bt");
            luaState.newThread();
            luaState.setField(LuaState.REGISTRYINDEX, "electrocraft_coroutine");
        } catch (IOException e) {
            getTerminal().print("Unable to load the BIOS check that you have installed ElectroCraft correctly");
        }
    }

    @ExposedToLua(value = false)
    public void loadAPI(LuaAPI api) {
        apis.add(api);
        luaState.register(api.getNamespace(), api.getGlobalFunctions(this), true);
        luaState.setGlobal(api.getNamespace());
    }

    @ExposedToLua(value = false)
    private State switchStates(State state) {
        State lastState = null;
        synchronized (stateStack) {
            lastState = stateStack.pop();

            if (state == State.STOPPING || state == State.REBOOTING) {
                stateStack.clear();
            }
            stateStack.push(state);
            if (state == State.YIELD || state == State.MC_RETURN) {
                sleepTicks = 0;
                ComputerThreadHandler.getInstance().enqueue(this);
            }
        }

        return lastState;
    }

    @ExposedToLua(value = false)
    public void start() {
        synchronized (stateStack) {
            stateStack.push(State.STARTING);
        }
    }

    @ExposedToLua(value = false)
    protected void initComputer() {
        // Create a new state
        if (luaState != null && luaState.isOpen()) {
            luaState.close();
        }
        luaState = new LuaState();
        // Load the allowed libraries
        luaState.openLib(Library.BASE);
        luaState.openLib(Library.DEBUG);
        luaState.openLib(Library.MATH);
        luaState.openLib(Library.STRING);
        luaState.openLib(Library.TABLE);

        // Load ElectroCraft default libraries
        loadAPI(new SystemAPI());
        loadAPI(new ComputerFile());
        loadAPI(new Network());
        loadAPI(new EndNet());
        loadAPI(mcIO = new MinecraftInterface());

        // Setup the persistence
        initPermanentsTables();

        // Load the bios
        loadBios();
    }

    @ExposedToLua(value = false)
    @Override
    public void run() {
        boolean mcReturned = false;
        synchronized(stateStack) {
            if (!(stateStack.peek() != State.YIELD && stateStack.peek() != State.MC_RETURN)) {
                if (isMcPaused) {
                    stateStack.push(State.PAUSED);
                } else {
                    mcReturned = switchStates(State.RUNNING) == State.MC_RETURN;
                }
            }
        }
        synchronized (stateStack) {
            int numberOfResults = 0;
            try {
                assert luaState.isThread(1);
                if (mcReturned) {
                    assert luaState.getTop() == 2;
                    assert luaState.isTable(2);
                    numberOfResults = luaState.resume(1, 1);
                } else {
                    if (!started) {
                        if (luaState.resume(1, 0) != 0) {
                            throw new LuaRuntimeException("Error starting lua thread");
                        }
                        luaState.gc(LuaState.GcAction.COLLECT, 0);
                        started = true;
                    } else {
                        synchronized (messages) {
                            Message message = messages.pop();
                            if (message != null) {
                                luaState.pushString(message.name);
                                luaState.pushJavaObject(message.data);
                                luaState.resume(1, 2);
                            } else {
                                luaState.resume(1, 0);
                            }
                        }
                    }
                }
            } catch (LuaRuntimeException e) {
                ElectroCraft.instance.getLogger().warning(e.getLocalizedMessage());
            }

            if (luaState.status(1) == LuaState.YIELD) {
                // Computer is still alive
                if (numberOfResults == 1) {
                    if (luaState.isFunction(2)) {
                        // TODO Call a function that is on the main thread. AKA most of them
                    } else if (luaState.isBoolean(2)) {
                        // TODO Shutdown on false, or reboot
                    } else if (luaState.isNumber(2)) {
                        // We are sleeping
                        sleepTicks = (long) luaState.toNumber(2);
                    }
                }
            } else {
                // TODO handle the termination
            }

            switch (stateStack.peek()) {
                case RUNNING: {
                    break;
                }
                case PAUSED: {
                    break;
                }
            }
        }
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
        if (this.graphicsMode != graphicsMode) {
            for (EntityPlayer p : clients) {
                if (!ConfigHandler.getCurrentConfig().get("general", "useMCServer", false).getBoolean(false)) {
                    ElectroCraft.instance.getServer().getClient((EntityPlayerMP) p).changeModes(!graphicsMode);
                } else {
                    CustomPacket packet = new CustomPacket();
                    packet.id = 0;
                    packet.data = new byte[] { (byte) (!graphicsMode ? 1 : 0) };
                    ElectroCraft.instance.getNetworkWrapper().sendTo(packet, (EntityPlayerMP) p);
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
        sleepTicks = 0;
        synchronized (stateStack) {
            stateStack.push(State.STOPPING);
        }
    }

    @ExposedToLua(value = false)
    public File getBaseDirectory() {
        return baseDirectory;
    }

    @ExposedToLua
    public synchronized boolean isRunning() {
        return stateStack.peek() == State.RUNNING;
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
    public void registerNetworkBlock(NetworkBlock block) {
        if (mcIO != null) {
            mcIO.register(block);
        }
    }

    @ExposedToLua(value = false)
    public void removeNetworkBlock(NetworkBlock block) {
        if (mcIO != null) {
            mcIO.remove(block);
        }
    }

    public long getStartTime() {
        return startTime;
    }
}
