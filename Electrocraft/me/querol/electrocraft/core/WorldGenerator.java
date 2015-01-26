package me.querol.electrocraft.core;

import me.querol.electrocraft.core.blocks.ElectroBlocks;

import java.util.Random;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.isSurfaceWorld()) {
            for (int i = 0; i < 20; i++) { // Rarity
                // Magnetite
                int randPosX = chunkX * 16 + random.nextInt(16);
                int randPosY = random.nextInt(128); // Min Height
                int randPosZ = chunkZ * 16 + random.nextInt(16);
                new WorldGenMinable(ElectroBlocks.MAGNETITE_ORE.getBlock().getDefaultState(), 10/*
                                                                                       * Vein
                                                                                       * Size
                                                                                       */).generate(world, random, new BlockPos(randPosX, randPosY, randPosZ));
            }
        }
    }
}
