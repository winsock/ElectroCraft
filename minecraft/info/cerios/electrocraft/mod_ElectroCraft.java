package info.cerios.electrocraft;

import java.util.Random;

import info.cerios.electrocraft.blocks.BlockHandler;
import info.cerios.electrocraft.blocks.ElectroBlocks;
import info.cerios.electrocraft.config.ConfigHandler;
import info.cerios.electrocraft.core.computer.ComputerHandler;
import info.cerios.electrocraft.items.ElectroItems;
import info.cerios.electrocraft.items.ItemHandler;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.World;
import net.minecraft.src.WorldGenMinable;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.oredict.OreDictionary;
import net.minecraft.src.forge.oredict.ShapedOreRecipe;
import net.minecraft.src.forge.oredict.ShapelessOreRecipe;

public class mod_ElectroCraft extends BaseMod {
	
	public static mod_ElectroCraft instance;
	private ComputerHandler computerHandler;
	
	public mod_ElectroCraft() {
		instance = this;
	}
	
	@Override
    public String getName() {
        return "ElectroCraft";
    }
	
	@Override
	public String getVersion() {
		return "ElectroCraft InfDev 1.0";
	}

	@Override
	public void load() {
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
		
		// Create the computer handler
		this.computerHandler = new ComputerHandler();
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
		for (ElectroBlocks electroBlock : ElectroBlocks.values()) {
			if (electroBlock.getBlock().blockID == block.blockID)
				return electroBlock.callRenderer(renderer, block, x, y, z);
		}
        return false;
    }

	private void registerBaseRecipes() {
		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ElectroItems.ELECTRO_DUST.getItem(), 4), Item.redstone, "magnetiteDust", "magnetiteDust", "magnetiteDust"));
	}

	public ComputerHandler getComputerHandler() {
		return computerHandler;
	}
}
