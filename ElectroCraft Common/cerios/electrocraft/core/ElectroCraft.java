package info.cerios.electrocraft.core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import info.cerios.electrocraft.computer.ComputerHandler;
import info.cerios.electrocraft.core.blocks.BlockHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.IComputerHandler;
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
@NetworkMod(clientSideRequired = true, serverSideRequired = false, connectionHandler = ConnectionHandler.class)
public class ElectroCraft {

	@Mod.Instance
    public static ElectroCraft instance;
    @SidedProxy(clientSide = "info.cerios.electrocraft.ElectroCraftClient", serverSide = "info.cerios.electrocraft.ElectroCraftServer")
    public static IElectroCraftSided electroCraftSided;
    
    private ComputerHandler computerHandler;
    private XECInterface xECComputer;

    public ElectroCraft() {
        instance = this;
    }

    @Mod.Init
    public void init(FMLInitializationEvent event) {
        // Create and load the config
        ConfigHandler.loadOrCreateConfigFile("default.cfg");

        // Initialize any sided methods
        electroCraftSided.init();
        
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

        // Register for on tick events
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
        	TickRegistry.registerScheduledTickHandler(electroCraftSided.getTickHandler(), Side.CLIENT);
        } else {
        	if (FMLCommonHandler.instance().getSide() == Side.BUKKIT) {
            	TickRegistry.registerScheduledTickHandler(electroCraftSided.getTickHandler(), Side.BUKKIT);
        	} else {
            	TickRegistry.registerScheduledTickHandler(electroCraftSided.getTickHandler(), Side.SERVER);
        	}
        }
        
        // Register our mod channel
        NetworkRegistry.instance().registerChannel(electroCraftSided.getPacketHandler(), "electrocraft");
                
        String tripleFaultASM = "hlt";

        // Create our xEC computer
        xECComputer = new XECInterface();
        xECComputer.createCPU();

        AssembledData data;
        String startupAsm = Utils.loadUncompiledAssembly("." + File.separator + "electrocraft" + File.separator + "startup.xsm");
        if (startupAsm.isEmpty()) {
            data = xECComputer.assemble(tripleFaultASM);
        } else {
            data = xECComputer.assemble(startupAsm);
        }
        long baseAddress = xECComputer.loadIntoMemory(data.data, data.length, data.codeStart);
        xECComputer.start(baseAddress);

        // Create the computer handler
        this.computerHandler = new ComputerHandler();
    }

    private void registerBaseRecipes() {
        CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ElectroItems.ELECTRO_DUST.getItem(), 4), Item.redstone, "magnetiteDust", "magnetiteDust", "magnetiteDust"));
        ModLoader.addRecipe(new ItemStack(ElectroBlocks.COMPUTER.getBlock()), "III", "ICI", "IEI", 'E', ElectroItems.ELECTRO_DUST.getItem(), 'I', Item.ingotIron, 'C', Item.compass);
        ModLoader.addRecipe(new ItemStack(ElectroBlocks.REDSTONE_ADAPTER.getBlock()), "III", "ICI", "IRI", 'R', Item.redstone, 'I', Item.ingotIron, 'C', Block.thinGlass);
        ModLoader.addRecipe(new ItemStack(ElectroBlocks.RIBBON_CABLE.getBlock(), 16), "WWW", "III", "WWW", 'W', Block.cloth, 'I', Item.ingotIron);
    }

    public IComputerHandler getComputerHandler() {
        return computerHandler;
    }

    public XECInterface getComputer() {
        return xECComputer;
    }
    
    public boolean isShiftHeld() {
        return electroCraftSided.isShiftHeld();
    }
}
