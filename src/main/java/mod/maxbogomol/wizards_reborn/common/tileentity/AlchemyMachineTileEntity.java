package mod.maxbogomol.wizards_reborn.common.tileentity;

import mod.maxbogomol.wizards_reborn.WizardsReborn;
import mod.maxbogomol.wizards_reborn.api.alchemy.PipeConnection;
import mod.maxbogomol.wizards_reborn.api.alchemy.SteamUtils;
import mod.maxbogomol.wizards_reborn.api.wissen.IWissenWandFunctionalTileEntity;
import mod.maxbogomol.wizards_reborn.api.wissen.WissenUtils;
import mod.maxbogomol.wizards_reborn.common.recipe.AlchemyMachineContext;
import mod.maxbogomol.wizards_reborn.common.recipe.AlchemyMachineRecipe;
import mod.maxbogomol.wizards_reborn.utils.PacketUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class AlchemyMachineTileEntity extends PipeBaseTileEntity implements TickableBlockEntity, IWissenWandFunctionalTileEntity {
    protected FluidTank fluidTank1 = new FluidTank(getMaxCapacity()) {
        @Override
        public void onContentsChanged() {
            AlchemyMachineTileEntity.this.setChanged();
        }
    };
    protected FluidTank fluidTank2 = new FluidTank(getMaxCapacity()) {
        @Override
        public void onContentsChanged() {
            AlchemyMachineTileEntity.this.setChanged();
        }
    };
    protected FluidTank fluidTank3 = new FluidTank(getMaxCapacity()) {
        @Override
        public void onContentsChanged() {
            AlchemyMachineTileEntity.this.setChanged();
        }
    };
    public LazyOptional<IFluidHandler> fluidHolder1 = LazyOptional.of(() -> fluidTank1);
    public LazyOptional<IFluidHandler> fluidHolder2 = LazyOptional.of(() -> fluidTank2);
    public LazyOptional<IFluidHandler> fluidHolder3 = LazyOptional.of(() -> fluidTank3);

    public final ItemStackHandler itemHandler = createHandler(6);
    public final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    public final ItemStackHandler itemOutputHandler = createHandler(1);
    public final LazyOptional<IItemHandler> outputHandler = LazyOptional.of(() -> itemOutputHandler);

    public static Direction[] directions = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };


    public int wissenInCraft= 0;
    public int wissenIsCraft = 0;
    public int steamInCraft= 0;
    public int steamIsCraft = 0;
    public boolean startCraft = false;

    public Random random = new Random();

    public AlchemyMachineTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public AlchemyMachineTileEntity(BlockPos pos, BlockState state) {
        this(WizardsReborn.ALCHEMY_MACHINE_TILE_ENTITY.get(), pos, state);
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            initConnections();

            boolean update = false;

            if (level.getBlockEntity(getBlockPos().above()) instanceof AlchemyBoilerTileEntity boiler) {
                SimpleContainer inv = new SimpleContainer(7);
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    inv.setItem(i, itemHandler.getStackInSlot(i));
                }
                inv.setItem(6, itemOutputHandler.getStackInSlot(0));

                AlchemyMachineContext conext = new AlchemyMachineContext(inv, fluidTank1, fluidTank2, fluidTank3);
                Optional<AlchemyMachineRecipe> recipe = level.getRecipeManager().getRecipeFor(WizardsReborn.ALCHEMY_MACHINE_RECIPE.get(), conext, level);
                wissenInCraft = recipe.map(AlchemyMachineRecipe::getRecipeWissen).orElse(0);
                steamInCraft = recipe.map(AlchemyMachineRecipe::getRecipeSteam).orElse(0);

                if ((wissenInCraft <= 0 && (wissenIsCraft > 0 || startCraft)) || (steamInCraft <= 0 && (steamIsCraft > 0 || startCraft))) {
                    wissenIsCraft = 0;
                    steamIsCraft = 0;
                    startCraft = false;

                    update = true;
                }

                if (recipe.isPresent()) {
                    if ((wissenInCraft > 0) && (boiler.wissen > 0) && (startCraft)) {
                        ItemStack output = recipe.get().getResultItem(RegistryAccess.EMPTY);

                        if (isCanCraft(inv, output)) {
                            int addRemainCraft = WissenUtils.getAddWissenRemain(wissenIsCraft, 6, wissenInCraft);
                            int removeRemain = WissenUtils.getRemoveWissenRemain(boiler.getWissen(), 6 - addRemainCraft);

                            wissenIsCraft = wissenIsCraft + (6 - addRemainCraft - removeRemain);
                            boiler.removeWissen(6 - addRemainCraft - removeRemain);

                            update = true;
                        }
                    }

                    if ((steamInCraft > 0) && (boiler.steam > 0) && (startCraft)) {
                        ItemStack output = recipe.get().getResultItem(RegistryAccess.EMPTY);

                        if (isCanCraft(inv, output)) {
                            int addRemainCraft = SteamUtils.getAddSteamRemain(steamIsCraft, 3, steamInCraft);
                            int removeRemain = SteamUtils.getRemoveSteamRemain(boiler.getSteam(), 3 - addRemainCraft);

                            steamIsCraft = steamIsCraft + (3 - addRemainCraft - removeRemain);
                            boiler.removeSteam(3 - addRemainCraft - removeRemain);

                            update = true;
                        }
                    }

                    if ((wissenInCraft > 0 || steamInCraft > 0) && startCraft) {
                        if (wissenInCraft <= wissenIsCraft && steamInCraft <= steamIsCraft) {
                            ItemStack output = recipe.get().getResultItem(RegistryAccess.EMPTY).copy();

                            if (isCanCraft(inv, output)) {
                                wissenInCraft = 0;
                                wissenIsCraft = 0;
                                steamInCraft = 0;
                                steamIsCraft = 0;
                                startCraft = false;

                                output.setCount(itemOutputHandler.getStackInSlot(0).getCount() + output.getCount());

                                itemOutputHandler.setStackInSlot(0, output);

                                for (int i = 0; i < 6; i++) {
                                    itemHandler.extractItem(i, 1, false);
                                }

                                for (int i = 0; i < recipe.get().getFluidIngredients().size(); i++) {
                                    FluidStack fluidStack = recipe.get().getFluidIngredients().get(i).getFluids().get(0);

                                    if (fluidTank1.isFluidValid(fluidStack)) {
                                        FluidStack extracted1 = fluidTank1.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                                        fluidTank1.drain(extracted1, IFluidHandler.FluidAction.EXECUTE);
                                    }

                                    if (fluidTank2.isFluidValid(fluidStack)) {
                                        FluidStack extracted2 = fluidTank2.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                                        fluidTank2.drain(extracted2, IFluidHandler.FluidAction.EXECUTE);
                                    }

                                    if (fluidTank3.isFluidValid(fluidStack)) {
                                        FluidStack extracted3 = fluidTank3.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                                        fluidTank3.drain(extracted3, IFluidHandler.FluidAction.EXECUTE);
                                    }
                                }

                                update = true;
                            }
                        }
                    }
                }
            }

            if (update) {
                PacketUtils.SUpdateTileEntityPacket(this);
                if (level.getBlockEntity(getBlockPos().above()) instanceof AlchemyBoilerTileEntity boiler) {
                    PacketUtils.SUpdateTileEntityPacket(boiler);
                }
            }
        }
    }

    private ItemStackHandler createHandler(int size) {
        return new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return true;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!isItemValid(slot, stack)) {
                    return stack;
                }

                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            Direction newSide = getTankDirection(getBlockState().getValue(HORIZONTAL_FACING));
            if (newSide == side) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, fluidHolder1);
            }
            newSide = getTankDirection(newSide);
            if (newSide ==side) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, fluidHolder2);
            }
            newSide = getTankDirection(newSide);
            if (newSide == side) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, fluidHolder3);
            }
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                CombinedInvWrapper item = new CombinedInvWrapper(itemHandler, itemOutputHandler);
                return LazyOptional.of(() -> item).cast();
            }

            if (side == Direction.DOWN) {
                return outputHandler.cast();
            } else {
                return handler.cast();
            }
        }

        return super.getCapability(cap, side);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, (e) -> e.getUpdateTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
        if (level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @NotNull
    @Override
    public final CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            PacketUtils.SUpdateTileEntityPacket(this);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fluidTank1", fluidTank1.writeToNBT(new CompoundTag()));
        tag.put("fluidTank2", fluidTank2.writeToNBT(new CompoundTag()));
        tag.put("fluidTank3", fluidTank3.writeToNBT(new CompoundTag()));

        tag.put("inv", itemHandler.serializeNBT());
        tag.put("output", itemOutputHandler.serializeNBT());

        tag.putInt("wissenInCraft", wissenInCraft);
        tag.putInt("wissenIsCraft", wissenIsCraft);
        tag.putInt("steamInCraft", steamInCraft);
        tag.putInt("steamIsCraft", steamIsCraft);
        tag.putBoolean("startCraft", startCraft);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fluidTank1.readFromNBT(tag.getCompound("fluidTank1"));
        fluidTank2.readFromNBT(tag.getCompound("fluidTank2"));
        fluidTank3.readFromNBT(tag.getCompound("fluidTank3"));

        itemHandler.deserializeNBT(tag.getCompound("inv"));
        itemOutputHandler.deserializeNBT(tag.getCompound("output"));

        wissenInCraft = tag.getInt("wissenInCraft");
        wissenIsCraft = tag.getInt("wissenIsCraft");
        steamInCraft = tag.getInt("steamInCraft");
        steamIsCraft = tag.getInt("steamIsCraft");
        startCraft = tag.getBoolean("startCraft");
    }

    public void initConnections() {
        Block block = level.getBlockState(worldPosition).getBlock();
        for (Direction direction : directions) {
            BlockState facingState = level.getBlockState(worldPosition.relative(direction));
            BlockEntity facingBE = level.getBlockEntity(worldPosition.relative(direction));
            if (facingState.is(WizardsReborn.FLUID_PIPE_CONNECTION_BLOCK_TAG)) {
                if (facingBE instanceof PipeBaseTileEntity && !((PipeBaseTileEntity) facingBE).getConnection(direction.getOpposite()).transfer) {
                    connections[direction.get3DDataValue()] = PipeConnection.NONE;
                } else {
                    connections[direction.get3DDataValue()] = PipeConnection.PIPE;
                }
            } else {
                connections[direction.get3DDataValue()] = PipeConnection.NONE;
            }
        }
        loaded = true;
        setChanged();
        level.getChunkAt(worldPosition).setUnsaved(true);
        level.updateNeighbourForOutputSignal(worldPosition, block);
    }

    public int getMaxCapacity() {
        return 5000;
    }

    public int getCapacity(int index) {
        switch (index) {
            case 0:
                return fluidTank1.getCapacity();
            case 1:
                return fluidTank2.getCapacity();
            case 2:
                return fluidTank3.getCapacity();
            default:
                return fluidTank1.getCapacity();
        }
    }

    public FluidStack getFluidStack(int index) {
        switch (index) {
            case 0:
                return fluidTank1.getFluid();
            case 1:
                return fluidTank2.getFluid();
            case 2:
                return fluidTank3.getFluid();
            default:
                return fluidTank1.getFluid();
        }
    }

    public FluidTank getTank(int index) {
        switch (index) {
            case 0:
                return fluidTank1;
            case 1:
                return fluidTank2;
            case 2:
                return fluidTank3;
            default:
                return fluidTank1;
        }
    }

    public Direction getTankDirection(Direction side) {
        switch (side) {
            case NORTH:
                return Direction.EAST;
            case SOUTH:
                return Direction.WEST;
            case WEST:
                return Direction.NORTH;
            case EAST:
                return Direction.SOUTH;
            default:
                return Direction.NORTH;
        }
    }

    @Override
    public void wissenWandFuction() {
        startCraft = true;
    }

    public boolean isCanCraft(SimpleContainer inv, ItemStack output) {
        if (inv.getItem(6).isEmpty()) {
            return true;
        }

        if ((ItemHandlerHelper.canItemStacksStack(output, inv.getItem(6))) && (inv.getItem(6).getCount() + output.getCount() <= output.getMaxStackSize())) {
            return true;
        }

        return false;
    }
}