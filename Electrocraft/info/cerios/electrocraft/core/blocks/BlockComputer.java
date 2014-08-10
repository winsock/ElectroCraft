package info.cerios.electrocraft.core.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import info.cerios.electrocraft.api.utils.Utils;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;

import java.io.FileNotFoundException;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockComputer extends BlockNetwork {

    public BlockComputer(int id) {
        super(Material.wood);
        Block.blockRegistry.addObject(id, "computer", this);
    }
    private IIcon computerFront, computerSides;

    @Override
    public IIcon getIcon(int side, int metadata) {
        if (metadata > 5) {
            metadata = 5;
        }
        if (side > 5) {
            side = 5;
        }

        if (side == 0)
            return computerSides;

        switch (metadata) {
            case 4:
                switch (side) {
                    case 2:
                        return computerFront;
                    case 3:
                    case 4:
                    case 5:
                    default:
                        return computerSides;
                }
            default:
            case 2:
                switch (side) {
                    case 3:
                        return computerFront;
                    case 2:
                    case 4:
                    case 5:
                    default:
                        return computerSides;
                }
            case 3:
                switch (side) {
                    case 4:
                        return computerFront;
                    case 2:
                    case 3:
                    case 5:
                    default:
                        return computerSides;
                }
            case 1:
                switch (side) {
                    case 5:
                        return computerFront;
                    case 2:
                    case 3:
                    case 4:
                    default:
                        return computerSides;
                }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);

        if (world.getTileEntity(x, y, z) instanceof TileEntityComputer) {
            TileEntityComputer computerTileEntity = (TileEntityComputer) world.getTileEntity(x, y, z);
            int direction = MathHelper.floor_double(entityLiving.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
            if (direction == 0) {
                direction = 4;
            }
            world.setBlockMetadataWithNotify(x, y, z, direction, 3);
            computerTileEntity.setDirection(ForgeDirection.values()[direction]);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block par5, int par6) {
        if (world.isRemote)
            return;
        if (world.getTileEntity(x, y, z) instanceof TileEntityComputer) {
            TileEntityComputer computerTileEntity = (TileEntityComputer) world.getTileEntity(x, y, z);
            if (computerTileEntity != null) {
                if (computerTileEntity.getComputer() != null) {
                    if (computerTileEntity.getComputer().isRunning()) {
                        computerTileEntity.stopComputer();
                    }
                    if (!computerTileEntity.getComputer().getBaseDirectory().delete() && ConfigHandler.getCurrentConfig().get(Configuration.CATEGORY_GENERAL, "deleteFiles", true).getBoolean(true)) {
                        try {
                            Utils.deleteRecursive(computerTileEntity.getComputer().getBaseDirectory());
                        } catch (FileNotFoundException e) {
                            ElectroCraft.instance.getLogger().severe("Unable to delete removed computers files! Path: " + computerTileEntity.getComputer().getBaseDirectory().getAbsolutePath());
                        }
                    }
                }
            }
        }
        super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9))
            return true;

        if (player instanceof EntityPlayerMP) {
            if (world.getTileEntity(x, y, z) instanceof TileEntityComputer) {
                TileEntityComputer computerTileEntity = (TileEntityComputer) world.getTileEntity(x, y, z);
                if (computerTileEntity != null) {
                    if (computerTileEntity.getComputer() == null) {
                        computerTileEntity.createComputer();
                    }
                    if (!computerTileEntity.getComputer().isRunning()) {
                        computerTileEntity.startComputer();
                    }
                    ElectroCraft.instance.setComputerForPlayer(player, computerTileEntity);
                    GuiPacket guiPacket = new GuiPacket();
                    guiPacket.setCloseWindow(false);
                    guiPacket.setGui(Gui.COMPUTER_SCREEN);
                    ElectroCraft.instance.getNetworkWrapper().sendTo(guiPacket, (EntityPlayerMP) player);
                    computerTileEntity.addActivePlayer(player);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_) {
        return new TileEntityComputer();
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        computerFront = iconRegister.registerIcon("electrocraft:computerFront");
        computerSides = iconRegister.registerIcon("electrocraft:computerSide");
    }
}
