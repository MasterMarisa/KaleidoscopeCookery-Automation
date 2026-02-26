package com.mastermarisa.kaleidoscopecookery_automation.client.ponder.scene;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.utils.FunnelWrapper;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.utils.PonderBlockWrapper;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public class PotScenes {
    public static void introduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        Level level = Objects.requireNonNull(Minecraft.getInstance().level);
        LivingEntity pig = new Pig(EntityType.PIG, level);

        scene.title("pot", "使用汤锅");
        scene.configureBasePlate(1, 1, 5);
        scene.showBasePlate();

        PonderBlockWrapper cogWheel1 = new PonderBlockWrapper(5, 1, 5, scene, util);
        PonderBlockWrapper cogWheel2 = new PonderBlockWrapper(6, 0, 5, scene, util);
        scene.world().setKineticSpeed(cogWheel1.selection(), 48);
        scene.world().setKineticSpeed(cogWheel2.selection(), 48);
        scene.world().showSection(cogWheel2.selection(), Direction.UP);

        Selection inputSel = util.select().fromTo(2, 1, 4, 2, 2, 5);
        FunnelWrapper funnel = new FunnelWrapper(2, 2, 4, scene, util, scene);
        PonderBlockWrapper depot = new PonderBlockWrapper(2, 1, 4, scene, util);
        PonderBlockWrapper arm = new PonderBlockWrapper(4, 1, 4, scene, util);
        PonderBlockWrapper stove = new PonderBlockWrapper(3, 1, 2, scene, util);
        PonderBlockWrapper pot = new PonderBlockWrapper(3, 2, 2, scene, util);
        scene.world().setKineticSpeed(arm.selection(), 96);

        ItemStack recipeItem = new ItemStack(ModItems.RECIPE_ITEM.get());
        RecipeItem.setRecipe(recipeItem, new RecipeItem.RecipeRecord(List.of(), ItemStack.EMPTY, RecipeItem.STOCKPOT));

        scene.idle(20);

        cogWheel1.showSection(Direction.DOWN);
        arm.showSection(Direction.DOWN);
        scene.idle(4);
        scene.effects().rotationSpeedIndicator(arm.pos);
        stove.showSection(Direction.WEST);
        pot.showSection(Direction.WEST);
        scene.idle(4);
        scene.world().showSection(inputSel, Direction.NORTH);
        scene.idle(8);
        funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(ModItems.OIL.get()));

        scene.idle(30);

        scene.overlay().showText(25).text("输入").colored(PonderPalette.INPUT).pointAt(depot.blockSurface(Direction.UP)).placeNearTarget();
        scene.idle(35);

        scene.overlay().showText(25).text("输出").colored(PonderPalette.OUTPUT).pointAt(pot.blockSurface(Direction.WEST)).placeNearTarget();
        scene.idle(40);

        scene.overlay().showText(35).text("首先为炒锅设置目标配方").pointAt(pot.blockSurface(Direction.WEST)).placeNearTarget();
        scene.overlay().showControls(pot.blockSurface(Direction.UP), Pointing.DOWN, 30).rightClick().withItem(recipeItem);
        scene.idle(50);

        scene.addKeyframe();

        scene.overlay().showText(60).text("若锅内没有油脂，动力臂会向锅内添油 (油脂 | 油壶 | 沾油的锅铲)").placeNearTarget();
        scene.idle(15);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().modifyBlockEntity(funnel.pos.below(), DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, new ItemStack(ModItems.OIL.get()), 0);
        scene.idle(5);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, new ItemStack(ModItems.OIL.get()), 0);
        scene.idle(12);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, ItemStack.EMPTY, -1);
        pot.modifyBlock((s) -> s.setValue(PotBlock.HAS_OIL, true).setValue(PotBlock.SHOW_OIL, true), false);
        scene.idle(45);

        scene.addKeyframe();

        scene.overlay().showText(200).text("然后，动力臂会向锅内放入匹配的食材").placeNearTarget();
        scene.idle(20);
        funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(ModItems.TOMATO.get()));
        for (int i = 0;i < 6;i++) {
            ItemStack stack = new ItemStack(i < 3 ? ModItems.TOMATO.get() : ModItems.FRIED_EGG.get());
            scene.idle(5);
            scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
            scene.idle(12);
            scene.world().modifyBlockEntity(funnel.pos.below(), DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
            scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, stack.copy(), 0);
            if (i != 5) funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(i < 2 ? ModItems.TOMATO.get() : ModItems.FRIED_EGG.get()));
            else funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(ModItems.KITCHEN_SHOVEL.get()));
            scene.idle(5);
            scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, stack.copy(), 0);
            scene.idle(12);
            scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, ItemStack.EMPTY, -1);
            pot.modifyBlockEntity(PotBlockEntity.class, (be) -> {
                be.addIngredient(level, pig, stack.copy());
            });
        }

        scene.idle(10);
        scene.addKeyframe();
        scene.idle(20);
        ItemStack shovel = new ItemStack(ModItems.KITCHEN_SHOVEL.get());

        scene.overlay().showText(175).text("当锅中的食材完全匹配配方后，动力臂会开始翻炒").placeNearTarget();
        scene.idle(20);
        scene.idle(10);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().modifyBlockEntity(funnel.pos.below(), DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, shovel.copy(), 0);
        funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(Items.BOWL));
        scene.idle(5);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, shovel.copy(), 0);
        scene.idle(12);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, shovel.copy(), -1);
        pot.modifyBlockEntity(PotBlockEntity.class, (be) -> {
            be.seed = System.currentTimeMillis();
            be.refresh();
        });
        for (int i = 0;i < 4;i++) {
            scene.idle(10);
            scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, shovel.copy(), 0);
            scene.idle(12);
            scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, shovel.copy(), -1);
            pot.modifyBlockEntity(PotBlockEntity.class, (be) -> {
                be.seed = System.currentTimeMillis();
                be.refresh();
            });
        }

        scene.idle(30);
        scene.addKeyframe();

        scene.idle(20);
        scene.overlay().showText(40).text("接下来只需等待一段时间...").placeNearTarget();
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(3,3,2) , Direction.UP), Pointing.DOWN, 35).withItem(new ItemStack(Items.CLOCK));
        scene.idle(55);

        scene.addKeyframe();
        scene.idle(20);

        scene.overlay().showText(140).text("待烹饪完成，动力臂会用容器从炒锅中盛取成品 (盛具数量与配方输出数相同)").placeNearTarget();
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(arm.pos.above(), Direction.UP), Pointing.DOWN, 15).rightClick();
        scene.idle(5);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(25);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().modifyBlockEntity(funnel.pos.below(), DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, new ItemStack(Items.BOWL), 0);
        scene.idle(5);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, new ItemStack(Items.BOWL), 0);
        scene.idle(12);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, new ItemStack(ModItems.SCRAMBLE_EGG_WITH_TOMATOES.get()), -1);
        pot.modifyBlockEntity(PotBlockEntity.class, PotBlockEntity::reset);
        pot.modifyBlock((s) -> s.setValue(PotBlock.HAS_OIL, false).setValue(PotBlock.SHOW_OIL, false), false);
        scene.idle(25);
        scene.overlay().showControls(util.vector().blockSurface(arm.pos.above(), Direction.UP), Pointing.DOWN, 15).rightClick();
        scene.idle(5);
        scene.world().instructArm(arm.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(arm.pos.above(), Direction.UP), Pointing.DOWN, 15).withItem(new ItemStack(ModItems.SCRAMBLE_EGG_WITH_TOMATOES.get()));
        scene.idle(40);
    }
}
