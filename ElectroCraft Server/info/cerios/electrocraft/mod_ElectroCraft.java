package info.cerios.electrocraft;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.server.FMLServerHandler;

import info.cerios.electrocraft.computer.ComputerHandler;
import info.cerios.electrocraft.computer.ComputerServer;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.IElectroCraftMod;
import info.cerios.electrocraft.core.IMinecraftMethods;
import info.cerios.electrocraft.core.blocks.BlockHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.IComputerHandler;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.jpc.emulator.peripheral.Keyboard;
import info.cerios.electrocraft.core.network.ComputerInputPacket;
import info.cerios.electrocraft.core.network.ElectroPacket;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.core.network.ShiftPacket;
import info.cerios.electrocraft.items.ElectroItems;
import info.cerios.electrocraft.items.ItemHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.WorldGenMinable;
import net.minecraft.src.forge.NetworkMod;
import net.minecraft.src.forge.oredict.OreDictionary;
import net.minecraft.src.forge.oredict.ShapedOreRecipe;
import net.minecraft.src.forge.oredict.ShapelessOreRecipe;

public class mod_ElectroCraft extends NetworkMod implements IElectroCraftMod {
	
	public static mod_ElectroCraft instance;
	private ComputerHandler computerHandler;
	private IMinecraftMethods minecraftMethods;
	private boolean shiftStatus = false;
	private ComputerServer computerServer;
	
	public mod_ElectroCraft() {
		instance = this;
	}
	
	@Override
	public boolean clientSideRequired() {
		return true;
	}
	
	@Override
	public boolean serverSideRequired() {
		return true;
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
	public void load() {
		// Create the minecraft methods proxy
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
		
		// Register for on tick events
		ModLoader.setInGameHook(this, true, false);
		
		// Register packer channel
		ModLoader.registerPacketChannel(this, "electrocraft");
		
		// Create the computer handler
		this.computerHandler = new ComputerHandler();
		
		// Start the computer server
		try {
			this.computerServer = new ComputerServer(1337);
			new Thread(computerServer).start();
		} catch (IOException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to start computer server on port: 1337!");
		}
	}
	
	@Override
	public boolean onTickInGame(MinecraftServer minecraftInstance) {
		computerHandler.update();
		return true;
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

	private void registerBaseRecipes() {
		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ElectroItems.ELECTRO_DUST.getItem(), 4), Item.redstone, "magnetiteDust", "magnetiteDust", "magnetiteDust"));
		ModLoader.addRecipe(new ItemStack(ElectroBlocks.COMPUTER.getBlock()), "III", "ICI", "IEI", 'E', ElectroItems.ELECTRO_DUST.getItem(), 'I', Item.ingotIron, 'C', Item.compass);
		ModLoader.addRecipe(new ItemStack(ElectroBlocks.REDSTONE_ADAPTER.getBlock()), "III", "ICI", "IRI", 'R', Item.redstone, 'I', Item.ingotIron, 'C', Block.thinGlass);
		ModLoader.addRecipe(new ItemStack(ElectroBlocks.RIBBON_CABLE.getBlock(), 16), "WWW", "III", "WWW", 'W', Block.cloth, 'I', Item.ingotIron);
	}
	
	@Override
    public void onPacket250Received(EntityPlayer source, Packet250CustomPayload payload) {
		if (!payload.channel.equalsIgnoreCase("electrocraft"))
			return;
		try {
			ElectroPacket packet = ElectroPacket.readMCPacket(payload);
			if (packet.getType() == Type.SHIFT) {
				ShiftPacket shiftPacket = (ShiftPacket)packet;
				shiftStatus = shiftPacket.getShiftState();
			} else if (packet.getType() == Type.ADDRESS) {
				NetworkAddressPacket addressPacket = (NetworkAddressPacket)packet;
				World world = FMLServerHandler.instance().getServer().getWorldManager(addressPacket.getWorldId());
				TileEntity tileEntity = world.getBlockTileEntity(addressPacket.getX(), addressPacket.getY(), addressPacket.getZ());
				if(tileEntity instanceof NetworkBlock) {
					((NetworkBlock)tileEntity).setControlAddress(addressPacket.getControlAddress());
					((NetworkBlock)tileEntity).setDataAddress(addressPacket.getDataAddress());
				}
			} else if (packet.getType() == Type.INPUT) {
				ComputerInputPacket inputPacket = (ComputerInputPacket)packet;
				Keyboard keyboard = (Keyboard)computerServer.getClient((EntityPlayerMP) source).getComputer().getComputer().getComponent(Keyboard.class);
				keyboard.putMouseEvent(inputPacket.getDeltaX(), inputPacket.getDeltaY(), inputPacket.getWheelDelta(), inputPacket.getEventMouseButton());
				if (inputPacket.wasKeyDown()) {
					keyboard.keyPressed((byte) inputPacket.getEventKey());
				} else {
					keyboard.keyReleased((byte) inputPacket.getEventKey());
				}
			}
		} catch (IOException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to parse packet send on our channel!");
		}
	}

	
	public IComputerHandler getComputerHandler() {
		return computerHandler;
	}
	
	public ComputerServer getServer() {
		return computerServer;
	}

	@Override
	public boolean isShiftHeld() {
		return shiftStatus;
	}
	
	@Override
    public void onClientLogin(EntityPlayer player) {
		FMLCommonHandler.instance().activateChannel(player, "electrocraft");
	}

	@Override
	public IMinecraftMethods getSidedMethods() {
		return minecraftMethods;
	}
}
