package gtc_expansion.tile.multi;

import gtc_expansion.GEBlocks;
import gtc_expansion.GEMachineGui;
import gtc_expansion.GTCExpansion;
import gtc_expansion.container.GEContainerVacuumFreezer;
import gtc_expansion.material.GEMaterial;
import gtc_expansion.recipes.GERecipeLists;
import gtc_expansion.util.GELang;
import gtclassic.api.helpers.int3;
import gtclassic.api.material.GTMaterialGen;
import gtclassic.api.recipe.GTRecipeMultiInputList;
import gtclassic.api.tile.multi.GTTileMultiBaseMachine;
import ic2.api.classic.item.IMachineUpgradeItem;
import ic2.api.classic.recipe.RecipeModifierHelpers;
import ic2.api.classic.recipe.machine.MachineOutput;
import ic2.api.recipe.IRecipeInput;
import ic2.core.RotationList;
import ic2.core.inventory.container.ContainerIC2;
import ic2.core.inventory.filters.ArrayFilter;
import ic2.core.inventory.filters.BasicItemFilter;
import ic2.core.inventory.filters.CommonFilters;
import ic2.core.inventory.filters.IFilter;
import ic2.core.inventory.filters.MachineFilter;
import ic2.core.inventory.management.AccessRule;
import ic2.core.inventory.management.InventoryHandler;
import ic2.core.inventory.management.SlotType;
import ic2.core.item.recipe.entry.RecipeInputItemStack;
import ic2.core.item.recipe.entry.RecipeInputOreDict;
import ic2.core.platform.lang.components.base.LocaleComp;
import ic2.core.platform.registry.Ic2Items;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GETileMultiVacuumFreezer extends GTTileMultiBaseMachine {
    protected static final int slotInput = 0;
    protected static final int slotOutput = 1;
    protected static final int slotFuel = 2;
    public IFilter filter = new MachineFilter(this);
    public static final IBlockState casingAdvancedState = GEBlocks.casingAdvanced.getDefaultState();
    public static final IBlockState casingReinforcedState = GEBlocks.casingReinforced.getDefaultState();
    public static final IBlockState airState = Blocks.AIR.getDefaultState();
    public static final ResourceLocation GUI_LOCATION = new ResourceLocation(GTCExpansion.MODID, "textures/gui/vacuumfreezer.png");
    private static final int defaultEu = 64;

    public GETileMultiVacuumFreezer() {
        super(3, 2, defaultEu, 128);
        maxEnergy = 10000;
    }

    @Override
    protected void addSlots(InventoryHandler handler) {
        handler.registerDefaultSideAccess(AccessRule.Both, RotationList.ALL);
        handler.registerDefaultSlotAccess(AccessRule.Both, slotFuel);
        handler.registerDefaultSlotAccess(AccessRule.Import, slotInput);
        handler.registerDefaultSlotAccess(AccessRule.Export, slotOutput);
        handler.registerDefaultSlotsForSide(RotationList.UP, slotInput);
        handler.registerDefaultSlotsForSide(RotationList.HORIZONTAL, slotInput);
        handler.registerDefaultSlotsForSide(RotationList.UP.invert(), slotOutput);
        handler.registerInputFilter(new ArrayFilter(CommonFilters.DischargeEU, new BasicItemFilter(Items.REDSTONE), new BasicItemFilter(Ic2Items.suBattery)), slotFuel);
        handler.registerInputFilter(filter, slotInput);
        handler.registerOutputFilter(CommonFilters.NotDischargeEU, slotFuel);
        handler.registerSlotType(SlotType.Fuel, slotFuel);
        handler.registerSlotType(SlotType.Input, slotInput);
        handler.registerSlotType(SlotType.Output, slotOutput);
    }

    @Override
    public LocaleComp getBlockName() {
        return GELang.VACUUM_FREEZER;
    }

    @Override
    public int[] getInputSlots() {
        return new int[]{slotInput};
    }

    @Override
    public IFilter[] getInputFilters(int[] ints) {
        return new IFilter[]{filter};
    }

    @Override
    public boolean isRecipeSlot(int slot) {
        if (slot == slotInput){
            return true;
        }
        return false;
    }

    @Override
    public int[] getOutputSlots() {
        return new int[]{slotOutput};
    }

    @Override
    public GTRecipeMultiInputList getRecipeList() {
        return GERecipeLists.VACUUM_FREEZER_RECIPE_LIST;
    }

    @Override
    public Class<? extends GuiScreen> getGuiClass(EntityPlayer player) {
        return GEMachineGui.GEVacuumFreezerGui.class;
    }

    public ResourceLocation getGuiTexture() {
        return GUI_LOCATION;
    }

    @Override
    public Set<IMachineUpgradeItem.UpgradeType> getSupportedTypes() {
        return new LinkedHashSet<>(Arrays.asList(IMachineUpgradeItem.UpgradeType.values()));
    }

    @Override
    public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer) {
        return new GEContainerVacuumFreezer(entityPlayer.inventory, this);
    }

    public static void init(){
        addRecipe(GTMaterialGen.getStack(GEMaterial.Tungsten, GEMaterial.hotIngot, 1), 57600, GTMaterialGen.getIngot(GEMaterial.Tungsten, 1));
        addRecipe(GTMaterialGen.getStack(GEMaterial.TungstenSteel, GEMaterial.hotIngot, 1), 57600, GTMaterialGen.getIngot(GEMaterial.TungstenSteel, 1));
        addRecipe(GTMaterialGen.getStack(GEMaterial.Iridium, GEMaterial.hotIngot, 1), 57600, GTMaterialGen.getIngot(GEMaterial.Iridium, 1));
        addRecipe(GTMaterialGen.getStack(GEMaterial.Osmium, GEMaterial.hotIngot, 1), 57600, GTMaterialGen.getIngot(GEMaterial.Osmium, 1));
        addRecipe(GTMaterialGen.getStack(GEMaterial.Kanthal, GEMaterial.hotIngot, 1), 57600, GTMaterialGen.getIngot(GEMaterial.Kanthal, 1));
    }

    public static void addRecipe(ItemStack input, int eu, ItemStack output){
        addRecipe(new RecipeInputItemStack(input), totalEu(eu), output);
    }

    public static RecipeModifierHelpers.IRecipeModifier[] totalEu(int amount) {
        return new RecipeModifierHelpers.IRecipeModifier[] { RecipeModifierHelpers.ModifierType.RECIPE_LENGTH.create((amount / defaultEu) - 100) };
    }

    public static void addRecipe(String input, int eu, ItemStack output){
        addRecipe(input, 1, eu, output);
    }

    public static void addRecipe(String input, int amount, int eu, ItemStack output){
        addRecipe(new RecipeInputOreDict(input, amount), totalEu(eu), output);
    }

    public static void addRecipe(IRecipeInput input, RecipeModifierHelpers.IRecipeModifier[] modifiers, ItemStack output){
        List<IRecipeInput> inlist = new ArrayList<>();
        List<ItemStack> outlist = new ArrayList<>();
        inlist.add(input);
        NBTTagCompound mods = new NBTTagCompound();
        for (RecipeModifierHelpers.IRecipeModifier modifier : modifiers) {
            modifier.apply(mods);
        }
        outlist.add(output);
        addRecipe(inlist, new MachineOutput(mods, outlist));
    }

    static void addRecipe(List<IRecipeInput> input, MachineOutput output) {
        GERecipeLists.VACUUM_FREEZER_RECIPE_LIST.addRecipe(input, output, output.getAllOutputs().get(0).getUnlocalizedName(), defaultEu);
    }

    /*@Override
    public Map<BlockPos, IBlockState> provideStructure() {
        Map<BlockPos, IBlockState> states = super.provideStructure();
        int3 dir = new int3(getPos(), getFacing());
        //Top Layer
        states.put(dir.back(1).asBlockPos(), casingReinforcedState);
        states.put(dir.left(1).asBlockPos(), casingReinforcedState);
        int i;
        for (i = 0; i < 2; i++){
            states.put(dir.forward(1).asBlockPos(), casingReinforcedState);
        }
        for (i = 0; i < 2; i++){
            states.put(dir.right(1).asBlockPos(), casingReinforcedState);
        }
        for (i = 0; i < 2; i++){
            states.put(dir.back(1).asBlockPos(), casingReinforcedState);
        }
        //Middle Layer
        states.put(dir.down(1).asBlockPos(), casingReinforcedState);
        states.put(dir.forward(1).asBlockPos(), casingAdvancedState);
        states.put(dir.forward(1).asBlockPos(), casingReinforcedState);
        states.put(dir.left(1).asBlockPos(), casingAdvancedState);
        states.put(dir.left(1).asBlockPos(), casingReinforcedState);
        states.put(dir.back(1).asBlockPos(), casingAdvancedState);
        states.put(dir.back(1).asBlockPos(), casingReinforcedState);
        states.put(dir.right(1).asBlockPos(), casingAdvancedState);
        states.put(dir.forward(1).asBlockPos(), airState);
        //Bottom Layer
        states.put(dir.down(1).asBlockPos(), casingAdvancedState);
        states.put(dir.back(1).asBlockPos(), casingReinforcedState);
        states.put(dir.left(1).asBlockPos(), casingReinforcedState);
        for (i = 0; i < 2; i++){
            states.put(dir.forward(1).asBlockPos(), casingReinforcedState);
        }
        for (i = 0; i < 2; i++){
            states.put(dir.right(1).asBlockPos(), casingReinforcedState);
        }
        for (i = 0; i < 2; i++){
            states.put(dir.back(1).asBlockPos(), casingReinforcedState);
        }
        return states;
    }*/

    @Override
    public boolean checkStructure() {
        if (!world.isAreaLoaded(pos, 3)) {
            return false;
        }
        int3 dir = new int3(getPos(), getFacing());
        //Top Layer
        if (!isAdvancedCasing(dir.down(1))){
            return false;
        }
        if (!(isReinforcedCasing(dir.back(1)))) {
            return false;
        }
        if (!(isReinforcedCasing(dir.left(1)))) {
            return false;
        }
        int i;
        for (i = 0; i < 2; i++){
            if (!isReinforcedCasing(dir.forward(1))) {
                return false;
            }
        }
        for (i = 0; i < 2; i++){
            if (!(isReinforcedCasing(dir.right(1)))) {
                return false;
            }
        }
        for (i = 0; i < 2; i++){
            if (!(isReinforcedCasing(dir.back(1)))) {
                return false;
            }
        }
        //Middle Layer
        if (!(isReinforcedCasing(dir.down(1)))) {
            return false;
        }
        if (!isAdvancedCasing(dir.forward(1))) {
            return false;
        }
        if (!(isReinforcedCasing(dir.forward(1)))) {
            return false;
        }
        if (!(isAdvancedCasing(dir.left(1)))) {
            return false;
        }
        if (!(isReinforcedCasing(dir.left(1)))) {
            return false;
        }
        if (!(isAdvancedCasing(dir.back(1)))) {
            return false;
        }
        if (!isReinforcedCasing(dir.back(1))) {
            return false;
        }
        if (!(isAdvancedCasing(dir.right(1)))) {
            return false;
        }
        if (world.getBlockState(dir.forward(1).asBlockPos()) != airState) {
            return false;
        }
        //Bottom Layer
        if (!isAdvancedCasing(dir.down(1))){
            return false;
        }
        if (!(isReinforcedCasing(dir.back(1)))) {
            return false;
        }
        if (!(isReinforcedCasing(dir.left(1)))) {
            return false;
        }
        for (i = 0; i < 2; i++){
            if (!(isReinforcedCasing(dir.forward(1)))) {
                return false;
            }
        }
        for (i = 0; i < 2; i++){
            if (!(isReinforcedCasing(dir.right(1)))) {
                return false;
            }
        }
        for (i = 0; i < 2; i++){
            if (!(isReinforcedCasing(dir.back(1)))) {
                return false;
            }
        }
        return true;
    }

    public boolean isAdvancedCasing(int3 pos) {
        return world.getBlockState(pos.asBlockPos()) == casingAdvancedState;
    }

    public boolean isReinforcedCasing(int3 pos) {
        return world.getBlockState(pos.asBlockPos()) == casingReinforcedState;
    }
}
