package info.cerios.electrocraft.blocks.render;

import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.blocks.ElectroWire;
import info.cerios.electrocraft.core.blocks.IBlockRenderer;
import net.minecraft.src.Block;
import net.minecraft.src.BlockRedstoneWire;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Tessellator;

public class ElectroWireRenderer implements IBlockRenderer {

	@Override
	public boolean render(Block block, int x, int y, int z) {
		RenderBlocks renderer = ModLoader.getMinecraftInstance().renderGlobal.globalRenderBlocks;
		Tessellator var5 = Tessellator.instance;
        int var7 = block.getBlockTextureFromSideAndMetadata(1, renderer.blockAccess.getBlockMetadata(x, y, z));

        if (renderer.overrideBlockTexture >= 0)
        {
            var7 = renderer.overrideBlockTexture;
        }

        var5.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z));

        // The color tint
        var5.setColorOpaque_F(1.0f, 1.0F, 1.0F);
        int var13 = (var7 & 15) << 4;
        int var14 = var7 & 240;
        double var15 = (double)((float)var13 / 256.0F);
        double var17 = (double)(((float)var13 + 15.99F) / 256.0F);
        double var19 = (double)((float)var14 / 256.0F);
        double var21 = (double)(((float)var14 + 15.99F) / 256.0F);
        // Checks for adjoining connectors
        boolean var29 = ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x - 1, y, z, 1) || !renderer.blockAccess.isBlockNormalCube(x - 1, y, z) && ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x - 1, y - 1, z, -1);
        boolean var30 = ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x + 1, y, z, 3) || !renderer.blockAccess.isBlockNormalCube(x + 1, y, z) && ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x + 1, y - 1, z, -1);
        boolean var31 = ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x, y, z - 1, 2) || !renderer.blockAccess.isBlockNormalCube(x, y, z - 1) && ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x, y - 1, z - 1, -1);
        boolean var32 = ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x, y, z + 1, 0) || !renderer.blockAccess.isBlockNormalCube(x, y, z + 1) && ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x, y - 1, z + 1, -1);

        if (!renderer.blockAccess.isBlockNormalCube(x, y + 1, z))
        {
            if (renderer.blockAccess.isBlockNormalCube(x - 1, y, z) && ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x - 1, y + 1, z, -1))
            {
                var29 = true;
            }

            if (renderer.blockAccess.isBlockNormalCube(x + 1, y, z) && ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x + 1, y + 1, z, -1))
            {
                var30 = true;
            }

            if (renderer.blockAccess.isBlockNormalCube(x, y, z - 1) && ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x, y + 1, z - 1, -1))
            {
                var31 = true;
            }

            if (renderer.blockAccess.isBlockNormalCube(x, y, z + 1) && ElectroWire.isPowerProviderOrWire(renderer.blockAccess, x, y + 1, z + 1, -1))
            {
                var32 = true;
            }
        }

        float var34 = (float)(x + 0);
        float var35 = (float)(x + 1);
        float var36 = (float)(z + 0);
        float var37 = (float)(z + 1);
        byte var38 = 0;

        if ((var29 || var30) && !var31 && !var32)
        {
            var38 = 1;
        }

        if ((var31 || var32) && !var30 && !var29)
        {
            var38 = 2;
        }

        if (var38 != 0)
        {
            var15 = (double)((float)(var13 + 16) / 256.0F);
            var17 = (double)(((float)(var13 + 16) + 15.99F) / 256.0F);
            var19 = (double)((float)var14 / 256.0F);
            var21 = (double)(((float)var14 + 15.99F) / 256.0F);
        }

        if (var38 == 0)
        {
            if (!var29)
            {
                var34 += 0.3125F;
            }

            if (!var29)
            {
                var15 += 0.01953125D;
            }

            if (!var30)
            {
                var35 -= 0.3125F;
            }

            if (!var30)
            {
                var17 -= 0.01953125D;
            }

            if (!var31)
            {
                var36 += 0.3125F;
            }

            if (!var31)
            {
                var19 += 0.01953125D;
            }

            if (!var32)
            {
                var37 -= 0.3125F;
            }

            if (!var32)
            {
                var21 -= 0.01953125D;
            }

            var5.addVertexWithUV((double)var35, (double)y + 0.015625D, (double)var37, var17, var21);
            var5.addVertexWithUV((double)var35, (double)y + 0.015625D, (double)var36, var17, var19);
            var5.addVertexWithUV((double)var34, (double)y + 0.015625D, (double)var36, var15, var19);
            var5.addVertexWithUV((double)var34, (double)y + 0.015625D, (double)var37, var15, var21);
        }
        else if (var38 == 1)
        {
            var5.addVertexWithUV((double)var35, (double)y + 0.015625D, (double)var37, var17, var21);
            var5.addVertexWithUV((double)var35, (double)y + 0.015625D, (double)var36, var17, var19);
            var5.addVertexWithUV((double)var34, (double)y + 0.015625D, (double)var36, var15, var19);
            var5.addVertexWithUV((double)var34, (double)y + 0.015625D, (double)var37, var15, var21);
        }
        else if (var38 == 2)
        {
            var5.addVertexWithUV((double)var35, (double)y + 0.015625D, (double)var37, var17, var21);
            var5.addVertexWithUV((double)var35, (double)y + 0.015625D, (double)var36, var15, var21);
            var5.addVertexWithUV((double)var34, (double)y + 0.015625D, (double)var36, var15, var19);
            var5.addVertexWithUV((double)var34, (double)y + 0.015625D, (double)var37, var17, var19);
        }

        if (!renderer.blockAccess.isBlockNormalCube(x, y + 1, z))
        {
            var15 = (double)((float)(var13 + 16) / 256.0F);
            var17 = (double)(((float)(var13 + 16) + 15.99F) / 256.0F);
            var19 = (double)((float)var14 / 256.0F);
            var21 = (double)(((float)var14 + 15.99F) / 256.0F);

            if (renderer.blockAccess.isBlockNormalCube(x - 1, y, z) && renderer.blockAccess.getBlockId(x - 1, y + 1, z) == ElectroBlocks.ELECTRO_WIRE.getBlock().blockID)
            {
                var5.addVertexWithUV((double)x + 0.015625D, (double)((float)(y + 1) + 0.021875F), (double)(z + 1), var17, var19);
                var5.addVertexWithUV((double)x + 0.015625D, (double)(y + 0), (double)(z + 1), var15, var19);
                var5.addVertexWithUV((double)x + 0.015625D, (double)(y + 0), (double)(z + 0), var15, var21);
                var5.addVertexWithUV((double)x + 0.015625D, (double)((float)(y + 1) + 0.021875F), (double)(z + 0), var17, var21);
            }

            if (renderer.blockAccess.isBlockNormalCube(x + 1, y, z) && renderer.blockAccess.getBlockId(x + 1, y + 1, z) == ElectroBlocks.ELECTRO_WIRE.getBlock().blockID)
            {
                var5.addVertexWithUV((double)(x + 1) - 0.015625D, (double)(y + 0), (double)(z + 1), var15, var21);
                var5.addVertexWithUV((double)(x + 1) - 0.015625D, (double)((float)(y + 1) + 0.021875F), (double)(z + 1), var17, var21);
                var5.addVertexWithUV((double)(x + 1) - 0.015625D, (double)((float)(y + 1) + 0.021875F), (double)(z + 0), var17, var19);
                var5.addVertexWithUV((double)(x + 1) - 0.015625D, (double)(y + 0), (double)(z + 0), var15, var19);
            }

            if (renderer.blockAccess.isBlockNormalCube(x, y, z - 1) && renderer.blockAccess.getBlockId(x, y + 1, z - 1) == ElectroBlocks.ELECTRO_WIRE.getBlock().blockID)
            {
                var5.addVertexWithUV((double)(x + 1), (double)(y + 0), (double)z + 0.015625D, var15, var21);
                var5.addVertexWithUV((double)(x + 1), (double)((float)(y + 1) + 0.021875F), (double)z + 0.015625D, var17, var21);
                var5.addVertexWithUV((double)(x + 0), (double)((float)(y + 1) + 0.021875F), (double)z + 0.015625D, var17, var19);
                var5.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)z + 0.015625D, var15, var19);
            }

            if (renderer.blockAccess.isBlockNormalCube(x, y, z + 1) && renderer.blockAccess.getBlockId(x, y + 1, z + 1) == ElectroBlocks.ELECTRO_WIRE.getBlock().blockID)
            {
                var5.addVertexWithUV((double)(x + 1), (double)((float)(y + 1) + 0.021875F), (double)(z + 1) - 0.015625D, var17, var19);
                var5.addVertexWithUV((double)(x + 1), (double)(y + 0), (double)(z + 1) - 0.015625D, var15, var19);
                var5.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)(z + 1) - 0.015625D, var15, var21);
                var5.addVertexWithUV((double)(x + 0), (double)((float)(y + 1) + 0.021875F), (double)(z + 1) - 0.015625D, var17, var21);
            }
        }

        return true;
	}
}
