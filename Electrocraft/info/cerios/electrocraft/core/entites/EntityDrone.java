package info.cerios.electrocraft.core.entites;

import info.cerios.electrocraft.api.IComputerHost;
import info.cerios.electrocraft.api.drone.tools.IDroneTool;
import info.cerios.electrocraft.api.utils.Utils;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.drone.Drone;
import info.cerios.electrocraft.core.drone.InventoryDrone;
import info.cerios.electrocraft.core.drone.tools.AbstractTool;
import info.cerios.electrocraft.core.network.CustomPacket;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;

public class EntityDrone extends EntityLiving implements IComputerHost {

    private Drone drone;
    private String id = "";
    private InventoryDrone inventory;
    private boolean firstTick = true;
    private volatile int rotationTicks = 0;
    private ForgeDirection digDirection = ForgeDirection.UNKNOWN;
    private AbstractTool defaultTool = new AbstractTool();
    private volatile boolean clientFlying = false;

    public EntityDrone(World par1World) {
        super(par1World);
        //texture = "/info/cerios/electrocraft/gfx/Drone.png";
        this.height = 0.8F;
        inventory = new InventoryDrone(this);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        if (worldObj.isRemote)
            return;
        id = nbt.getString("cid");
        createDrone();
        inventory.readFromNBT(nbt);
        if (drone != null) {
            drone.setFlying(nbt.getBoolean("flying"));
            if (nbt.getBoolean("isOn")) {
                if (nbt.hasKey("programStorage"))
                    drone.setProgramStorage(nbt.getCompoundTag("programStorage"));
                drone.callLoad();
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        if (worldObj.isRemote)
            return;
        inventory.writeToNBT(nbt);
        nbt.setString("cid", id);
        if (drone != null) {
            nbt.setBoolean("flying", drone.getFlying());
            nbt.setBoolean("isOn", drone.isRunning());

            drone.callSave();
            if (drone.getProgramStorage() != null) {
                nbt.setTag("programStorage", drone.getProgramStorage());
            }
        }
    }

    public InventoryDrone getInventory() {
        return inventory;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (firstTick) {
            if (worldObj.isRemote) {
                CustomPacket packet = new CustomPacket();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                try {
                    dos.writeInt(getEntityId());
                    packet.id = 7;
                    packet.data = bos.toByteArray();
                    ElectroCraft.instance.getNetworkWrapper().sendToServer(packet);
                } catch (IOException e) {
                    ElectroCraft.instance.getLogger().fine("Error sending inventory update to entity!");
                }
            }
            firstTick = false;
        }
        if (worldObj.isRemote) {
            if (rotationTicks > 0) {
                rotationTicks--;
            }
            return;
        }
        if (drone != null && drone.isRunning()) {
            drone.tick();
        }
        if (rotationTicks > 0) {
            rotationTicks--;
            if (rotationTicks <= 0) {
                doToolAction();
            }
        }
    }

    @Override
    public void setDead() {
        this.dead = true;
        if (drone != null) {
            drone.postEvent("kill");
            drone.shutdown();
            if (!drone.getBaseDirectory().delete() && ConfigHandler.getCurrentConfig().get(Configuration.CATEGORY_GENERAL, "deleteFiles", true).getBoolean(true)) {
                try {
                    Utils.deleteRecursive(drone.getBaseDirectory());
                } catch (FileNotFoundException e) {
                    ElectroCraft.instance.getLogger().severe("Unable to delete dead drones files! Path: " + drone.getBaseDirectory().getAbsolutePath());
                }
            }
        }
    }

    public void setClientFlying(boolean fly) {
        this.clientFlying = fly;
    }

    public void rotate(final float yaw, final int ticks) {
        newPosRotationIncrements = ticks;
        newPosX = posX;
        newPosY = posY;
        newPosZ = posZ;
        newRotationYaw = rotationYawHead = yaw;
        newRotationPitch = 0f;
    }

    public boolean isStillMovingOrRotating() {
        return this.newPosRotationIncrements > 0 && drone.isRunning();
    }

    @Override
    protected void fall(float par1) {
        if (worldObj.isRemote) {
            if (!clientFlying) {
                super.fall(par1);
            }
        } else if (drone == null || !drone.getFlying()) {
            super.fall(par1);
        }
    }

    @Override
    public boolean isOnLadder() {
        return false;
    }

    @Override
    public void moveEntityWithHeading(float par1, float par2) {
        if (worldObj.isRemote) {
            if (!clientFlying) {
                super.moveEntityWithHeading(par1, par2);
                return;
            }
        } else if (drone == null || !drone.getFlying()) {
            super.moveEntityWithHeading(par1, par2);
            return;
        }
        // Flying code from EntityFlying
        if (this.isInWater()) {
            this.moveFlying(par1, par2, 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.800000011920929D;
            this.motionY *= 0.800000011920929D;
            this.motionZ *= 0.800000011920929D;
        } else if (this.handleLavaMovement()) {
            this.moveFlying(par1, par2, 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        } else {
            float var3 = 0.91F;

            if (this.onGround) {
                var3 = 0.54600006F;
                Block block = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));
                int id = Block.getIdFromBlock(block);

                if (id > 0) {
                    var3 = block.slipperiness * 0.91F;
                }
            }

            float var8 = 0.16277136F / (var3 * var3 * var3);
            this.moveFlying(par1, par2, this.onGround ? 0.1F * var8 : 0.02F);
            var3 = 0.91F;

            if (this.onGround) {
                var3 = 0.54600006F;
                Block block = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));
                int id = Block.getIdFromBlock(block);

                if (id > 0) {
                    var3 = block.slipperiness * 0.91F;
                }
            }

            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= var3;
            this.motionY *= var3;
            this.motionZ *= var3;
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d1 = this.posX - this.prevPosX;
        double d0 = this.posZ - this.prevPosZ;
        float f4 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

        if (f4 > 1.0F) {
            f4 = 1.0F;
        }

        this.limbSwingAmount += (f4 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    public Drone getDrone() {
        return drone;
    }

    public void doToolAction() {
        if (getHeldItem() == null)
            return;

        ForgeDirection dir = drone.getDirection(rotationYaw);
        if (digDirection != ForgeDirection.UNKNOWN) {
            dir = digDirection;
            digDirection = ForgeDirection.UNKNOWN;
        }

        int x = (int) (Math.floor(posX) + dir.offsetX);
        int y = (int) (Math.floor(posY) + dir.offsetY);
        int z = (int) (Math.floor(posZ) + dir.offsetZ);

        IDroneTool tool = defaultTool;

        for (IDroneTool t : ElectroCraft.instance.getDroneTools()) {
            if (t.isRightTool(getHeldItem())) {
                tool = t;
                break;
            }
        }

        if (tool.appliesToBlock(getHeldItem(), worldObj.getBlock(x, y, z), worldObj.getBlockMetadata(x, y, z))) {
            for (ItemStack item : tool.preformAction(getHeldItem(), this, worldObj, x, y, z)) {
                drone.addToInventory(inventory, 0, 36, item);
            }
            tool.damageItem(this, getHeldItem(), worldObj.getBlock(x, y, z), worldObj.getBlockMetadata(x, y, z));
            drone.postEvent("tool", true);
        } else {
            drone.postEvent("tool", false);
        }
    }

    public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        int metadata = world.getBlockMetadata(x, y, z);
        if (getHeldItem().getEnchantmentTagList() != null) {
            for (int i = 0; i < getHeldItem().getEnchantmentTagList().tagCount(); i++) {
                if (getHeldItem().getEnchantmentTagList().getCompoundTagAt(i).getShort("id") == Enchantment.fortune.effectId) {
                    return block.getDrops(world, x, y, z, metadata, getHeldItem().getEnchantmentTagList().getCompoundTagAt(i).getShort("lvl"));
                }
            }
        }
        return block.getDrops(world, x, y, z, metadata, 0);
    }

    @Override
    public ItemStack getHeldItem() {
        return inventory.tools[1];
    }

    public int getRotationTicks() {
        return rotationTicks;
    }

    public void setRotationTicks(int ticks) {
        this.rotationTicks = ticks;
        if (ticks == 0) {
            this.newPosRotationIncrements = 0;
        }
    }

    public void setDigDirection(ForgeDirection dir) {
        this.digDirection = dir;
    }

    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            ElectroCraft.instance.setComputerForPlayer(player, this);
            if (player.isSneaking()) {
                player.openGui(ElectroCraft.instance, this.getEntityId(), worldObj, (int) posX, (int) posY, (int) posZ);
            } else if ((player.ridingEntity != null) || (player.getHeldItem() != null && Item.getIdFromItem(player.getHeldItem().getItem()) == Item.getIdFromItem((Item) Item.itemRegistry.getObject("saddle")))) {
                player.mountEntity(this);
            } else {
                if (drone == null) {
                    createDrone();
                }
                GuiPacket guiPacket = new GuiPacket();
                guiPacket.setCloseWindow(false);
                guiPacket.setGui(Gui.COMPUTER_SCREEN);
                ElectroCraft.instance.getNetworkWrapper().sendTo(guiPacket, (EntityPlayerMP) player);
                if (!drone.isRunning()) {
                    drone.start();
                }
                drone.addClient(player);
            }
            return true;
        } else {
            super.interact(player);
        }
        return true;
    }

    public void createDrone() {
        if (!worldObj.isRemote) {
            File worldFolder = worldObj.getSaveHandler().getWorldDirectory();
            String dir;
            if (id == null || id.isEmpty()) {
                id = String.valueOf(Calendar.getInstance().getTime().getTime());
                dir = worldFolder.getAbsolutePath() + File.separator + "electrocraft" + File.separator + "computers" + File.separator + id;
                File file = new File(dir);
                while (file.exists()) {
                    id = String.valueOf(Calendar.getInstance().getTime().getTime());
                    dir = worldFolder.getAbsolutePath() + File.separator + "electrocraft" + File.separator + "computers" + File.separator + id;
                    file = new File(dir);
                }
            }
            dir = worldFolder.getAbsolutePath() + File.separator + "electrocraft" + File.separator + "computers" + File.separator + id;
            drone = new Drone(new ArrayList<EntityPlayer>(), dir, 320, 240, 15, 50);
            drone.setDrone(this);
        }
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public Computer getComputer() {
        return drone;
    }

    @Override
    public void removeActivePlayer(EntityPlayer player) {
        if (drone != null) {
            drone.removeClient(player);
        }
        ElectroCraft.instance.setComputerForPlayer(player, null);
    }
}
