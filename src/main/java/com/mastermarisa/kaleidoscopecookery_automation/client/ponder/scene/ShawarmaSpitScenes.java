package com.mastermarisa.kaleidoscopecookery_automation.client.ponder.scene;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ShawarmaSpitBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ShawarmaSpitBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.utils.PonderBlockWrapper;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;

import java.util.Collections;
import java.util.Objects;

public class ShawarmaSpitScenes {
    public static void introduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        Level level = Objects.requireNonNull(Minecraft.getInstance().level);

        scene.title("shawarma_spit", "使用汤锅");
        scene.configureBasePlate(1, 1, 5);
        scene.showBasePlate();

        PonderBlockWrapper cogWheel1 = new PonderBlockWrapper(5, 1, 3, scene, util);
        PonderBlockWrapper cogWheel2 = new PonderBlockWrapper(6, 0, 3, scene, util);
        PonderBlockWrapper cogWheel3 = new PonderBlockWrapper(3, 1, 2, scene, util);
        scene.world().setKineticSpeed(cogWheel1.selection(), 48);
        scene.world().setKineticSpeed(cogWheel2.selection(), 48);
        cogWheel2.showSection(Direction.UP);

        PonderBlockWrapper armInput = new PonderBlockWrapper(4, 1, 2, scene, util);
        PonderBlockWrapper armOutput = new PonderBlockWrapper(2, 1, 2, scene, util);
        scene.world().setKineticSpeed(armInput.selection(), 96);
        scene.world().setKineticSpeed(armOutput.selection(), 96);

        PonderBlockWrapper spitLow = new PonderBlockWrapper(3, 1, 4, scene, util);
        PonderBlockWrapper spitHigh = new PonderBlockWrapper(3, 2, 4, scene, util);
        PonderBlockWrapper redStone = new PonderBlockWrapper(2, 1, 4, scene, util);
        PonderBlockWrapper lever = new PonderBlockWrapper(1, 1, 4, scene, util);
        Selection spitSel = util.select().fromTo(spitLow.pos, spitHigh.pos);

        PonderBlockWrapper depot = new PonderBlockWrapper(3, 1, 2, scene, util);
        ItemStack beef = new ItemStack(Items.BEEF);

        scene.idle(20);

        cogWheel1.showSection(Direction.DOWN);
        armInput.showSection(Direction.DOWN);
        scene.idle(4);
        scene.effects().rotationSpeedIndicator(armInput.pos);
        depot.showSection(Direction.SOUTH);
        scene.world().showSection(spitSel, Direction.EAST);
        scene.idle(4);
        redStone.showSection(Direction.EAST);
        scene.idle(4);
        lever.showSection(Direction.EAST);
        scene.world().createItemOnBeltLike(depot.pos, Direction.WEST, beef.copy());

        scene.idle(20);

        scene.overlay().showText(25).text("输入").colored(PonderPalette.INPUT).pointAt(depot.blockSurface(Direction.UP)).placeNearTarget();
        scene.idle(35);

        scene.overlay().showOutlineWithText(spitSel, 30).text("输出").colored(PonderPalette.OUTPUT).pointAt(spitLow.blockSurface(Direction.UP)).placeNearTarget();
        scene.idle(40);

        scene.addKeyframe();

        scene.idle(20);
        scene.overlay().showText(120).text("若旋风烤肉塔的内容为空，动力臂会向塔内放入存在营火配方的材料").placeNearTarget();
        scene.idle(20);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().modifyBlockEntity(depot.pos, DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, beef.copy(), 0);
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, beef.copy(), 0);
        scene.idle(12);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, beef.copy(), -1);
        spitLow.modifyBlockEntity(ShawarmaSpitBlockEntity.class, (be) -> {
            be.onPutCookingItem(level, new ItemStack(Items.BEEF, 8));
            be.refresh();
        });
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, beef.copy(), 1);
        scene.idle(12);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, ItemStack.EMPTY, -1);
        spitHigh.modifyBlockEntity(ShawarmaSpitBlockEntity.class, (be) -> {
            be.onPutCookingItem(level, new ItemStack(Items.BEEF, 8));
            be.refresh();
        });
        scene.idle(30);
        scene.overlay().showControls(util.vector().blockSurface(lever.pos, Direction.UP), Pointing.DOWN, 15).rightClick();
        scene.idle(5);
        lever.modifyBlock((s) -> s.setValue(LeverBlock.POWERED, true), false);
        redStone.modifyBlock((s) -> s.setValue(RedStoneWireBlock.POWER, 15), false);
        spitLow.modifyBlock((s) -> s.setValue(ShawarmaSpitBlock.POWERED, true), false);
        spitHigh.modifyBlock((s) -> s.setValue(ShawarmaSpitBlock.POWERED, true), false);
        scene.idle(30);

        scene.addKeyframe();

        scene.idle(20);

        scene.overlay().showText(40).text("接下来只需等待一段时间...").placeNearTarget();
        scene.overlay().showControls(util.vector().blockSurface(spitHigh.pos.above() , Direction.UP), Pointing.DOWN, 35).withItem(new ItemStack(Items.CLOCK));
        scene.idle(55);
        spitLow.modifyBlockEntity(ShawarmaSpitBlockEntity.class, (be) -> {
            be.cookingItem.split(8);
            be.cookedItem = new ItemStack(Items.COOKED_BEEF, 8);
            be.refresh();
        });
        spitHigh.modifyBlockEntity(ShawarmaSpitBlockEntity.class, (be) -> {
            be.cookingItem.split(8);
            be.cookedItem = new ItemStack(Items.COOKED_BEEF, 8);
            be.refresh();
        });

        scene.addKeyframe();

        scene.idle(20);

        depot.hideSection(Direction.UP);
        scene.idle(15);
        armOutput.showSection(Direction.DOWN);
        cogWheel3.setBlock(AllBlocks.COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Y), false);
        scene.world().setKineticSpeed(cogWheel3.selection(), 96);
        cogWheel3.showSection(Direction.DOWN);
        scene.idle(20);
        scene.overlay().showOutlineWithText(spitSel, 30).text("输入").colored(PonderPalette.INPUT).pointAt(spitLow.blockSurface(Direction.UP)).placeNearTarget();
        scene.idle(35);

        scene.addKeyframe();

        ItemStack cooked = new ItemStack(Items.COOKED_BEEF);
        scene.overlay().showText(55).text("待烹饪完成，以旋风烤肉塔为输入点的动力臂会取出产物").placeNearTarget();
        scene.idle(20);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, cooked.copy(), 0);
        spitLow.modifyBlockEntity(ShawarmaSpitBlockEntity.class, (be) -> {
            be.cookedItem.split(8);
            be.refresh();
        });
        scene.idle(5);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, cooked.copy(), 1);
        scene.idle(12);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, cooked.copy(), -1);
        spitHigh.modifyBlockEntity(ShawarmaSpitBlockEntity.class, (be) -> {
            be.cookedItem.split(8);
            be.refresh();
        });
        scene.idle(25);
        scene.overlay().showControls(util.vector().blockSurface(armOutput.pos.above(), Direction.UP), Pointing.DOWN, 15).rightClick();
        scene.idle(5);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(armOutput.pos.above(), Direction.UP), Pointing.DOWN, 15).withItem(cooked.copy());
        scene.idle(40);
    }
}
