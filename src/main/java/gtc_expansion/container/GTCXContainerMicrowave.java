package gtc_expansion.container;

import gtc_expansion.tile.GTCXTileMicrowave;
import gtclassic.api.gui.GTGuiCompMachinePower;
import gtclassic.api.slot.GTSlotUpgrade;
import ic2.core.inventory.container.ContainerTileComponent;
import ic2.core.inventory.gui.GuiIC2;
import ic2.core.inventory.gui.components.base.MachineProgressComp;
import ic2.core.inventory.slots.SlotCustom;
import ic2.core.inventory.slots.SlotDischarge;
import ic2.core.inventory.slots.SlotOutput;
import ic2.core.util.math.Box2D;
import ic2.core.util.math.Vec2i;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GTCXContainerMicrowave extends ContainerTileComponent<GTCXTileMicrowave> {

    public static Box2D machineProgressBox = new Box2D(78, 24, 20, 18); // the progress bar and size
    public static Vec2i machineProgressPos = new Vec2i(176, 0); // where the overlay is

    public GTCXContainerMicrowave(InventoryPlayer player, GTCXTileMicrowave tile) {
        super(tile);
        this.addSlotToContainer(new SlotCustom(tile, 0, 53, 25, null)); // input
        this.addSlotToContainer(new SlotDischarge(tile, Integer.MAX_VALUE, 2, 8, 62)); // battery
        this.addSlotToContainer(new SlotOutput(player.player, tile, 1, 107, 25)); // output
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer(new GTSlotUpgrade(tile, 3 + i, 62 + (i * 18), 62));
        }
        this.addComponent(new GTGuiCompMachinePower(tile));
        this.addPlayerInventory(player);
        this.addComponent(new MachineProgressComp(tile, machineProgressBox, machineProgressPos));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onGuiLoaded(GuiIC2 gui) {
        gui.dissableInvName();
    }

    @Override
    public ResourceLocation getTexture() {
        return this.getGuiHolder().getGuiTexture();
    }

    @Override
    public int guiInventorySize() {
        return this.getGuiHolder().slotCount;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.getGuiHolder().canInteractWith(playerIn);
    }
}
