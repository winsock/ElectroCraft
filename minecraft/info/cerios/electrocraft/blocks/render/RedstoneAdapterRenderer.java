package info.cerios.electrocraft.blocks.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderBlocks;

public class RedstoneAdapterRenderer implements IBlockRenderer {

	@Override
	public boolean render(RenderBlocks renderer, Block block, int x, int y, int z) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, ModLoader.getMinecraftInstance().renderEngine.getTexture(block.getTextureFile()));

		return true;
	}
}
