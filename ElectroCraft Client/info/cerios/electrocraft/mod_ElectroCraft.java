package info.cerios.electrocraft;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

import info.cerios.electrocraft.blocks.render.BlockRenderers;
import info.cerios.electrocraft.computer.ComputerClient;
import info.cerios.electrocraft.computer.ComputerHandler;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.IElectroCraftMod;
import info.cerios.electrocraft.core.IMinecraftMethods;
import info.cerios.electrocraft.core.blocks.BlockHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.IComputerHandler;
import info.cerios.electrocraft.core.network.ElectroPacket;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.core.network.ShiftPacket;
import info.cerios.electrocraft.gui.GuiComputerScreen;
import info.cerios.electrocraft.gui.GuiNetworkAddressScreen;
import info.cerios.electrocraft.items.ElectroItems;
import info.cerios.electrocraft.items.ItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.World;
import net.minecraft.src.WorldGenMinable;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.NetworkMod;
import net.minecraft.src.forge.oredict.OreDictionary;
import net.minecraft.src.forge.oredict.ShapedOreRecipe;
import net.minecraft.src.forge.oredict.ShapelessOreRecipe;

public class mod_ElectroCraft extends NetworkMod implements IElectroCraftMod {
	
	public static mod_ElectroCraft instance;
	private ComputerHandler computerHandler;
	private IMinecraftMethods minecraftMethods;
	private boolean lastShiftState = false;
	private ComputerClient computerClient;

	public mod_ElectroCraft() {
		instance = this;
	}
	
	@Override
    public String getName() {
        return "ElectroCraft";
    }
	
	@Override
	public String getVersion() {
		return "ElectroCraft In-Dev 0.1";
		
	}
	
	@Override
	public void serverConnect(NetClientHandler handler) {
		try {
			try {
				computerClient = new ComputerClient(1337, (SocketAddress)ModLoader.getPrivateValue(NetworkManager.class, (NetworkManager)ModLoader.getPrivateValue(NetClientHandler.class, handler, "netManager"), "remoteSocketAddress"));
				new Thread(computerClient).start();
			} catch(RuntimeException e) {
				computerClient = new ComputerClient(1337, (SocketAddress)ModLoader.getPrivateValue(NetworkManager.class, (NetworkManager)ModLoader.getPrivateValue(NetClientHandler.class, handler, "g"), "i"));
				new Thread(computerClient).start();
			}
		} catch (UnknownHostException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft ComputerClient: Unable to find remote host!");
		} catch (IOException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft ComputerClient: Unable to connect to remote host!");
		}
    }

	@Override
	public void load() {
		// Create my minecraft proxy class
		minecraftMethods = new MinecraftMethods();
		
		// Create and load the config
		ConfigHandler.loadOrCreateConfigFile("default.cfg");
		
		// Create the item handler
		ItemHandler itemHandler = new ItemHandler();
		
		// Create and register the ore handler
		BlockHandler oreHandler = new BlockHandler(itemHandler);
		OreDictionary.registerOreHandler(oreHandler);
		
		// Register our items and blocks
		//*NOTE* THIS POSISTION IS IMPORTANT, IT HAS TO GO AFTER ORE HANDLER REGISTRATION *NOTE*
		oreHandler.registerBlocks();
		itemHandler.registerItems();
		
		// Register the recipes
		registerBaseRecipes();
		
		// Preload the Graphics
		MinecraftForgeClient.preloadTexture("/info/cerios/electrocraft/gfx/blocks.png");
		MinecraftForgeClient.preloadTexture("/info/cerios/electrocraft/gfx/items.png");
		
		// Register for on tick events
		ModLoader.setInGameHook(this, true, false);
		
		ModLoader.registerPacketChannel(this, "electrocraft");
				
		// Create the computer handler
		this.computerHandler = new ComputerHandler();
	}
	
	@Override
	public boolean onTickInGame(float time, Minecraft minecraftInstance) {
		computerHandler.update();
		
		try {
			if (FMLClientHandler.instance().getClient().isMultiplayerWorld()) {
				// Shift Packet
				if (lastShiftState != (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
					lastShiftState = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
					ShiftPacket shiftPacket = new ShiftPacket();
					shiftPacket.setShiftState(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
					FMLClientHandler.instance().sendPacket(shiftPacket.getMCPacket());
				}
			}
		} catch (IOException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to send packet!");
		}
		return true;
	}
	
	@Override
	public void receiveCustomPacket(Packet250CustomPayload packet) {
		ElectroPacket ecPacket;
		try {
			ecPacket = ElectroPacket.readMCPacket(packet);
			
			if (ecPacket.getType() == Type.GUI) {
				GuiPacket guiPacket = (GuiPacket) ecPacket;
				if (guiPacket.closeWindow())
					minecraftMethods.closeGui();
				else if (guiPacket.getGui() == Gui.COMPUTER_SCREEN) {
					FMLClientHandler.instance().getClient().displayGuiScreen(new GuiComputerScreen());
				}
			} else if (ecPacket.getType() == Type.ADDRESS) {
				NetworkAddressPacket networkPacket = (NetworkAddressPacket)ecPacket;
				FMLClientHandler.instance().getClient().displayGuiScreen(new GuiNetworkAddressScreen(networkPacket));
			}
		} catch (IOException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to read packet sent on our channel!");
		}
	}
	
	@Override
	public void generateSurface(World world, Random random, int chunkX, int chunkZ) {
		for(int i = 0; i < 20; i++) { // Rarity
			// Copper
            int randPosX = chunkX + random.nextInt(16);
            int randPosY = random.nextInt(128); // Min Height
            int randPosZ = chunkZ + random.nextInt(16);
            new WorldGenMinable(ElectroBlocks.COPPER_ORE.getBlock().blockID, 10/* Vein Size */).generate(world, random, randPosX, randPosY, randPosZ);
            // Magnetite
            randPosX = chunkX + random.nextInt(16);
            randPosY = random.nextInt(128); // Min Height
            randPosZ = chunkZ + random.nextInt(16);
            new WorldGenMinable(ElectroBlocks.MAGNETITE_ORE.getBlock().blockID, 10/* Vein Size */).generate(world, random, randPosX, randPosY, randPosZ);
        }
    }
	
	@Override
	public boolean renderWorldBlock(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, int modelID) {
		for (BlockRenderers renderBlock : BlockRenderers.values()) {
			if (renderBlock.getBlock().getBlock().blockID == block.blockID)
				return renderBlock.getRenderer().render(block, x, y, z);
		}
        return false;
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
	
	@Override
	public boolean isShiftHeld() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}

	@Override
	public IMinecraftMethods getSidedMethods() {
		return minecraftMethods;
	}
	
	public ComputerClient getComputerClient() {
		return computerClient;
	}
}
