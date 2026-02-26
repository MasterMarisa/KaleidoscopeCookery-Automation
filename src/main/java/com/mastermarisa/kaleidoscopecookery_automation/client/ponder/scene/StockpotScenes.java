package com.mastermarisa.kaleidoscopecookery_automation.client.ponder.scene;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StoveBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.utils.FunnelWrapper;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.utils.PonderBlockWrapper;
import com.mastermarisa.kaleidoscopecookery_automation.mixin.StockpotBlockEntityAccessor;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Objects;

public class StockpotScenes {
    public static void introduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        Level level = Objects.requireNonNull(Minecraft.getInstance().level);
        LivingEntity pig = new Pig(EntityType.PIG, level);

        scene.title("stockpot", "使用汤锅");
        scene.configureBasePlate(1, 1, 5);
        scene.showBasePlate();

        PonderBlockWrapper cogWheel1 = new PonderBlockWrapper(5, 1, 5, scene, util);
        PonderBlockWrapper cogWheel2 = new PonderBlockWrapper(6, 0, 5, scene, util);
        PonderBlockWrapper cogWheel3 = new PonderBlockWrapper(4, 1, 3, scene, util);
        cogWheel3.setBlock(AllBlocks.COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Y), false);
        scene.world().setKineticSpeed(cogWheel1.selection(), 48);
        scene.world().setKineticSpeed(cogWheel2.selection(), 48);
        scene.world().setKineticSpeed(cogWheel3.selection(), 96);
        scene.world().showSection(cogWheel2.selection(), Direction.UP);

        Selection inputSel = util.select().fromTo(2, 1, 4, 2, 2, 5);
        FunnelWrapper funnel = new FunnelWrapper(2, 2, 4, scene, util, scene);
        PonderBlockWrapper depot = new PonderBlockWrapper(2, 1, 4, scene, util);
        PonderBlockWrapper armInput = new PonderBlockWrapper(4, 1, 4, scene, util);
        PonderBlockWrapper armOutput = new PonderBlockWrapper(4, 1, 2, scene, util);
        PonderBlockWrapper stove = new PonderBlockWrapper(3, 1, 2, scene, util);
        PonderBlockWrapper pot = new PonderBlockWrapper(3, 2, 2, scene, util);
        PonderBlockWrapper stove1 = new PonderBlockWrapper(2, 1, 2, scene, util);
        PonderBlockWrapper pot1 = new PonderBlockWrapper(2, 2, 2, scene, util);
        stove.setBlock(ModBlocks.STOVE.get().defaultBlockState().setValue(StoveBlock.FACING,Direction.NORTH).setValue(StoveBlock.LIT,true), false);
        stove1.modifyBlock((s) -> s.setValue(StoveBlock.LIT, true), false);
        pot1.modifyBlock((s) -> s.setValue(StockpotBlock.HAS_LID, true), false);
        scene.world().setKineticSpeed(armInput.selection(), 96);
        scene.world().setKineticSpeed(armOutput.selection(), 96);

        ItemStack recipeItem = new ItemStack(ModItems.RECIPE_ITEM.get());
        RecipeItem.setRecipe(recipeItem, new RecipeItem.RecipeRecord(List.of(), ItemStack.EMPTY, RecipeItem.STOCKPOT));
        ItemStack bucket = new ItemStack(Items.BUCKET);

        scene.idle(20);

        cogWheel1.showSection(Direction.DOWN);
        armInput.showSection(Direction.DOWN);
        scene.idle(4);
        scene.effects().rotationSpeedIndicator(armInput.pos);
        stove.showSection(Direction.WEST);
        pot.showSection(Direction.WEST);
        scene.idle(4);
        scene.world().showSection(inputSel, Direction.NORTH);
        scene.idle(8);
        funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(Items.WATER_BUCKET));

        scene.idle(30);

        scene.overlay().showText(25).text("输入").colored(PonderPalette.INPUT).pointAt(depot.blockSurface(Direction.UP)).placeNearTarget();
        scene.idle(35);

        scene.overlay().showText(25).text("输出").colored(PonderPalette.OUTPUT).pointAt(pot.blockSurface(Direction.WEST)).placeNearTarget();
        scene.idle(40);

        scene.overlay().showText(35).text("首先为汤锅设置目标配方").pointAt(pot.blockSurface(Direction.WEST)).placeNearTarget();
        scene.overlay().showControls(pot.blockSurface(Direction.UP), Pointing.DOWN, 30).rightClick().withItem(recipeItem);
        scene.idle(50);

        scene.addKeyframe();

        scene.overlay().showText(95).text("若没有添加汤底，动力臂会尝试向锅内添加物品形式的汤底，并返还容器").placeNearTarget();
        scene.idle(15);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().modifyBlockEntity(funnel.pos.below(), DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, new ItemStack(Items.WATER_BUCKET), 0);
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, new ItemStack(Items.WATER_BUCKET), 0);
        scene.idle(12);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, bucket, -1);
        scene.world().modifyBlockEntity(pot.pos, StockpotBlockEntity.class, (be) -> {
            be.addSoupBase(level, pig, new ItemStack(Items.WATER_BUCKET));
        });
        scene.idle(25);
        scene.overlay().showControls(util.vector().blockSurface(armInput.pos.above(), Direction.UP), Pointing.DOWN, 15).rightClick();
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(armInput.pos.above(), Direction.UP), Pointing.DOWN, 15).withItem(bucket);
        scene.idle(45);

        scene.addKeyframe();

        scene.overlay().showText(175).text("然后，动力臂会向锅内放入匹配的食材").placeNearTarget();
        scene.idle(20);
        funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(Items.POTATO));
        for (int i = 0;i < 5;i++) {
            ItemStack stack = new ItemStack(i <= 2 ? Items.POTATO : Items.BEEF);
            scene.idle(5);
            scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
            scene.idle(12);
            scene.world().modifyBlockEntity(funnel.pos.below(), DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
            scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, stack.copy(), 0);
            if (i != 4) funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(i < 2 ? Items.POTATO : Items.BEEF));
            else funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(ModItems.STOCKPOT_LID.get()));
            scene.idle(5);
            scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, stack.copy(), 0);
            scene.idle(12);
            scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, ItemStack.EMPTY, -1);
            scene.world().modifyBlockEntity(pot.pos, StockpotBlockEntity.class, (be) -> {
                be.addIngredient(level, pig, stack.copy());
            });
        }

        scene.idle(10);
        scene.addKeyframe();
        scene.idle(20);

        scene.overlay().showText(55).text("当锅中的食材完全匹配配方后，动力臂会为汤锅盖上盖子").placeNearTarget();
        scene.idle(15);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().modifyBlockEntity(funnel.pos.below(), DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, new ItemStack(ModItems.STOCKPOT_LID.get()), 0);
        funnel.transferToBeltLike(Direction.SOUTH, new ItemStack(Items.BOWL));
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, new ItemStack(ModItems.STOCKPOT_LID.get()), 0);
        scene.idle(12);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, ItemStack.EMPTY, -1);
        pot.modifyBlock((s) -> s.setValue(StockpotBlock.HAS_LID, true), false);

        scene.idle(30);
        scene.addKeyframe();

        scene.idle(20);
        scene.overlay().showText(40).text("接下来只需等待一段时间...").placeNearTarget();
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(3,3,2) , Direction.UP), Pointing.DOWN, 35).withItem(new ItemStack(Items.CLOCK));
        scene.idle(55);

        scene.addKeyframe();
        scene.idle(20);

        pot.hideSection(Direction.UP);
        stove.hideSection(Direction.UP);
        scene.idle(15);
        pot1.showSection(Direction.DOWN);
        stove1.showSection(Direction.DOWN);
        armOutput.showSection(Direction.DOWN);
        cogWheel3.showSection(Direction.DOWN);

        scene.idle(20);

        //scene.overlay().showOutline(PonderPalette.INPUT, pot1.selection(), pot1.selection(), 25);
        scene.overlay().showText(25).text("输入").colored(PonderPalette.INPUT).pointAt(pot1.blockSurface(Direction.WEST)).placeNearTarget();

        scene.idle(35);

        scene.addKeyframe();

        scene.idle(20);

        pot1.modifyBlockEntity(StockpotBlockEntity.class, (be) -> {
            StockpotBlockEntityAccessor accessor = (StockpotBlockEntityAccessor) be;
            accessor.invokeApplyRecipe(level, be.getContainer(), level.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE, ResourceLocation.parse("kaleidoscope_cookery:stockpot/braised_beef_with_potatoes")));
            accessor.setStatus(3);
            be.refresh();
        });

        scene.overlay().showText(55).text("待烹饪完成，以汤锅为输入点的动力臂会揭下锅盖").placeNearTarget();
        scene.idle(15);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().instructArm(armOutput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, new ItemStack(ModItems.STOCKPOT_LID.get()), -1);
        pot1.modifyBlock((s) -> s.setValue(StockpotBlock.HAS_LID, false), false);

        scene.idle(50);

        scene.addKeyframe();

        scene.idle(20);

        scene.overlay().showText(95).text("最后，以汤锅为输出点的动力臂会用容器从汤锅中盛取成品 (盛具数量必须为1或与锅中剩余份数相同)").placeNearTarget();
        scene.idle(15);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(12);
        scene.world().modifyBlockEntity(funnel.pos.below(), DepotBlockEntity.class, (be) -> be.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, new ItemStack(Items.BOWL), 0);
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, new ItemStack(Items.BOWL), 1);
        scene.idle(12);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, new ItemStack(ModItems.BRAISED_BEEF_WITH_POTATOES.get()), -1);
        pot1.setBlock(Blocks.AIR.defaultBlockState(), false);
        pot1.setBlock(ModBlocks.STOCKPOT.get().defaultBlockState(), false);
        scene.idle(25);
        scene.overlay().showControls(util.vector().blockSurface(armInput.pos.above(), Direction.UP), Pointing.DOWN, 15).rightClick();
        scene.idle(5);
        scene.world().instructArm(armInput.pos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(armInput.pos.above(), Direction.UP), Pointing.DOWN, 15).withItem(new ItemStack(ModItems.BRAISED_BEEF_WITH_POTATOES.get()));
        scene.idle(40);
    }
}
