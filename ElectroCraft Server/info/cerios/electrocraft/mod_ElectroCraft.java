package info.cerios.electrocraft;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.server.FMLServerHandler;

import info.cerios.electrocraft.computer.ComputerHandler;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.IElectroCraftMod;
import info.cerios.electrocraft.core.IMinecraftMethods;
import info.cerios.electrocraft.core.blocks.BlockHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.IComputerHandler;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.computer.XECInterface;
import info.cerios.electrocraft.core.computer.XECInterface.AssembledData;
import info.cerios.electrocraft.core.items.ElectroItems;
import info.cerios.electrocraft.core.items.ItemHandler;
import info.cerios.electrocraft.core.network.ComputerInputPacket;
import info.cerios.electrocraft.core.network.ElectroPacket;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.core.network.ServerPortPacket;
import info.cerios.electrocraft.core.network.ShiftPacket;
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
	private XECInterface xECComputer;

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
		
		// Create our xEC computer
		xECComputer = new XECInterface();
		xECComputer.createCPU();
		String testAssembly = "; A Simple program that changes the display buffer randomly\n" +
				".code\n" +
				"rand:\n" +
				"mov eax, 0 ; move the lower limit of the random function to eax\n" +
				"randi eax, 2000000 ; call the random function with the range of 0-255 and store the result in eax\n" +
				"mov ecx, [0x8000] ; Get the value at 0x8000(The start of the VGA IO Memory) The value there is the size of the display buffer\n" +
				"add ecx, 0x8004 ; Add the lower limit of the diplay buffer\n" +
				"mov ebx, 0x8004 ; move the lower limit of the display buffer\n" +
				"randi ebx, ecx ; get a random address between 0x8004 and the value of the size of the display buffer\n" +
				"mov [ebx], eax ; set the pixel at the random address\n" +
				"call sleep ; Sleep the program to prevent eating up CPU cycles\n" +
				"jmp rand ; And jump back to the begining and repeat\n" +
				"sleep:\n" +
				"mov cx, 100 ; Loop 100 times\n" +
				"sleeploop:\n"+
				"nop ; No instruction\n" +
				"loop sleeploop ; Loop while cx > 0\n" +
				"ret ; return to the caller\n";
		AssembledData data = xECComputer.assemble(testAssembly);
		long baseAddress = xECComputer.loadIntoMemory(data.data, data.length);
		xECComputer.start(baseAddress);

		// Create the computer handler
		this.computerHandler = new ComputerHandler();
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
				
			}
		} catch (IOException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to parse packet send on our channel!");
		}
	}

	
	public IComputerHandler getComputerHandler() {
		return computerHandler;
	}
	
	public XECInterface getComputer() {
		return xECComputer;
	}
	
	@Override
	public boolean isShiftHeld() {
		return shiftStatus;
	}
	
	@Override
    public void onClientLogin(EntityPlayer player) {
		FMLCommonHandler.instance().activateChannel(player, "electrocraft");
		ServerPortPacket portPacket = new ServerPortPacket();
		try {
			((EntityPlayerMP)player).playerNetServerHandler.sendPacket(portPacket.getMCPacket());
		} catch (IOException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to send computer servers port!");
		}
	}

	@Override
	public IMinecraftMethods getSidedMethods() {
		return minecraftMethods;
	}
}
