package info.cerios.electrocraft.core;

import info.cerios.electrocraft.core.blocks.ElectroBlocks;

import java.util.Random;

import net.minecraft.src.IChunkProvider;
import net.minecraft.src.World;
import net.minecraft.src.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;

public class WorldGenerator implements IWorldGenerator {
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (!world.provider.isHellWorld) {
			for (int i = 0; i < 20; i++) { // Rarity
				// Magnetite
				int randPosX = chunkX * 16 + random.nextInt(16);
				int randPosY = random.nextInt(128); // Min Height
				int randPosZ = chunkZ * 16 + random.nextInt(16);
				new WorldGenMinable(
						ElectroBlocks.MAGNETITE_ORE.getBlock().blockID, 10/*
																		 * Vein
																		 * Size
																		 */)
						.generate(world, random, randPosX, randPosY, randPosZ);
			}
		}
	}
}
