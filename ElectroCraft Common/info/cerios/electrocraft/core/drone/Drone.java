package info.cerios.electrocraft.core.drone;

import info.cerios.electrocraft.api.drone.upgrade.ICard;
import info.cerios.electrocraft.api.utils.ObjectPair;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.drone.tools.SwordTool;
import info.cerios.electrocraft.core.entites.EntityDrone;
import info.cerios.electrocraft.core.network.CustomPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemInWorldManager;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class Drone extends Computer {

	private EntityDrone drone;
	private EntityPlayerMP fakePlayer;
	private boolean flying = false;
	private ObjectPair<ICard, ItemStack> leftCard;
	private ObjectPair<ICard, ItemStack> rightCard;

	public Drone(List<EntityPlayer> clients, String baseDirectory, int width,
			int height, int rows, int columns) {
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
		if (!drone.worldObj.isRemote) {
			ItemInWorldManager itemManager = new ItemInWorldManager(
					drone.worldObj);
			fakePlayer = new EntityPlayerMP(FMLCommonHandler.instance()
					.getMinecraftServerInstance(), drone.worldObj,
					"FakePlayerEC" + getBaseDirectory().getName(), itemManager);
			fakePlayer.preventEntitySpawning = true;
		}
	}

	public EntityDrone getDrone() {
		return drone;
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
		NamedJavaFunction[] droneAPI = new NamedJavaFunction[] {
				new NamedJavaFunction() {
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
									ForgeDirection dir = ForgeDirection
											.getOrientation(intDir);
									drone.drone.move(dir.offsetX, dir.offsetY,
											dir.offsetZ, 5);
									return new Integer[] { dir.offsetX,
											dir.offsetY, dir.offsetZ };
								}
							};
						} else {
							callable = new Callable<Integer[]>() {
								@Override
								public Integer[] call() throws Exception {
									ForgeDirection dir = getDirection(drone.drone.rotationYaw);
									drone.drone.move(dir.offsetX, dir.offsetY,
											dir.offsetZ, 5);
									return new Integer[] { dir.offsetX,
											dir.offsetY, dir.offsetZ };
								}
							};
						}
						FutureTask<Integer[]> task = new FutureTask<Integer[]>(
								callable);
						ElectroCraft.instance.registerRunnable(task);
						try {
							Integer[] location = task.get();
							while (drone.drone.isStillMovingOrRotating()) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
								}
							}

							callable = new Callable<Integer[]>() {
								@Override
								public Integer[] call() throws Exception {
									int afterX = (int) (Math.floor(drone
											.getDrone().posX));
									int afterY = (int) (Math.floor(drone
											.getDrone().posY));
									int afterZ = (int) (Math.floor(drone
											.getDrone().posZ));
									return new Integer[] { afterX, afterY,
											afterZ };
								}
							};
							task = new FutureTask<Integer[]>(callable);
							ElectroCraft.instance.registerRunnable(task);
							Integer[] afterLocation = task.get();

							if (location[0] == afterLocation[0]
									&& location[1] == afterLocation[1]
									&& location[2] == afterLocation[2]) {
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
										drone.drone
												.setDigDirection(ForgeDirection
														.getOrientation(number));
										drone.drone.setRotationTicks(60);
										CustomPacket packet = new CustomPacket();
										packet.id = 5;
										ByteArrayOutputStream bos = new ByteArrayOutputStream();
										DataOutputStream dos = new DataOutputStream(
												bos);
										try {
											dos.writeInt(drone.drone.entityId);
											dos.writeInt(drone.drone
													.getRotationTicks());
											packet.id = 5;
											packet.data = bos.toByteArray();
											PacketDispatcher
													.sendPacketToAllAround(
															drone.drone.posX,
															drone.drone.posY,
															drone.drone.posZ,
															20,
															drone.drone.worldObj.provider.dimensionId,
															packet.getMCPacket());
										} catch (IOException e) {
											ElectroCraft.instance
													.getLogger()
													.fine("Error sending tool use update to entity!");
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
										DataOutputStream dos = new DataOutputStream(
												bos);
										try {
											dos.writeInt(drone.drone.entityId);
											dos.writeInt(drone.drone
													.getRotationTicks());
											packet.id = 5;
											packet.data = bos.toByteArray();
											PacketDispatcher
													.sendPacketToAllAround(
															drone.drone.posX,
															drone.drone.posY,
															drone.drone.posZ,
															20,
															drone.drone.worldObj.provider.dimensionId,
															packet.getMCPacket());
										} catch (IOException e) {
											ElectroCraft.instance
													.getLogger()
													.fine("Error sending tool use update to entity!");
										}
									}
									return true;
								}
							};
						}
						final FutureTask<Boolean> task = new FutureTask<Boolean>(
								callable);
						ElectroCraft.instance.registerRunnable(task);
						try {
							task.get();
						} catch (InterruptedException e) {
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
						final int slot1 = luaState.checkInteger(-1), slot2 = luaState
								.checkInteger(-2);
						Callable<ItemStack> getStack = new Callable<ItemStack>() {
							@Override
							public ItemStack call() throws Exception {
								return drone.drone.getInventory()
										.getStackInSlot(slot1);
							}
						};
						final FutureTask<ItemStack> task = new FutureTask<ItemStack>(
								getStack);
						ElectroCraft.instance.registerRunnable(task);
						try {
							if (task.get() == null) {
								Callable<Boolean> setStack = new Callable<Boolean>() {
									@Override
									public Boolean call() throws Exception {
										drone.drone.getInventory()
												.setInventorySlotContents(
														slot1, task.get());
										drone.drone.getInventory()
												.setInventorySlotContents(
														slot2, null);
										return true;
									}
								};
								final FutureTask<Boolean> setTask = new FutureTask<Boolean>(
										setStack);
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
						final int amount = luaState.checkInteger(-1), slot2 = luaState
								.checkInteger(-2), slot1 = luaState
								.checkInteger(-3);
						Callable<Boolean> callable = new Callable<Boolean>() {
							@Override
							public Boolean call() throws Exception {
								ItemStack stack = drone.drone.getInventory()
										.getStackInSlot(slot2);
								if (stack == null)
									return false;
								int remainder = 0;
								if (stack.stackSize <= amount) {
									remainder = stack.stackSize - amount;
									stack.stackSize = amount;
								} else {
									stack.stackSize = 0;
									remainder = amount;
								}
								if (drone.drone.getInventory().getStackInSlot(
										-2) == null) {
									drone.drone.getInventory()
											.setInventorySlotContents(slot2,
													stack);
									if (remainder > 0) {
										ItemStack newStack = stack.copy();
										newStack.stackSize = remainder;
										drone.drone.getInventory()
												.setInventorySlotContents(
														slot1, newStack);
									} else {
										drone.drone.getInventory()
												.setInventorySlotContents(
														slot1, null);
									}
									return true;
								} else
									return false;
							}
						};
						final FutureTask<Boolean> task = new FutureTask<Boolean>(
								callable);
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
							final int int1 = luaState.checkInteger(-1), int2 = luaState
									.checkInteger(-2), int3 = luaState
									.checkInteger(-3);
							callable = new Callable<Boolean>() {
								@Override
								public Boolean call() throws Exception {
									ForgeDirection dir;
									dir = ForgeDirection.getOrientation(int1);

									int amount = 0;
									ItemStack stack = null;
									amount = int2;
									stack = drone.drone.getInventory()
											.getStackInSlot(int3);
									ItemStack remainder = stack.copy();
									remainder.stackSize = stack.stackSize
											- amount;
									if (remainder.stackSize <= 0) {
										amount -= Math.abs(remainder.stackSize);
										remainder = null;
									}
									drone.drone.getInventory()
											.setInventorySlotContents(int3,
													remainder);
									stack.stackSize = amount;

									if (stack == null || amount <= 0)
										return false;

									int x = (int) (Math
											.floor(drone.getDrone().posX) + dir.offsetX);
									int y = (int) (Math
											.floor(drone.getDrone().posY) + dir.offsetY);
									int z = (int) (Math
											.floor(drone.getDrone().posZ) + dir.offsetZ);
									if (drone.drone.worldObj
											.getBlockTileEntity(x, y, z) != null
											&& drone.drone.worldObj
													.getBlockTileEntity(x, y, z) instanceof IInventory) {
										IInventory inv = (IInventory) drone.drone.worldObj
												.getBlockTileEntity(x, y, z);
										if (inv instanceof ISidedInventory) {
											ISidedInventory sidedInv = (ISidedInventory) inv;
											if (!addToInventory(
													sidedInv,
													sidedInv.getStartInventorySide(dir
															.getOpposite()),
													sidedInv.getSizeInventorySide(dir
															.getOpposite()),
													stack)) {
												drone.drone.entityDropItem(
														stack, 0f);
											}
										} else {
											if (!addToInventory(inv, 0,
													inv.getSizeInventory(),
													stack)) {
												drone.drone.entityDropItem(
														stack, 0f);
											}
										}
									} else {
										drone.drone.entityDropItem(stack, 0f);
									}
									return true;
								}
							};
						} else if (luaState.getTop() == 2) {
							final int int1 = luaState.checkInteger(-1), int2 = luaState
									.checkInteger(-2);
							callable = new Callable<Boolean>() {
								@Override
								public Boolean call() throws Exception {
									ForgeDirection dir;
									dir = ForgeDirection.getOrientation(int1);

									int amount = 0;
									ItemStack stack = null;
									stack = drone.drone.getInventory()
											.getStackInSlot(int2);
									if (stack != null) {
										amount = stack.stackSize;
									}
									drone.drone.getInventory()
											.setInventorySlotContents(int2,
													null);

									if (stack == null || amount <= 0)
										return false;

									int x = (int) (Math
											.floor(drone.getDrone().posX) + dir.offsetX);
									int y = (int) (Math
											.floor(drone.getDrone().posY) + dir.offsetY);
									int z = (int) (Math
											.floor(drone.getDrone().posZ) + dir.offsetZ);
									if (drone.drone.worldObj
											.getBlockTileEntity(x, y, z) != null
											&& drone.drone.worldObj
													.getBlockTileEntity(x, y, z) instanceof IInventory) {
										IInventory inv = (IInventory) drone.drone.worldObj
												.getBlockTileEntity(x, y, z);
										if (inv instanceof ISidedInventory) {
											ISidedInventory sidedInv = (ISidedInventory) inv;
											if (!addToInventory(
													sidedInv,
													sidedInv.getStartInventorySide(dir
															.getOpposite()),
													sidedInv.getSizeInventorySide(dir
															.getOpposite()),
													stack)) {
												drone.drone.entityDropItem(
														stack, 0f);
											}
										} else {
											if (!addToInventory(inv, 0,
													inv.getSizeInventory(),
													stack)) {
												drone.drone.entityDropItem(
														stack, 0f);
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
									ForgeDirection dir;
									dir = getDirection(drone.drone.rotationYaw);

									int amount = 0;
									ItemStack stack = null;
									stack = drone.drone.getInventory()
											.getStackInSlot(int1);
									if (stack != null) {
										amount = stack.stackSize;
									}
									drone.drone.getInventory()
											.setInventorySlotContents(int1,
													null);

									if (stack == null || amount <= 0)
										return false;

									int x = (int) (Math
											.floor(drone.getDrone().posX) + dir.offsetX);
									int y = (int) (Math
											.floor(drone.getDrone().posY) + dir.offsetY);
									int z = (int) (Math
											.floor(drone.getDrone().posZ) + dir.offsetZ);
									if (drone.drone.worldObj
											.getBlockTileEntity(x, y, z) != null
											&& drone.drone.worldObj
													.getBlockTileEntity(x, y, z) instanceof IInventory) {
										IInventory inv = (IInventory) drone.drone.worldObj
												.getBlockTileEntity(x, y, z);
										if (inv instanceof ISidedInventory) {
											ISidedInventory sidedInv = (ISidedInventory) inv;
											if (!addToInventory(
													sidedInv,
													sidedInv.getStartInventorySide(dir
															.getOpposite()),
													sidedInv.getSizeInventorySide(dir
															.getOpposite()),
													stack)) {
												drone.drone.entityDropItem(
														stack, 0f);
											}
										} else {
											if (!addToInventory(inv, 0,
													inv.getSizeInventory(),
													stack)) {
												drone.drone.entityDropItem(
														stack, 0f);
											}
										}
									} else {
										drone.drone.entityDropItem(stack, 0f);
									}
									return true;
								}
							};
						}
						final FutureTask<Boolean> task = new FutureTask<Boolean>(
								callable);
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
						final ForgeDirection dir = ForgeDirection
								.getOrientation(luaState.checkInteger(-1));
						final int slot = luaState.checkInteger(-2);
						Callable<Boolean> callable = new Callable<Boolean>() {
							@Override
							public Boolean call() throws Exception {
								int x = (int) (Math
										.floor(drone.getDrone().posX) + dir.offsetX);
								int y = (int) (Math
										.floor(drone.getDrone().posY) + dir.offsetY);
								int z = (int) (Math
										.floor(drone.getDrone().posZ) + dir.offsetZ);
								Block block = Block.blocksList[drone.getDrone().worldObj
										.getBlockId(x, y, z)];
								if (drone.getDrone().getInventory()
										.getStackInSlot(slot).getItem() instanceof ItemBlock) {
									if ((block == null || block
											.isBlockReplaceable(
													drone.getDrone().worldObj,
													x, y, z))
											&& drone.getDrone().getInventory()
													.getStackInSlot(slot) != null) {
										((ItemBlock) drone.getDrone()
												.getInventory()
												.getStackInSlot(slot).getItem())
												.placeBlockAt(
														drone.getDrone()
																.getInventory()
																.getStackInSlot(
																		slot),
														fakePlayer,
														drone.getDrone().worldObj,
														x, y, z, dir
																.getOpposite()
																.ordinal(), 0,
														0, 0);
									}
								} else {
									drone.getDrone()
											.getInventory()
											.getStackInSlot(slot)
											.tryPlaceItemIntoWorld(
													fakePlayer,
													drone.getDrone().worldObj,
													x,
													y,
													z,
													dir.getOpposite().ordinal(),
													0, 0, 0);
								}
								return true;
							}
						};

						FutureTask<Boolean> task = new FutureTask<Boolean>(
								callable);
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
						final ForgeDirection dir = ForgeDirection
								.getOrientation(luaState.checkInteger(-1));
						Callable<Boolean> callable;
						if (luaState.getTop() == 1) {
							callable = new Callable<Boolean>() {
								@Override
								public Boolean call() throws Exception {
									int x = (int) (Math
											.floor(drone.getDrone().posX) + dir.offsetX);
									int y = (int) (Math
											.floor(drone.getDrone().posY) + dir.offsetY);
									int z = (int) (Math
											.floor(drone.getDrone().posZ) + dir.offsetZ);
									return fakePlayer.theItemInWorldManager
											.activateBlockOrUseItem(fakePlayer,
													drone.getDrone().worldObj,
													null, x, y, z, dir
															.getOpposite()
															.ordinal(), 0, 0, 0);
								}
							};
						} else {
							final int slot = luaState.checkInteger(-2);
							callable = new Callable<Boolean>() {
								@Override
								public Boolean call() throws Exception {
									int x = (int) (Math
											.floor(drone.getDrone().posX) + dir.offsetX);
									int y = (int) (Math
											.floor(drone.getDrone().posY) + dir.offsetY);
									int z = (int) (Math
											.floor(drone.getDrone().posZ) + dir.offsetZ);
									return fakePlayer.theItemInWorldManager
											.activateBlockOrUseItem(
													fakePlayer,
													drone.getDrone().worldObj,
													drone.getDrone()
															.getInventory()
															.getStackInSlot(
																	slot), x,
													y, z, dir.getOpposite()
															.ordinal(), 0, 0, 0);
								}
							};
						}

						FutureTask<Boolean> task = new FutureTask<Boolean>(
								callable);
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
			luaState.register(leftCard.getValue1()
					.getName(leftCard.getValue2()), leftCard.getValue1()
					.getFunctions(leftCard.getValue2(), this));
			luaState.pop(1);
		}
		if (luaState != null && luaState.isOpen() && rightCard != null) {
			luaState.register(
					rightCard.getValue1().getName(rightCard.getValue2()),
					rightCard.getValue1().getFunctions(rightCard.getValue2(),
							this));
			luaState.pop(1);
		}
		luaStateLock.unlock();
	}

	public void setLeftCard(ICard card, ItemStack stack) {
		luaStateLock.lock();
		if (luaState != null
				&& luaState.isOpen()
				&& card == null
				&& this.leftCard != null
				&& (this.rightCard != null ? (rightCard.getValue2() != leftCard
						.getValue2()) : true)) {
			luaState.pushNil();
			luaState.setGlobal(leftCard.getValue1().getName(
					leftCard.getValue2()));
		}
		if ((leftCard == null || (card != leftCard.getValue1()
				&& luaState != null && luaState.isOpen()))
				&& card != null && luaState != null) {
			luaState.register(card.getName(stack),
					card.getFunctions(stack, this));
			luaState.setGlobal(card.getName(stack));
		}
		luaStateLock.unlock();
		this.leftCard = (card == null ? null
				: new ObjectPair<ICard, ItemStack>(card, stack));
	}

	public void setRightCard(ICard card, ItemStack stack) {
		luaStateLock.lock();
		if (luaState != null
				&& luaState.isOpen()
				&& card == null
				&& this.rightCard != null
				&& (this.leftCard != null ? (rightCard.getValue2() != leftCard
						.getValue2()) : true)) {
			luaState.pushNil();
			luaState.setGlobal(rightCard.getValue1().getName(
					rightCard.getValue2()));
		}
		if ((rightCard == null || (card != rightCard.getValue1()
				&& luaState != null && luaState.isOpen()))
				&& card != null && luaState != null) {
			luaState.register(card.getName(stack),
					card.getFunctions(stack, this));
			luaState.setGlobal(card.getName(stack));
		}
		luaStateLock.unlock();
		this.rightCard = (card == null ? null
				: new ObjectPair<ICard, ItemStack>(card, stack));
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
				dos.writeInt(drone.entityId);
				dos.writeBoolean(fly);
				packet.id = 6;
				packet.data = bos.toByteArray();
				PacketDispatcher.sendPacketToAllAround(drone.posX, drone.posY,
						drone.posZ, 20, drone.worldObj.provider.dimensionId,
						packet.getMCPacket());
			} catch (IOException e) {
				ElectroCraft.instance.getLogger().fine(
						"Error sending tool use update to entity!");
			}
		}
		this.flying = fly;
	}

	public boolean addToInventory(IInventory inventory, int startIndex,
			int endIndex, ItemStack item) {
		for (int i = startIndex; i < endIndex; i++) {
			if (inventory.getStackInSlot(i) != null
					&& inventory.getStackInSlot(i).itemID == item.itemID) {
				if (inventory.getStackInSlot(i).stackSize + item.stackSize > item
						.getMaxStackSize()) {
					int totalAmount = inventory.getStackInSlot(i).stackSize
							+ item.stackSize;
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

	public ForgeDirection getDirection(float rotation) {
		return getDirection(getDir(rotation));
	}

	public ForgeDirection getDirection(int direction) {
		switch (direction) {
		case 0:
			return ForgeDirection.SOUTH;
		case 1:
			return ForgeDirection.WEST;
		case 2:
			return ForgeDirection.NORTH;
		case 3:
			return ForgeDirection.EAST;
		default:
			return ForgeDirection.UNKNOWN;
		}
	}

	public float getRotation(ForgeDirection direction) {
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
