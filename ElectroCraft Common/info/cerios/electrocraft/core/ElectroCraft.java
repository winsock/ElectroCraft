package info.cerios.electrocraft.core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import info.cerios.electrocraft.core.blocks.BlockHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.XECInterface;
import info.cerios.electrocraft.core.computer.XECInterface.AssembledData;
import info.cerios.electrocraft.core.items.ElectroItems;
import info.cerios.electrocraft.core.items.ItemHandler;
import info.cerios.electrocraft.core.network.*;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.utils.Utils;
import net.minecraft.src.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

@Mod(modid = "electrocraft", name = "ElectroCraft", version = "In-Dev 0.2")
@NetworkMod(channels = {"electrocraft"}, clientSideRequired = true, serverSideRequired = false, packetHandler=UniversialPacketHandler.class, connectionHandler = ConnectionHandler.class)
public class ElectroCraft {

	@Mod.Instance
    public static ElectroCraft instance;
    @SidedProxy(clientSide = "info.cerios.electrocraft.ElectroCraftClient", serverSide = "info.cerios.electrocraft.ElectroCraftSidedServer")
    public static IElectroCraftSided electroCraftSided;
    
    private XECInterface xecInterface;
    private ComputerServer server;

    public ElectroCraft() {
        instance = this;
    }

    @Mod.Init
    public void init(FMLInitializationEvent event) {
        // Create and load the config
        ConfigHandler.loadOrCreateConfigFile("default.cfg");

        // Initialize any sided methods
        electroCraftSided.init();
        
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
        
        // Create the computer handler
        this.xecInterface = new XECInterface();
    }
    
    @Mod.ServerStarting
    public void onServerStarting(FMLServerStartingEvent event) {
    	try {
    		// First try the specified port
    		server = new ComputerServer(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("serverport", "general", 1337).getInt(1337));
			new Thread(server).start();
		} catch (IOException e) {
			try {
				// Otherwise try to get a free port
				server = new ComputerServer(0);
				new Thread(server).start();
			} catch (IOException e1) {
				FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft Server: Tried to start server on port: " + ConfigHandler.getCurrentConfig().getOrCreateIntProperty("serverport", "general", 1337).getInt(1337) + " and a random free port and failed!");
			}
		}
    }
    
    public ComputerServer getServer() {
    	return server;
    }

    private void registerBaseRecipes() {
    	GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ElectroItems.ELECTRO_DUST.getItem(), 4), Item.redstone, "magnetiteDust", "magnetiteDust", "magnetiteDust"));
    	GameRegistry.addRecipe(new ItemStack(ElectroBlocks.COMPUTER.getBlock()), "III", "ICI", "IEI", 'E', ElectroItems.ELECTRO_DUST.getItem(), 'I', Item.ingotIron, 'C', Item.compass);
    	GameRegistry.addRecipe(new ItemStack(ElectroBlocks.REDSTONE_ADAPTER.getBlock()), "III", "ICI", "IRI", 'R', Item.redstone, 'I', Item.ingotIron, 'C', Block.thinGlass);
    	GameRegistry.addRecipe(new ItemStack(ElectroBlocks.RIBBON_CABLE.getBlock(), 16), "WWW", "III", "WWW", 'W', Block.cloth, 'I', Item.ingotIron);
    }

    public XECInterface getComputerInterface() {
        return xecInterface;
    }
    
    public boolean isShiftHeld() {
        return electroCraftSided.isShiftHeld();
    }
}
