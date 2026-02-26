package com.mastermarisa.kaleidoscopecookery_automation.client.ponder.scene;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StoveBlock;
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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;

public class SteamerScenes {
    public static void introduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("steamer", "使用汤锅");
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

        PonderBlockWrapper stove = new PonderBlockWrapper(3, 1, 4, scene, util);
        PonderBlockWrapper steamer1 = new PonderBlockWrapper(3, 2, 4, scene, util);
        PonderBlockWrapper steamer2 = new PonderBlockWrapper(3, 3, 4, scene, util);
        Selection steamersSel = util.select().fromTo(steamer1.pos, steamer2.pos);
        stove.modifyBlock((s) -> s.setValue(StoveBlock.LIT, true), false);

        PonderBlockWrapper depot = new PonderBlockWrapper(3, 1, 2, scene, util);
        ItemStack dough = new ItemStack(ModItems.RAW_DOUGH.get());

        scene.idle(20);

        cogWheel1.showSection(Direction.DOWN);
        armInput.showSection(Direction.DOWN);
        scene.idle(4);
        scene.effects().rotationSpeedIndicator(armInput.pos);
        depot.showSection(Direction.SOUTH);
        stove.showSection(Direction.EAST);
        scene.idle(4);
        steamer1.showSection(Direction.EAST);
        scene.idle(4);
        steamer2.showSection(Direction.EAST);
        scene.world().createItemOnBeltLike(depot.pos, Direction.NORTH, dough.copy());

        scene.idle(30);

        scene.overlay().showText(25).text("输入").colored(PonderPalette.INPUT).pointAt(depot.blockSurface(Direction.UP)).placeNearTarget();
        scene.idle(35);

        scene.overlay().showOutlineWithText(steamersSel, 30).text("输出").colored(PonderPalette.OUTPUT).pointAt(steamer1.blockSurface(Direction.UP)).placeNearTarget();
        scene.idle(40);

        scene.addKeyframe();

        scene.idle(20);
        scene.overlay().showText(140).text("若蒸笼中存在空位，动力臂会向蒸笼内放入存在蒸笼配方的食材").placeNearTarget();
        scene.idle(20);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().modifyBlockEntity(depot.pos, DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, dough.copy(), 0);
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, dough.copy(), 0);
        scene.idle(12);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, dough.copy(), -1);
        steamer1.modifyBlockEntity(SteamerBlockEntity.class, (be) -> {
            Collections.fill(be.getItems(), dough.copy());
            be.refresh();
        });
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, dough.copy(), 1);
        scene.idle(12);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, ItemStack.EMPTY, -1);
        steamer2.modifyBlockEntity(SteamerBlockEntity.class, (be) -> {
            Collections.fill(be.getItems(), dough.copy());
            be.refresh();
        });
        scene.idle(5);
        scene.idle(25);
        scene.overlay().showControls(util.vector().blockSurface(steamer2.pos, Direction.UP), Pointing.DOWN, 15).rightClick().whileSneaking();
        scene.idle(5);
        steamer2.modifyBlock((s) -> s.setValue(SteamerBlock.HAS_LID, true), false);
        scene.idle(30);

        scene.addKeyframe();

        scene.idle(20);

        scene.overlay().showText(40).text("接下来只需等待一段时间...").placeNearTarget();
        scene.overlay().showControls(util.vector().blockSurface(steamer2.pos.above() , Direction.UP), Pointing.DOWN, 35).withItem(new ItemStack(Items.CLOCK));
        scene.idle(55);

        scene.addKeyframe();

        scene.idle(20);

        depot.hideSection(Direction.UP);
        scene.idle(15);
        armOutput.showSection(Direction.DOWN);
        cogWheel3.setBlock(AllBlocks.COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Y), false);
        scene.world().setKineticSpeed(cogWheel3.selection(), 96);
        cogWheel3.showSection(Direction.DOWN);
        scene.idle(20);
        scene.overlay().showOutlineWithText(steamersSel, 30).text("输入").colored(PonderPalette.INPUT).pointAt(steamer1.blockSurface(Direction.UP)).placeNearTarget();
        scene.idle(35);

        scene.addKeyframe();

        ItemStack mantou = new ItemStack(ModItems.MANTOU.get());
        scene.overlay().showText(55).text("待烹饪完成，以蒸笼为输入点的动力臂会取出产物").placeNearTarget();
        scene.idle(20);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, mantou.copy(), 0);
        steamer1.modifyBlockEntity(SteamerBlockEntity.class, (be) -> {
            be.getItems().clear();
            be.refresh();
        });
        scene.idle(5);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, mantou.copy(), 1);
        scene.idle(12);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, mantou.copy(), -1);
        steamer2.modifyBlockEntity(SteamerBlockEntity.class, (be) -> {
            Collections.fill(be.getItems(), dough.copy());
            be.refresh();
        });
        scene.idle(25);
        scene.overlay().showControls(util.vector().blockSurface(armOutput.pos.above(), Direction.UP), Pointing.DOWN, 15).rightClick();
        scene.idle(5);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(armOutput.pos.above(), Direction.UP), Pointing.DOWN, 15).withItem(mantou.copy());
        scene.idle(40);
    }
}
