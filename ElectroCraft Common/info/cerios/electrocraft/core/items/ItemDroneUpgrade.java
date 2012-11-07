package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.api.drone.upgrade.ICard;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.drone.Drone;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraftforge.common.ForgeDirection;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

public class ItemDroneUpgrade extends Item implements ICard {

	public ItemDroneUpgrade(int id) {
		super(id);
		setHasSubtypes(true);
	}

	@Override
	public int getIconFromDamage(int meta) {
		switch (meta) {
		case 0:
			return 9;
		case 1:
			return 8;
		case 2:
			return 6;
		case 3:
			return 7;
		case 4:
			return 5;
		}
		return 0;
	}

	@Override
	public String getItemNameIS(ItemStack stack) {
		switch (stack.getItemDamage()) {
		case 0:
			return "info.cerios.electrocraft.items.gyro";
		case 1:
			return "info.cerios.electrocraft.items.gps";
		case 2:
			return "info.cerios.electrocraft.items.anaylzer";
		case 3:
			return "info.cerios.electrocraft.items.engine";
		case 4:
			return "info.cerios.electrocraft.items.ai";
		}
		return "";
	}

	@Override
	public String getName(ItemStack stack) {
		switch (stack.getItemDamage()) {
		case 0:
			return "gyro";
		case 1:
			return "gps";
		case 2:
			return "anaylzer";
		case 3:
			return "engine";
		case 4:
			return "ai";
		}
		return "";
	}

	@Override
	public NamedJavaFunction[] getFunctions(ItemStack stack, Drone drone) {
		switch (stack.getItemDamage()) {
		case 0:
			return new NamedJavaFunction[] { new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					Callable<Integer> callable = new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							ForgeDirection dir = drone.getDirection(drone
									.getDrone().rotationYaw);
							return dir.ordinal();
						}
					};
					final FutureTask<Integer> task = new FutureTask<Integer>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						luaState.pushInteger(task.get());
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
					return "getForgeDir";
				}
			}.init(drone), new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					Callable<Integer> callable = new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return drone.getDir(drone.getDrone().rotationYaw);
						}
					};
					final FutureTask<Integer> task = new FutureTask<Integer>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						luaState.pushInteger(task.get());
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
					return "getDir";
				}
			}.init(drone), new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					Callable<Float> callable = new Callable<Float>() {
						@Override
						public Float call() throws Exception {
							return drone.getDrone().rotationYaw;
						}
					};
					final FutureTask<Float> task = new FutureTask<Float>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						luaState.pushNumber(task.get());
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
					return "getRotation";
				}
			}.init(drone), new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					final float rotation = (float) luaState.checkNumber(-1);
					Callable<Boolean> callable = new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							drone.getDrone().rotate(rotation, 5);
							return true;
						}
					};
					final FutureTask<Boolean> task = new FutureTask<Boolean>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						task.get();
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					while (drone.getDrone().isStillMovingOrRotating()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
						}
					}
					return 0;
				}

				@Override
				public String getName() {
					return "rotate";
				}
			}.init(drone), new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					final int dir = luaState.checkInteger(-1);
					Callable<Boolean> callable = new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							drone.getDrone().rotate(
									drone.getRotation(drone.getDirection(dir)),
									5);
							return true;
						}
					};
					final FutureTask<Boolean> task = new FutureTask<Boolean>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						task.get();
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					while (drone.getDrone().isStillMovingOrRotating()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
						}
					}
					return 0;
				}

				@Override
				public String getName() {
					return "face";
				}
			}.init(drone), };
		case 1:
			return new NamedJavaFunction[] { new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					Callable<Integer[]> callable = new Callable<Integer[]>() {
						@Override
						public Integer[] call() throws Exception {
							return new Integer[] {
									MathHelper
											.floor_double(drone.getDrone().posX),
									MathHelper
											.floor_double(drone.getDrone().posY),
									MathHelper
											.floor_double(drone.getDrone().posZ) };
						}
					};
					final FutureTask<Integer[]> task = new FutureTask<Integer[]>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						Integer[] values = task.get();
						luaState.pushInteger(values[0]);
						luaState.pushInteger(values[1]);
						luaState.pushInteger(values[2]);
						return 3;
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					return 0;
				}

				@Override
				public String getName() {
					return "getLocation";
				}
			}.init(drone), new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					Callable<Double[]> callable = new Callable<Double[]>() {
						@Override
						public Double[] call() throws Exception {
							return new Double[] {
									drone.getDrone().posX
											- Math.floor(drone.getDrone().posX),
									drone.getDrone().posY
											- Math.floor(drone.getDrone().posY),
									drone.getDrone().posZ
											- Math.floor(drone.getDrone().posZ) };
						}
					};
					final FutureTask<Double[]> task = new FutureTask<Double[]>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						Double[] values = task.get();
						luaState.pushNumber(values[0]);
						luaState.pushNumber(values[1]);
						luaState.pushNumber(values[2]);
						return 3;
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					return 0;
				}

				@Override
				public String getName() {
					return "getOffset";
				}
			}.init(drone), };
		case 2:
			return new NamedJavaFunction[] { new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					Callable<Integer> callable = new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							ForgeDirection dir = drone.getDirection(drone
									.getDrone().rotationYaw);
							return drone.getDrone().worldObj
									.getBlockId((int) (dir.offsetX + drone
											.getDrone().posX),
											(int) (dir.offsetY + drone
													.getDrone().posY),
											(int) (dir.offsetZ + drone
													.getDrone().posZ));
						}
					};
					final FutureTask<Integer> task = new FutureTask<Integer>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						luaState.pushInteger(task.get());
						return 3;
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					return 0;
				}

				@Override
				public String getName() {
					return "sampleFront";
				}
			}.init(drone), new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(final LuaState luaState) {
					Callable<Integer[]> callable = new Callable<Integer[]>() {
						@Override
						public Integer[] call() throws Exception {
							ForgeDirection dir = ForgeDirection
									.getOrientation(luaState.checkInteger(-1));
							int x = (int) (Math.floor(drone.getDrone().posX) + dir.offsetX);
							int y = (int) (Math.floor(drone.getDrone().posY) + dir.offsetY);
							int z = (int) (Math.floor(drone.getDrone().posZ) + dir.offsetZ);
							return new Integer[] {
									drone.getDrone().worldObj.getBlockId(x, y,
											z),
									drone.getDrone().worldObj.getBlockMetadata(
											x, y, z) };
						}
					};
					final FutureTask<Integer[]> task = new FutureTask<Integer[]>(
							callable);
					ElectroCraft.instance.registerRunnable(task);
					try {
						Integer[] values = task.get();
						luaState.pushInteger(values[0]);
						luaState.pushInteger(values[1]);
						return 2;
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					return 0;
				}

				@Override
				public String getName() {
					return "sample";
				}
			}.init(drone), };
		case 3:
			return new NamedJavaFunction[] { new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					final double x = luaState.checkNumber(-3), y = luaState
							.checkNumber(-2), z = luaState.checkNumber(-1);
					Callable<Boolean> callable = new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							double dx = x - drone.getDrone().posX;
							double dy = y - drone.getDrone().posY;
							double dz = z - drone.getDrone().posZ;
							drone.getDrone().moveEntity(dx, dy, dz);
							return true;
						}
					};
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
					return "moveTo";
				}
			}.init(drone), new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(LuaState luaState) {
					drone.setFlying(luaState.checkBoolean(-1));
					return 0;
				}

				@Override
				public String getName() {
					return "fly";
				}
			}.init(drone), };
		case 4:
			return new NamedJavaFunction[] { new NamedJavaFunction() {
				Drone drone;

				public NamedJavaFunction init(Drone drone) {
					this.drone = drone;
					return this;
				}

				@Override
				public int invoke(final LuaState luaState) {
					Callable<Boolean> callable = new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return drone
									.getDrone()
									.getNavigator()
									.tryMoveToXYZ(
											luaState.checkNumber(-4),
											luaState.checkNumber(-3),
											luaState.checkNumber(-2),
											MathHelper.clamp_float(
													(float) luaState
															.checkNumber(-1),
													0f, 1f));
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
					return "move";
				}
			}.init(drone), };
		}
		return null;
	}

	@Override
	public void passiveFunctionTick(ItemStack stack) {
		switch (stack.getItemDamage()) {

		}
	}

	@Override
	public void getSubItems(int id, CreativeTabs tab, List list) {
		list.add(new ItemStack(id, 1, 0));
		list.add(new ItemStack(id, 1, 1));
		list.add(new ItemStack(id, 1, 2));
		list.add(new ItemStack(id, 1, 3));
		list.add(new ItemStack(id, 1, 4));
	}

	@Override
	public String getTextureFile() {
		return "/info/cerios/electrocraft/gfx/items.png";
	}
}
