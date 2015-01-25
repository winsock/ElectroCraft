package info.cerios.electrocraft.core.drone;

import com.mojang.authlib.GameProfile;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import info.cerios.electrocraft.api.drone.upgrade.ICard;
import info.cerios.electrocraft.api.utils.ObjectPair;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.drone.tools.SwordTool;
import info.cerios.electrocraft.core.entites.EntityDrone;
import info.cerios.electrocraft.core.network.CustomPacket;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Drone extends Computer {

    private EntityDrone drone;
    private EntityMoveHelper moveHelper;
    private FakePlayer fakePlayer;
    private boolean flying = false;
    private ObjectPair<ICard, ItemStack> leftCard;
    private ObjectPair<ICard, ItemStack> rightCard;

    public Drone(List<EntityPlayer> clients, String baseDirectory, int width, int height, int rows, int columns) {
        super(clients, baseDirectory, width, height, rows, columns);
    }

    @Override
    public void tick() {
        super.tick();
        if (leftCard != null) {
            leftCard.getValue1().passiveFunctionTick(leftCard.getValue2());
        }
        if (rightCard != null) {
            rightCard.getValue1().passiveFunctionTick(rightCard.getValue2());
        }
        if (fakePlayer != null) {
            fakePlayer.posX = drone.posX;
            fakePlayer.posY = drone.posY;
            fakePlayer.posZ = drone.posZ;
        }
    }

    public void setDrone(EntityDrone drone) {
        this.drone = drone;
        this.moveHelper = new EntityMoveHelper(drone);
        if (!drone.worldObj.isRemote) {
            fakePlayer = new FakePlayer(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(drone.worldObj.provider.getDimensionId()), new GameProfile(UUID.randomUUID(), "FakePlayerEC" + getBaseDirectory().getName()));
            fakePlayer.preventEntitySpawning = true;
        }
    }

    public EntityDrone getDrone() {
        return drone;
    }

    public EntityMoveHelper getMoveHelper() {
        return moveHelper;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        drone.setRotationTicks(0);
    }

    @Override
    public void loadLuaDefaults() {
        super.loadLuaDefaults();
        luaStateLock.lock();
        NamedJavaFunction[] droneAPI = new NamedJavaFunction[] { new NamedJavaFunction() {
            Drone drone;

            public NamedJavaFunction init(Drone drone) {
                this.drone = drone;
                return this;
            }

            @Override
            public int invoke(LuaState luaState) {
                Callable<Integer[]> callable;
                if (luaState.isNumber(-1)) {
                    final int intDir = luaState.checkInteger(-1);
                    callable = new Callable<Integer[]>() {
                        @Override
                        public Integer[] call() throws Exception {
                            EnumFacing dir = EnumFacing.getFront(intDir);
                            BlockPos moveTo = drone.getDrone().getPosition().offset(dir);
                            drone.getMoveHelper().setMoveTo(moveTo.getX(), moveTo.getY(), moveTo.getZ(), 5);
                            return new Integer[] { dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ() };
                        }
                    };
                } else {
                    callable = new Callable<Integer[]>() {
                        @Override
                        public Integer[] call() throws Exception {
                            EnumFacing dir = EnumFacing.fromAngle(drone.drone.rotationYaw);
                            BlockPos moveTo = drone.getDrone().getPosition().offset(dir);
                            drone.getMoveHelper().setMoveTo(moveTo.getX(), moveTo.getY(), moveTo.getZ(), 5);
                            return new Integer[] { dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ() };
                        }
                    };
                }
                FutureTask<Integer[]> task = new FutureTask<Integer[]>(callable);
                ElectroCraft.instance.registerRunnable(task);
                try {
                    Integer[] location = task.get();
                    while (drone.getMoveHelper().isUpdating()) {
                        try {
                            drone.getMoveHelper().onUpdateMoveHelper();
                            Thread.sleep(1);
                        } catch (InterruptedException ignored) {
                        }
                    }

                    callable = new Callable<Integer[]>() {
                        @Override
                        public Integer[] call() throws Exception {
                            int afterX = (int) (Math.floor(drone.getDrone().posX));
                            int afterY = (int) (Math.floor(drone.getDrone().posY));
                            int afterZ = (int) (Math.floor(drone.getDrone().posZ));
                            return new Integer[] { afterX, afterY, afterZ };
                        }
                    };
                    task = new FutureTask<Integer[]>(callable);
                    ElectroCraft.instance.registerRunnable(task);
                    Integer[] afterLocation = task.get();

                    if (location[0].equals(afterLocation[0]) && location[1].equals(afterLocation[1]) && location[2].equals(afterLocation[2])) {
                        luaState.pushBoolean(false);
                    } else {
                        luaState.pushBoolean(true);
                    }
                } catch (InterruptedException e) {
                    luaState.pushBoolean(false);
                } catch (ExecutionException e) {
                    luaState.pushBoolean(false);
                    e.printStackTrace();
                }
                return 1;
            }

            @Override
            public String getName() {
                return "move";
            }
        }.init(this), new NamedJavaFunction() {
            Drone drone;

            public NamedJavaFunction init(Drone drone) {
                this.drone = drone;
                return this;
            }

            @Override
            public int invoke(LuaState luaState) {
                Callable<Boolean> callable;
                if (luaState.isNumber(-1)) {
                    final int number = luaState.checkInteger(-1);
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            if (drone.drone.getRotationTicks() <= 0) {
                                drone.drone.setDigDirection(EnumFacing.getFront(number));
                                drone.drone.setRotationTicks(60);
                                CustomPacket packet = new CustomPacket();
                                packet.id = 5;
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                DataOutputStream dos = new DataOutputStream(bos);
                                try {
                                    dos.writeInt(drone.drone.getEntityId());
                                    dos.writeInt(drone.drone.getRotationTicks());
                                    packet.id = 5;
                                    packet.data = bos.toByteArray();
                                    ElectroCraft.instance.getNetworkWrapper().sendToAllAround(packet, new NetworkRegistry.TargetPoint(drone.drone.worldObj.provider.getDimensionId(), drone.drone.posX, drone.drone.posY, drone.drone.posZ, 20));
                                } catch (IOException e) {
                                    ElectroCraft.instance.getLogger().fine("Error sending tool use update to entity!");
                                }
                            }
                            return true;
                        }
                    };
                } else {
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            if (drone.drone.getRotationTicks() <= 0) {
                                drone.drone.setRotationTicks(60);
                                CustomPacket packet = new CustomPacket();
                                packet.id = 5;
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                DataOutputStream dos = new DataOutputStream(bos);
                                try {
                                    dos.writeInt(drone.drone.getEntityId());
                                    dos.writeInt(drone.drone.getRotationTicks());
                                    packet.id = 5;
                                    packet.data = bos.toByteArray();
                                    ElectroCraft.instance.getNetworkWrapper().sendToAllAround(packet, new NetworkRegistry.TargetPoint(drone.drone.worldObj.provider.getDimensionId(), drone.drone.posX, drone.drone.posY, drone.drone.posZ, 20));
                                } catch (IOException e) {
                                    ElectroCraft.instance.getLogger().fine("Error sending tool use update to entity!");
                                }
                            }
                            return true;
                        }
                    };
                }
                final FutureTask<Boolean> task = new FutureTask<Boolean>(callable);
                ElectroCraft.instance.registerRunnable(task);
                try {
                    task.get();
                } catch (InterruptedException ignored) {
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            public String getName() {
                return "useTool";
            }
        }.init(this), new NamedJavaFunction() {
            Drone drone;

            public NamedJavaFunction init(Drone drone) {
                this.drone = drone;
                return this;
            }

            @Override
            public int invoke(LuaState luaState) {
                final int slot1 = luaState.checkInteger(-1), slot2 = luaState.checkInteger(-2);
                Callable<ItemStack> getStack = new Callable<ItemStack>() {
                    @Override
                    public ItemStack call() throws Exception {
                        return drone.drone.getInventory()[slot1];
                    }
                };
                final FutureTask<ItemStack> task = new FutureTask<ItemStack>(getStack);
                ElectroCraft.instance.registerRunnable(task);
                try {
                    if (task.get() == null) {
                        Callable<Boolean> setStack = new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                drone.drone.getInventory()[slot1] = task.get();
                                drone.drone.getInventory()[slot2] = null;
                                return true;
                            }
                        };
                        final FutureTask<Boolean> setTask = new FutureTask<Boolean>(setStack);
                        ElectroCraft.instance.registerRunnable(setTask);
                        luaState.pushBoolean(setTask.get());
                    } else {
                        luaState.pushBoolean(false);
                    }
                } catch (InterruptedException e) {
                    luaState.pushBoolean(false);
                } catch (ExecutionException e) {
                    luaState.pushBoolean(false);
                    e.printStackTrace();
                }
                return 1;
            }

            @Override
            public String getName() {
                return "moveStack";
            }
        }.init(this), new NamedJavaFunction() {
            Drone drone;

            public NamedJavaFunction init(Drone drone) {
                this.drone = drone;
                return this;
            }

            @Override
            public int invoke(LuaState luaState) {
                final int amount = luaState.checkInteger(-1), slot2 = luaState.checkInteger(-2), slot1 = luaState.checkInteger(-3);
                Callable<Boolean> callable = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        ItemStack stack = drone.drone.getInventory()[slot2];
                        if (stack == null)
                            return false;
                        int remainder;
                        if (stack.stackSize <= amount) {
                            remainder = stack.stackSize - amount;
                            stack.stackSize = amount;
                        } else {
                            stack.stackSize = 0;
                            remainder = amount;
                        }
                        if (drone.drone.getInventory()[-2] == null) {
                            drone.drone.getInventory()[slot2] = stack;
                            if (remainder > 0) {
                                ItemStack newStack = stack.copy();
                                newStack.stackSize = remainder;
                                drone.drone.getInventory()[slot1] = newStack;
                            } else {
                                drone.drone.getInventory()[slot1] = null;
                            }
                            return true;
                        } else
                            return false;
                    }
                };
                final FutureTask<Boolean> task = new FutureTask<Boolean>(callable);
                ElectroCraft.instance.registerRunnable(task);
                try {
                    luaState.pushBoolean(task.get());
                } catch (InterruptedException e) {
                    luaState.pushBoolean(false);
                } catch (ExecutionException e) {
                    luaState.pushBoolean(false);
                    e.printStackTrace();
                }
                return 1;
            }

            @Override
            public String getName() {
                return "moveItems";
            }
        }.init(this), new NamedJavaFunction() {
            Drone drone;

            public NamedJavaFunction init(Drone drone) {
                this.drone = drone;
                return this;
            }

            @Override
            public int invoke(LuaState luaState) {
                Callable<Boolean> callable;

                if (luaState.getTop() == 3) {
                    final int int1 = luaState.checkInteger(-1), int2 = luaState.checkInteger(-2), int3 = luaState.checkInteger(-3);
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            EnumFacing dir = EnumFacing.getFront(int1);

                            int amount;
                            ItemStack stack;
                            amount = int2;
                            stack = drone.drone.getInventory()[int3];
                            ItemStack remainder = stack.copy();
                            remainder.stackSize = stack.stackSize - amount;
                            if (remainder.stackSize <= 0) {
                                amount -= Math.abs(remainder.stackSize);
                                remainder = null;
                            }
                            drone.drone.getInventory()[int3] = remainder;
                            stack.stackSize = amount;

                            if (amount <= 0)
                                return false;

                            BlockPos blockPos = drone.drone.getPosition().offset(dir);
                            if (drone.drone.worldObj.getTileEntity(blockPos) != null && drone.drone.worldObj.getTileEntity(blockPos) instanceof IInventory) {
                                IInventory inv = (IInventory) drone.drone.worldObj.getTileEntity(blockPos);
                                if (inv instanceof ISidedInventory) {
                                    ISidedInventory sidedInv = (ISidedInventory) inv;
                                    if (!addToInventory(sidedInv, sidedInv.getSlotsForFace(dir.getOpposite()), stack)) {
                                        drone.drone.entityDropItem(stack, 0f);
                                    }
                                } else {
                                    if (!addToInventory(inv, 0, inv.getSizeInventory(), stack)) {
                                        drone.drone.entityDropItem(stack, 0f);
                                    }
                                }
                            } else {
                                drone.drone.entityDropItem(stack, 0f);
                            }
                            return true;
                        }
                    };
                } else if (luaState.getTop() == 2) {
                    final int int1 = luaState.checkInteger(-1), int2 = luaState.checkInteger(-2);
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            EnumFacing dir = EnumFacing.getFront(int1);

                            int amount = 0;
                            ItemStack stack;
                            stack = drone.drone.getInventory()[int2];
                            if (stack != null) {
                                amount = stack.stackSize;
                            }
                            drone.drone.getInventory()[int2] = null;

                            if (stack == null || amount <= 0)
                                return false;

                            BlockPos blockPos = drone.drone.getPosition().offset(dir);
                            if (drone.drone.worldObj.getTileEntity(blockPos) != null && drone.drone.worldObj.getTileEntity(blockPos) instanceof IInventory) {
                                IInventory inv = (IInventory) drone.drone.worldObj.getTileEntity(blockPos);
                                if (inv instanceof ISidedInventory) {
                                    ISidedInventory sidedInv = (ISidedInventory) inv;
                                    if (!addToInventory(sidedInv, sidedInv.getSlotsForFace(dir.getOpposite()), stack)) {
                                        drone.drone.entityDropItem(stack, 0f);
                                    }
                                } else {
                                    if (!addToInventory(inv, 0, inv.getSizeInventory(), stack)) {
                                        drone.drone.entityDropItem(stack, 0f);
                                    }
                                }
                            } else {
                                drone.drone.entityDropItem(stack, 0f);
                            }
                            return true;
                        }
                    };
                } else {
                    final int int1 = luaState.checkInteger(-1);
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            EnumFacing dir = EnumFacing.fromAngle(drone.drone.rotationYaw);

                            int amount = 0;
                            ItemStack stack;
                            stack = drone.drone.getInventory()[int1];
                            if (stack != null) {
                                amount = stack.stackSize;
                            }
                            drone.drone.getInventory()[int1] = null;

                            if (stack == null || amount <= 0)
                                return false;

                            BlockPos blockPos = drone.drone.getPosition().offset(dir);
                            if (drone.drone.worldObj.getTileEntity(blockPos) != null && drone.drone.worldObj.getTileEntity(blockPos) instanceof IInventory) {
                                IInventory inv = (IInventory) drone.drone.worldObj.getTileEntity(blockPos);
                                if (inv instanceof ISidedInventory) {
                                    ISidedInventory sidedInv = (ISidedInventory) inv;
                                    if (!addToInventory(sidedInv, sidedInv.getSlotsForFace(dir.getOpposite()), stack)) {
                                        drone.drone.entityDropItem(stack, 0f);
                                    }
                                } else {
                                    if (!addToInventory(inv, 0, inv.getSizeInventory(), stack)) {
                                        drone.drone.entityDropItem(stack, 0f);
                                    }
                                }
                            } else {
                                drone.drone.entityDropItem(stack, 0f);
                            }
                            return true;
                        }
                    };
                }
                final FutureTask<Boolean> task = new FutureTask<Boolean>(callable);
                ElectroCraft.instance.registerRunnable(task);
                try {
                    luaState.pushBoolean(task.get());
                } catch (InterruptedException e) {
                    luaState.pushBoolean(false);
                } catch (ExecutionException e) {
                    luaState.pushBoolean(false);
                    e.printStackTrace();
                }

                return 1;
            }

            @Override
            public String getName() {
                return "drop";
            }
        }.init(this), new NamedJavaFunction() {
            Drone drone;

            public NamedJavaFunction init(Drone drone) {
                this.drone = drone;
                return this;
            }

            @Override
            public int invoke(LuaState luaState) {
                final EnumFacing dir = EnumFacing.getFront(luaState.checkInteger(-1));
                final int slot = luaState.checkInteger(-2);
                Callable<Boolean> callable = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        BlockPos blockPos = drone.drone.getPosition().offset(dir);
                        Block block = drone.getDrone().worldObj.getBlockState(blockPos).getBlock();
                        if (drone.getDrone().getInventory()[slot].getItem() instanceof ItemBlock) {
                            if ((block == null || block.isReplaceable(drone.getDrone().worldObj, blockPos)) && drone.getDrone().getInventory()[slot] != null) {
                                ItemBlock blockToPlace = (ItemBlock) drone.getDrone().getInventory()[slot].getItem();
                                blockToPlace.placeBlockAt(drone.getDrone().getInventory()[slot], fakePlayer, drone.getDrone().worldObj, blockPos, dir.getOpposite(), 0, 0, 0, blockToPlace.getBlock().getDefaultState());
                            }
                        } else {
                            drone.getDrone().getInventory()[slot].onItemUse(fakePlayer, drone.getDrone().worldObj, blockPos, dir.getOpposite(), 0, 0, 0);
                        }
                        return true;
                    }
                };

                FutureTask<Boolean> task = new FutureTask<Boolean>(callable);
                ElectroCraft.instance.registerRunnable(task);
                try {
                    luaState.pushBoolean(task.get());
                } catch (InterruptedException e) {
                    luaState.pushBoolean(false);
                } catch (ExecutionException e) {
                    luaState.pushBoolean(false);
                    e.printStackTrace();
                }
                return 1;
            }

            @Override
            public String getName() {
                return "place";
            }
        }.init(this), new NamedJavaFunction() {
            Drone drone;

            public NamedJavaFunction init(Drone drone) {
                this.drone = drone;
                return this;
            }

            @Override
            public int invoke(LuaState luaState) {
                final EnumFacing dir = EnumFacing.getFront(luaState.checkInteger(-1));
                Callable<Boolean> callable;
                if (luaState.getTop() == 1) {
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            BlockPos blockPos = drone.drone.getPosition().offset(dir);
                            return fakePlayer.theItemInWorldManager.activateBlockOrUseItem(fakePlayer, drone.getDrone().worldObj, null, blockPos, dir.getOpposite(), 0, 0, 0);
                        }
                    };
                } else {
                    final int slot = luaState.checkInteger(-2);
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            BlockPos blockPos = drone.drone.getPosition().offset(dir);
                            return fakePlayer.theItemInWorldManager.activateBlockOrUseItem(fakePlayer, drone.getDrone().worldObj, drone.getDrone().getInventory()[slot], blockPos, dir.getOpposite(), 0, 0, 0);
                        }
                    };
                }

                FutureTask<Boolean> task = new FutureTask<Boolean>(callable);
                ElectroCraft.instance.registerRunnable(task);
                try {
                    luaState.pushBoolean(task.get());
                } catch (InterruptedException e) {
                    luaState.pushBoolean(false);
                } catch (ExecutionException e) {
                    luaState.pushBoolean(false);
                    e.printStackTrace();
                }
                return 1;
            }

            @Override
            public String getName() {
                return "use";
            }
        }.init(this), };
        this.luaState.register("drone", droneAPI);
        luaState.pop(1);
        if (luaState != null && luaState.isOpen() && leftCard != null) {
            luaState.register(leftCard.getValue1().getName(leftCard.getValue2()), leftCard.getValue1().getFunctions(leftCard.getValue2(), this));
            luaState.pop(1);
        }
        if (luaState != null && luaState.isOpen() && rightCard != null) {
            luaState.register(rightCard.getValue1().getName(rightCard.getValue2()), rightCard.getValue1().getFunctions(rightCard.getValue2(), this));
            luaState.pop(1);
        }
        luaStateLock.unlock();
    }

    public void setLeftCard(ICard card, ItemStack stack) {
        luaStateLock.lock();
        if (luaState != null && luaState.isOpen() && card == null && this.leftCard != null && (this.rightCard == null || (rightCard.getValue2() != leftCard.getValue2()))) {
            luaState.pushNil();
            luaState.setGlobal(leftCard.getValue1().getName(leftCard.getValue2()));
        }
        if ((leftCard == null || (card != leftCard.getValue1() && luaState != null && luaState.isOpen())) && card != null && luaState != null) {
            luaState.register(card.getName(stack), card.getFunctions(stack, this));
            luaState.setGlobal(card.getName(stack));
        }
        luaStateLock.unlock();
        this.leftCard = (card == null ? null : new ObjectPair<ICard, ItemStack>(card, stack));
    }

    public void setRightCard(ICard card, ItemStack stack) {
        luaStateLock.lock();
        if (luaState != null && luaState.isOpen() && card == null && this.rightCard != null && (this.leftCard == null || (rightCard.getValue2() != leftCard.getValue2()))) {
            luaState.pushNil();
            luaState.setGlobal(rightCard.getValue1().getName(rightCard.getValue2()));
        }
        if ((rightCard == null || (card != rightCard.getValue1() && luaState != null && luaState.isOpen())) && card != null && luaState != null) {
            luaState.register(card.getName(stack), card.getFunctions(stack, this));
            luaState.setGlobal(card.getName(stack));
        }
        luaStateLock.unlock();
        this.rightCard = (card == null ? null : new ObjectPair<ICard, ItemStack>(card, stack));
    }

    public boolean getFlying() {
        return flying;
    }

    public void setFlying(boolean fly) {
        if (fly != flying) {
            CustomPacket packet = new CustomPacket();
            packet.id = 6;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                dos.writeInt(drone.getEntityId());
                dos.writeBoolean(fly);
                packet.id = 6;
                packet.data = bos.toByteArray();
                ElectroCraft.instance.getNetworkWrapper().sendToAllAround(packet, new NetworkRegistry.TargetPoint(drone.worldObj.provider.getDimensionId(), drone.posX, drone.posY, drone.posZ, 20));
            } catch (IOException e) {
                ElectroCraft.instance.getLogger().fine("Error sending tool use update to entity!");
            }
        }
        this.flying = fly;
    }

    public boolean addToInventory(IInventory inventory, int startSlot, int endSlot, ItemStack item) {
        if (startSlot > endSlot)
            return false;
        int [] slots = new int[endSlot - startSlot];
        int count = 0;
        for (int i = startSlot; i < endSlot; i++) {
            slots[count] = i;
            count++;
        }
        return addToInventory(inventory, slots, item);
    }

    public boolean addToInventory(IInventory inventory, int[] slots, ItemStack item) {
        for (int i : slots) {
            if (inventory.getStackInSlot(i) != null && Item.getIdFromItem(inventory.getStackInSlot(i).getItem()) == Item.getIdFromItem(item.getItem())) {
                if (inventory.getStackInSlot(i).stackSize + item.stackSize > item.getMaxStackSize()) {
                    int totalAmount = inventory.getStackInSlot(i).stackSize + item.stackSize;
                    item.stackSize = item.getMaxStackSize();
                    totalAmount -= item.stackSize;
                    inventory.setInventorySlotContents(i, item);
                    item.stackSize = totalAmount;
                } else {
                    item.stackSize += inventory.getStackInSlot(i).stackSize;
                    inventory.setInventorySlotContents(i, item);
                    return true;
                }
            } else if (inventory.getStackInSlot(i) == null) {
                inventory.setInventorySlotContents(i, item);
                return true;
            }
        }
        return false;
    }

    public int getDir(float rotation) {
        return MathHelper.floor_double(rotation * 4.0F / 360.0F + 0.5D) & 3;
    }

    public EnumFacing getDirection(float rotation) {
        return getDirection(getDir(rotation));
    }

    public EnumFacing getDirection(int direction) {
        switch (direction) {
            case 0:
                return EnumFacing.SOUTH;
            case 1:
                return EnumFacing.WEST;
            case 2:
                return EnumFacing.NORTH;
            case 3:
                return EnumFacing.EAST;
            default:
                return null;
        }
    }

    public float getRotation(EnumFacing direction) {
        switch (direction) {
            case NORTH:
                return 180f;
            case WEST:
                return 90f;
            case SOUTH:
                return 0f;
            case EAST:
                return 270f;
            default:
                return 0f;
        }
    }

    // Register tools
    static {
        ElectroCraft.instance.registerDroneTool(new SwordTool());
    }
}
