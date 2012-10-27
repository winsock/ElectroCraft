package info.cerios.electrocraft.core;

import info.cerios.electrocraft.api.IComputerHost;
import info.cerios.electrocraft.api.IElectroCraft;
import info.cerios.electrocraft.api.computer.IComputerCallback;
import info.cerios.electrocraft.api.computer.IComputerRunnable;
import info.cerios.electrocraft.api.computer.IMCRunnable;
import info.cerios.electrocraft.api.drone.tools.IDroneTool;
import info.cerios.electrocraft.api.drone.upgrade.ICard;
import info.cerios.electrocraft.api.utils.ObjectPair;
import info.cerios.electrocraft.core.blocks.BlockHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlock;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.ComputerSocketManager;
import info.cerios.electrocraft.core.computer.LuaSecurity;
import info.cerios.electrocraft.core.container.ContainerDrone;
import info.cerios.electrocraft.core.entites.EntityDrone;
import info.cerios.electrocraft.core.items.ElectroItems;
import info.cerios.electrocraft.core.items.ItemHandler;
import info.cerios.electrocraft.core.network.ComputerServer;
import info.cerios.electrocraft.core.network.ConnectionHandler;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.UniversialPacketHandler;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

@Mod(modid = "electrocraft", name = "ElectroCraft", version = "Beta 0.30")
@NetworkMod(channels = {"electrocraft"}, clientSideRequired = true, serverSideRequired = false, packetHandler=UniversialPacketHandler.class, connectionHandler = ConnectionHandler.class)
public class ElectroCraft implements IElectroCraft {

	@Mod.Instance("electrocraft")
	public static ElectroCraft instance;
	@SidedProxy(clientSide = "info.cerios.electrocraft.ElectroCraftClient", serverSide = "info.cerios.electrocraft.ElectroCraftSidedServer")
	public static IElectroCraftSided electroCraftSided;
	private Map<EntityPlayer, IComputerHost> nonCustomServerComputerMap = new HashMap<EntityPlayer, IComputerHost>();
	private List<FutureTask<?>> mainThreadFunctions = new ArrayList<FutureTask<?>>();
	private List<ModContainer> addons = new ArrayList<ModContainer>();
	private List<ICard> droneUpgradeCards = new ArrayList<ICard>();
	private List<IDroneTool> droneTools = new ArrayList<IDroneTool>();
	private Object mainThreadLock = new Object();
	private LuaSecurity securityManager;

	// The Mod's color palette
	public static final int[] colorPalette = {
		0x000000,0x800000,0x008000,0x808000,0x000080,0x800080,0x008080,0xc0c0c0,0x808080,0xff0000,0x00ff00,0xffff00,0x0000ff,0xff00ff,0x00ffff,0xffffff,0x000000,0x00005f,0x000087,0x0000af,0x0000d7,0x0000ff,0x005f00,0x005f5f,0x005f87,0x005faf,0x005fd7,0x005fff,0x008700,0x00875f,0x008787,0x0087af,0x0087d7,0x0087ff,0x00af00,0x00af5f,0x00af87,0x00afaf,0x00afd7,0x00afff,0x00d700,0x00d75f,0x00d787,0x00d7af,0x00d7d7,0x00d7ff,0x00ff00,0x00ff5f,0x00ff87,0x00ffaf,0x00ffd7,0x00ffff,0x5f0000,0x5f005f,0x5f0087,0x5f00af,0x5f00d7,0x5f00ff,0x5f5f00,0x5f5f5f,0x5f5f87,0x5f5faf,0x5f5fd7,0x5f5fff,0x5f8700,0x5f875f,0x5f8787,0x5f87af,0x5f87d7,0x5f87ff,0x5faf00,0x5faf5f,0x5faf87,0x5fafaf,0x5fafd7,0x5fafff,0x5fd700,0x5fd75f,0x5fd787,0x5fd7af,0x5fd7d7,0x5fd7ff,0x5fff00,0x5fff5f,0x5fff87,0x5fffaf,0x5fffd7,0x5fffff,0x870000,0x87005f,0x870087,0x8700af,0x8700d7,0x8700ff,0x875f00,0x875f5f,0x875f87,0x875faf,0x875fd7,0x875fff,0x878700,0x87875f,0x878787,0x8787af,0x8787d7,0x8787ff,0x87af00,0x87af5f,0x87af87,0x87afaf,0x87afd7,0x87afff,0x87d700,0x87d75f,0x87d787,0x87d7af,0x87d7d7,0x87d7ff,0x87ff00,0x87ff5f,0x87ff87,0x87ffaf,0x87ffd7,0x87ffff,0xaf0000,0xaf005f,0xaf0087,0xaf00af,0xaf00d7,0xaf00ff,0xaf5f00,0xaf5f5f,0xaf5f87,0xaf5faf,0xaf5fd7,0xaf5fff,0xaf8700,0xaf875f,0xaf8787,0xaf87af,0xaf87d7,0xaf87ff,0xafaf00,0xafaf5f,0xafaf87,0xafafaf,0xafafd7,0xafafff,0xafd700,0xafd75f,0xafd787,0xafd7af,0xafd7d7,0xafd7ff,0xafff00,0xafff5f,0xafff87,0xafffaf,0xafffd7,0xafffff,0xd70000,0xd7005f,0xd70087,0xd700af,0xd700d7,0xd700ff,0xd75f00,0xd75f5f,0xd75f87,0xd75faf,0xd75fd7,0xd75fff,0xd78700,0xd7875f,0xd78787,0xd787af,0xd787d7,0xd787ff,0xd7af00,0xd7af5f,0xd7af87,0xd7afaf,0xd7afd7,0xd7afff,0xd7d700,0xd7d75f,0xd7d787,0xd7d7af,0xd7d7d7,0xd7d7ff,0xd7ff00,0xd7ff5f,0xd7ff87,0xd7ffaf,0xd7ffd7,0xd7ffff,0xff0000,0xff005f,0xff0087,0xff00af,0xff00d7,0xff00ff,0xff5f00,0xff5f5f,0xff5f87,0xff5faf,0xff5fd7,0xff5fff,0xff8700,0xff875f,0xff8787,0xff87af,0xff87d7,0xff87ff,0xffaf00,0xffaf5f,0xffaf87,0xffafaf,0xffafd7,0xffafff,0xffd700,0xffd75f,0xffd787,0xffd7af,0xffd7d7,0xffd7ff,0xffff00,0xffff5f,0xffff87,0xffffaf,0xffffd7,0xffffff,0x080808,0x121212,0x1c1c1c,0x262626,0x303030,0x3a3a3a,0x444444,0x4e4e4e,0x585858,0x606060,0x666666,0x767676,0x808080,0x8a8a8a,0x949494,0x9e9e9e,0xa8a8a8,0xb2b2b2,0xbcbcbc,0xc6c6c6,0xd0d0d0,0xdadada,0xe4e4e4,0xeeeeee
	};

	private ComputerServer server;
	private ComputerSocketManager computerSocketManager;

	/**
	 * Electrocraft's Logger
	 */
	private Logger ecLogger;
	public static final String ecLoggerName = "ElectroCraft";

	public ElectroCraft() {
		instance = this;
	}

	@Mod.PreInit
	public void preInit(FMLPreInitializationEvent event) {
		// Create and load the config
		ConfigHandler.loadOrCreateConfigFile("default.cfg");
		ConfigHandler.getCurrentConfig().get(Configuration.CATEGORY_GENERAL, "useMCServer", true);
		ConfigHandler.getCurrentConfig().save();
	}

	@Mod.Init
	public void init(FMLInitializationEvent event) {    
		// Set up the logger
		ecLogger = Logger.getLogger(ecLoggerName);
		ElectroCraftLogHandler consoleHandler = new ElectroCraftLogHandler();
		consoleHandler.setFormatter(new ElectroCraftLogFormatter());
		ecLogger.setUseParentHandlers(false);
		ecLogger.addHandler(consoleHandler);

		// Log that we are starting
		ecLogger.info("Loading version: " + FMLCommonHandler.instance().findContainerFor(this).getDisplayVersion());

		// Initialize any sided methods
		electroCraftSided.init();

		// Register our renderers
		electroCraftSided.registerRenderers();

		// Register the drone entity
		EntityRegistry.registerGlobalEntityID(EntityDrone.class, "ecdrone", EntityRegistry.findGlobalUniqueEntityId(), 0xFFFFFF, 0x000000);
		EntityRegistry.registerModEntity(EntityDrone.class, "ecdrone", 0, this, 128, 1, true);

		// Register our world generator
		GameRegistry.registerWorldGenerator(new WorldGenerator());

		// Create the item handler
		ItemHandler itemHandler = new ItemHandler();

		// Create and register the ore handler
		BlockHandler oreHandler = new BlockHandler(itemHandler);

		// Register the ore registration event
		MinecraftForge.EVENT_BUS.register(oreHandler);

		// Register our items and blocks
		//*NOTE* THIS POSISTION IS IMPORTANT, IT HAS TO GO AFTER THE EVENET BUS REGESTRATION *NOTE*
		oreHandler.registerBlocks();
		itemHandler.registerItems();

		// Register the recipes
		registerBaseRecipes();

		// Preload the textures
		electroCraftSided.loadTextures();

		// Register client stuff
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			TickRegistry.registerScheduledTickHandler(electroCraftSided.getTickHandler(), Side.CLIENT);
		}

		// Register the tick handler for delegating back to the main thread
		UniversialTickHandler tickHandler = new UniversialTickHandler();
		TickRegistry.registerScheduledTickHandler(tickHandler, Side.SERVER);

		// Register our GUI handler
		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);

		// Create out computer socket connection manager
		computerSocketManager = new ComputerSocketManager();
		
		// Create the security manager
		securityManager = new LuaSecurity("ec");

		// Log that we are done loading
		ecLogger.info("Done loading version: " + FMLCommonHandler.instance().findContainerFor(this).getDisplayVersion());
	}
	
	public LuaSecurity getSecurityManager() {
		return securityManager;
	}

	private IGuiHandler guiHandler = new IGuiHandler() {
		@Override
		public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
			Entity entity = null;
			if ((entity = getEntityByID(ID, world)) != null && entity instanceof EntityDrone)
				return new ContainerDrone(((EntityDrone) entity).getInventory(), player.inventory);
			return null;
		}

		@Override
		public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
			Entity entity = null;
			if ((entity = getEntityByID(ID, world)) != null && entity instanceof EntityDrone)
				return electroCraftSided.getClientGuiFor(Gui.DRONE_INVENTORY, new ContainerDrone(((EntityDrone) entity).getInventory(), player.inventory));
			return null;
		}
	};

	public Entity getEntityByID(int entityID, World world) {
		for (Object o: world.loadedEntityList) {
			if (!(o instanceof Entity))
				continue;
			if (((Entity)o).entityId == entityID) {
				return ((Entity)o);
			}
		}
		return null;
	}

	@Mod.ServerStarting
	public void onServerStarting(FMLServerStartingEvent event) {
		if (!ConfigHandler.getCurrentConfig().get(Configuration.CATEGORY_GENERAL, "useMCServer", false).getBoolean(false)) {
			try {
				// First try the specified port
				server = new ComputerServer(ConfigHandler.getCurrentConfig().get("general", "serverport", 1337).getInt(1337));
				new Thread(server).start();
			} catch (IOException e) {
				try {
					// Otherwise try to get a free port
					server = new ComputerServer(0);
					new Thread(server).start();
				} catch (IOException e1) {
					getLogger().severe("ElectroCraft Server: Tried to start server on port: " + ConfigHandler.getCurrentConfig().get("general", "serverport", 1337).getInt(1337) + " and a random free port and failed!");
				}
			}
		}
	}

	@Mod.ServerStopping
	public void onServerStopping(FMLServerStoppingEvent event) {
		synchronized(mainThreadLock) {
			for (FutureTask<?> task : mainThreadFunctions) {
				task.cancel(true);
			}
			mainThreadFunctions.clear();
			if (ConfigHandler.getCurrentConfig().get(Configuration.CATEGORY_GENERAL, "useMCServer", false).getBoolean(false)) {
				this.nonCustomServerComputerMap.clear();
			} else {
				server.setRunning(false);
				server = null;
			}
		}
	}

	public ComputerServer getServer() {
		return server;
	}

	public IComputerHost getComputerForPlayer(EntityPlayer player) {
		if (ConfigHandler.getCurrentConfig().get(Configuration.CATEGORY_GENERAL, "useMCServer", false).getBoolean(false)) {
			return this.nonCustomServerComputerMap.get(player);
		} else {
			return getServer().getClient((EntityPlayerMP) player).getComputer();
		}
	}

	public void setComputerForPlayer(EntityPlayer player, IComputerHost computer) {
		if (ConfigHandler.getCurrentConfig().get(Configuration.CATEGORY_GENERAL, "useMCServer", false).getBoolean(false)) {
			this.nonCustomServerComputerMap.put(player, computer);
		} else {
			getServer().getClient((EntityPlayerMP) player).setComputer(computer);
		}
	}

	public ComputerSocketManager getComputerSocketManager() {
		return computerSocketManager;
	}

	private void registerBaseRecipes() {
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ElectroItems.ELECTRO_DUST.getItem(), 4), Item.redstone, "magnetiteDust", "magnetiteDust", "magnetiteDust"));
		GameRegistry.addRecipe(new ItemStack(ElectroBlocks.SERIAL_CABLE.getBlock(), 16), "WWW", "III", "WWW", 'I', Item.ingotIron, 'W', Block.cloth);
		GameRegistry.addRecipe(new ItemStack(ElectroBlocks.COMPUTER.getBlock()), "III", "ICI", "IEI", 'E', ElectroItems.ELECTRO_DUST.getItem(), 'I', Item.ingotIron, 'C', Item.compass);
		GameRegistry.addRecipe(new ItemStack(ElectroBlocks.REDSTONE_ADAPTER.getBlock()), "III", "ICI", "IRI", 'R', Item.redstone, 'I', Item.ingotIron, 'C', Block.thinGlass);
		GameRegistry.addRecipe(new ItemStack(ElectroItems.DRONE_UPGRADES.getItem(), 1, 0), "EDE", "PGP", "DPD", 'E', Item.emerald, 'D', ElectroItems.ELECTRO_DUST.getItem(), 'G', Item.ghastTear, 'P', Item.enderPearl);
		GameRegistry.addRecipe(new ItemStack(ElectroItems.DRONE_UPGRADES.getItem(), 1, 1), "RDR", "PGP", "ETE", 'E', Item.emerald, 'D', ElectroItems.ELECTRO_DUST.getItem(), 'R', Item.redstone, 'D', Block.torchRedstoneActive, 'P', Item.enderPearl, 'G', Item.ghastTear);
		GameRegistry.addRecipe(new ItemStack(ElectroItems.DRONE_UPGRADES.getItem(), 1, 2), "RDR", "EGE", "CPC", 'E', Block.oreEmerald, 'D', Block.oreDiamond, 'P', Item.enderPearl, 'R', Block.oreRedstone, 'G', Item.ghastTear, 'C', Block.oreCoal);
		GameRegistry.addRecipe(new ItemStack(ElectroItems.DRONE_UPGRADES.getItem(), 1, 3), "EPE", "PGP", "EPE", 'E', Item.emerald, 'P', Item.enderPearl, 'G', Item.ghastTear);
		GameRegistry.addRecipe(new ItemStack(ElectroItems.DRONE_UPGRADES.getItem(), 1, 4), "RPR", "GEG", "BPB", 'E', Item.emerald, 'B', Item.bone, 'P', Item.enderPearl, 'R', Item.rottenFlesh, 'G', Item.ghastTear);
		GameRegistry.addRecipe(new ItemStack(ElectroItems.DRONE.getItem(), 1), "E E", "CIC", "PPP", 'E', ElectroItems.ELECTRO_DUST.getItem(), 'C', ElectroBlocks.COMPUTER.getBlock(), 'I', Item.ingotIron, 'P', Item.enderPearl);
	}

	public boolean isShiftHeld() {
		return electroCraftSided.isShiftHeld();
	}
	
	public boolean isRunning() {
		return FMLCommonHandler.instance().getMinecraftServerInstance() != null && FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning();
	}

	@Override
	public void registerRunnable(FutureTask<?> task) {
		synchronized(mainThreadLock) {
			if (FMLCommonHandler.instance().getMinecraftServerInstance() != null && FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning()) {
				mainThreadFunctions.add(task);
			} else {
				task.cancel(true);
			}
		}
	}

	public List<FutureTask<?>> getAndClearTasks() {
		synchronized(mainThreadLock) {
			List<FutureTask<?>> tasks = new ArrayList<FutureTask<?>>(mainThreadFunctions);
			mainThreadFunctions.clear();
			return tasks;
		}
	}

	public List<ICard> getUpgradeCards() {
		return this.droneUpgradeCards;
	}

	public List<IDroneTool> getDroneTools() {
		return this.droneTools;
	}

	@Override
	public void registerAddon(Object mod) {
		this.addons.add(FMLCommonHandler.instance().findContainerFor(mod));
	}

	@Override
	public void regsiterCard(ICard card) {
		this.droneUpgradeCards.add(card);
	}

	@Override
	public void registerDroneTool(IDroneTool tool) {
		this.droneTools.add(tool);
	}

	public Logger getLogger() {
		return ecLogger;
	}

	final static class ElectroCraftLogHandler extends Handler {

		PrintStream stdout = new PrintStream(new FileOutputStream(FileDescriptor.out));
		PrintStream stderr = new PrintStream(new FileOutputStream(FileDescriptor.err));

		@Override
		public void close() throws SecurityException {
			stdout.close();
			stderr.close();
		}

		@Override
		public void flush() { }

		@Override
		public void publish(LogRecord record) {
			if (getFormatter() == null) {
				setFormatter(new SimpleFormatter());
			}

			try {
				String message = getFormatter().format(record);
				if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
					stderr.write(message.getBytes());                       
				}
				else {
					stdout.write(message.getBytes());
				}
			} catch (Exception exception) {
				reportError(null, exception, ErrorManager.FORMAT_FAILURE);
				return;
			}


		}
	}

	final static class ElectroCraftLogFormatter extends Formatter {

		static final String LINE_SEPARATOR = System.getProperty("line.separator");
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		@Override
		public String format(LogRecord record) {
			StringBuilder msg = new StringBuilder();
			msg.append(this.dateFormat.format(Long.valueOf(record.getMillis())));
			msg.append(" [ElectroCraft]");
			Level lvl = record.getLevel();

			if (lvl == Level.FINEST)
			{
				msg.append(" [FINEST] ");
			}
			else if (lvl == Level.FINER)
			{
				msg.append(" [FINER] ");
			}
			else if (lvl == Level.FINE)
			{
				msg.append(" [FINE] ");
			}
			else if (lvl == Level.INFO)
			{
				msg.append(" [INFO] ");
			}
			else if (lvl == Level.WARNING)
			{
				msg.append(" [WARNING] ");
			}
			else if (lvl == Level.SEVERE)
			{
				msg.append(" [SEVERE] ");
			}
			else if (lvl == Level.SEVERE)
			{
				msg.append(" [" + lvl.getLocalizedName() + "] ");
			}

			if (record.getLoggerName() != null && record.getLoggerName() != ecLoggerName)
			{
				msg.append("["+record.getLoggerName()+"] ");
			}

			msg.append(record.getMessage());
			msg.append(LINE_SEPARATOR);
			Throwable thr = record.getThrown();

			if (thr != null)
			{
				StringWriter thrDump = new StringWriter();
				thr.printStackTrace(new PrintWriter(thrDump));
				msg.append(thrDump.toString());
			}
			return msg.toString();
		}
	}
}
