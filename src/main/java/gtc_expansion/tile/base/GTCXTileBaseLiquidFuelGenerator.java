package gtc_expansion.tile.base;

import gtclassic.api.helpers.GTHelperFluid;
import gtclassic.api.recipe.GTRecipeMultiInputList;
import gtclassic.api.recipe.GTRecipeMultiInputList.MultiRecipe;
import ic2.api.classic.network.adv.NetworkField;
import ic2.api.classic.recipe.crafting.RecipeInputFluid;
import ic2.api.classic.recipe.machine.MachineOutput;
import ic2.api.recipe.IRecipeInput;
import ic2.core.RotationList;
import ic2.core.block.base.tile.TileEntityFuelGeneratorBase;
import ic2.core.fluid.IC2Tank;
import ic2.core.inventory.management.AccessRule;
import ic2.core.inventory.management.InventoryHandler;
import ic2.core.inventory.management.SlotType;
import ic2.core.item.misc.ItemDisplayIcon;
import ic2.core.util.obj.IClickable;
import ic2.core.util.obj.ITankListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.function.Predicate;

public abstract class GTCXTileBaseLiquidFuelGenerator extends TileEntityFuelGeneratorBase implements ITankListener, IClickable {

    @NetworkField(
            index = 7
    )
    public IC2Tank tank;
    @NetworkField(
            index = 8
    )
    public float maxFuel = 0.0F;
    protected int slotInput = 0;
    protected int slotOutput = 1;
    protected int slotDisplay = 2;
    protected MultiRecipe lastRecipe;
    protected boolean shouldCheckRecipe;
    public static final String recipeTicks = "recipeTicks";
    public static final String recipeEu = "recipeEu";

    public GTCXTileBaseLiquidFuelGenerator(int slots) {
        super(slots);
        this.tank = new IC2Tank(8000);
        this.tank.addListener(this);
        shouldCheckRecipe = true;
        this.addGuiFields("tank", "maxFuel");
    }

    @Override
    protected void addSlots(InventoryHandler handler) {
        handler.registerDefaultSideAccess(AccessRule.Both, RotationList.UP.invert());
        handler.registerDefaultSlotAccess(AccessRule.Import, 0);
        handler.registerDefaultSlotAccess(AccessRule.Export, 1);
        handler.registerDefaultSlotsForSide(RotationList.DOWN.invert(), 0);
        handler.registerDefaultSlotsForSide(RotationList.UP.invert(), 1);
        handler.registerSlotType(SlotType.Input, 0);
        handler.registerSlotType(SlotType.Output, 1);
    }

    @Override
    public void update() {
        boolean hasPower = this.storage > 0;
        if (this.shouldCheckRecipe) {
            this.lastRecipe = this.getRecipe();
            this.shouldCheckRecipe = false;
        }
        if ((hasPower) != this.getActive()) {
            this.setActive(hasPower);
        }
        boolean operate = this.lastRecipe != null && this.lastRecipe != GTRecipeMultiInputList.INVALID_RECIPE;
        GTHelperFluid.doFluidContainerThings(this, this.tank, slotInput, slotOutput);
        if (operate && this.needsFuel()){
            gainFuel();
        }
        gainEnergy();
        if (this.fuel <= 0 && !shouldCheckRecipe){
            shouldCheckRecipe = true;
        }
    }

    @Override
    public boolean gainFuel() {
        if (this.fuel <= 0 && this.tank.getFluidAmount() > 0) {
            if (this.tank.getFluid() != null) {
                FluidStack drained = this.tank.drainInternal(1000, false);
                if (drained != null) {
                    int toAdd = (int)((float)getRecipeTicks(lastRecipe.getOutputs()) * ((float)drained.amount / 1000.0F));
                    if (toAdd >= 1){
                        this.tank.drainInternal(1000, true);
                        this.fuel += toAdd;
                        this.maxFuel = (float)toAdd;
                        this.production = getRecipeEu(lastRecipe.getOutputs());
                        this.getNetwork().updateTileGuiField(this, "fuel");
                        this.getNetwork().updateTileGuiField(this, "maxFuel");
                        this.getNetwork().updateTileGuiField(this, "production");
                    }
                }
            }
        }
        return false;
    }

    public FluidStack getContained() {
        return this.tank.getFluid();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tank.readFromNBT(nbt.getCompoundTag("Tank"));
        this.maxFuel = nbt.getFloat("MaxFuel");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        this.tank.writeToNBT(this.getTag(nbt, "Tank"));
        nbt.setFloat("MaxFuel", this.maxFuel);
        return nbt;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.tank)
                : super.getCapability(capability, facing);
    }

    @Override
    public ResourceLocation getOperationSoundFile() {
        return new ResourceLocation("ic2", "sounds/Generators/WatermillLoop.ogg");
    }

    @Override
    public void onTankChanged(IFluidTank tank) {
        this.getNetwork().updateTileGuiField(this, "inputTank");
        this.inventory.set(slotDisplay, ItemDisplayIcon.createWithFluidStack(this.tank.getFluid()));
        shouldCheckRecipe = true;
    }

    public MultiRecipe getRecipe(){
        if (lastRecipe == GTRecipeMultiInputList.INVALID_RECIPE) {
            return null;
        }
        // Check if previous recipe is valid
        FluidStack input = tank.getFluid();
        if (lastRecipe != null) {
            lastRecipe = checkRecipe(lastRecipe, input) ? lastRecipe : null;
            if (lastRecipe == null) {
                production = 0;
            }
        }
        // If previous is not valid, find a new one
        if (lastRecipe == null) {
            lastRecipe = getRecipeList().getPriorityRecipe(new Predicate<MultiRecipe>() {

                @Override
                public boolean test(MultiRecipe t) {
                    return checkRecipe(t, input);
                }
            });
        }
        // If no recipe is found, return
        if (lastRecipe == null) {
            return null;
        }
        return null;
    }

    public abstract GTRecipeMultiInputList getRecipeList();

    public boolean checkRecipe(GTRecipeMultiInputList.MultiRecipe entry, FluidStack input) {
        IRecipeInput recipeInput = entry.getInput(0);
        if (recipeInput instanceof RecipeInputFluid){
            return input != null && input.containsFluid(((RecipeInputFluid)recipeInput).fluid);
        }
        return false;
    }

    public static int getRecipeTicks(MachineOutput output) {
        if (output == null || output.getMetadata() == null) {
            return 0;
        }
        return output.getMetadata().getInteger(recipeTicks);
    }

    public static int getRecipeEu(MachineOutput output) {
        if (output == null || output.getMetadata() == null) {
            return 0;
        }
        return output.getMetadata().getInteger(recipeEu);
    }

    @Override
    public boolean hasRightClick() {
        return true;
    }

    @Override
    public boolean onRightClick(EntityPlayer entityPlayer, EnumHand enumHand, EnumFacing enumFacing, Side side) {
        return GTHelperFluid.doClickableFluidContainerEmptyThings(entityPlayer, enumHand, world, pos, tank);
    }

    @Override
    public boolean hasLeftClick() {
        return false;
    }

    @Override
    public void onLeftClick(EntityPlayer entityPlayer, Side side) {

    }

    @Override
    public float getMaxFuel() {
        return maxFuel;
    }
}
