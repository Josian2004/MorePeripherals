package com.hakimen.peripherals.blocks.tile_entities;

import com.hakimen.peripherals.registry.BlockEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerInterfaceEntity extends BlockEntity {

    public BlockState spawner;
    public SpawnerBlockEntity entity;

    public SpawnerInterfaceEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegister.spawnerInterfaceEntity.get(), pos, state);
    }

    public void tick(){
        if(level.getBlockState(getBlockPos().above()).getBlock() instanceof SpawnerBlock){
            spawner = level.getBlockState(getBlockPos().above());
            entity = (SpawnerBlockEntity) level.getBlockEntity(getBlockPos().above());
            return;
        }
        else if(level.getBlockState(getBlockPos().east()).getBlock() instanceof SpawnerBlock){
            spawner = level.getBlockState(getBlockPos().east());
            entity = (SpawnerBlockEntity) level.getBlockEntity(getBlockPos().east());
            return;
        }
        else if(level.getBlockState(getBlockPos().west()).getBlock() instanceof SpawnerBlock){
            spawner = level.getBlockState(getBlockPos().west());
            entity = (SpawnerBlockEntity) level.getBlockEntity(getBlockPos().west());
            return;
        }
        else if(level.getBlockState(getBlockPos().south()).getBlock() instanceof SpawnerBlock){
            spawner = level.getBlockState(getBlockPos().south());
            entity = (SpawnerBlockEntity) level.getBlockEntity(getBlockPos().south());
            return;
        }
        else if(level.getBlockState(getBlockPos().north()).getBlock() instanceof SpawnerBlock){
            spawner = level.getBlockState(getBlockPos().north());
            entity = (SpawnerBlockEntity) level.getBlockEntity(getBlockPos().north());
            return;
        }
        else if(level.getBlockState(getBlockPos().below()).getBlock() instanceof SpawnerBlock){
            spawner = level.getBlockState(getBlockPos().below());
            entity = (SpawnerBlockEntity) level.getBlockEntity(getBlockPos().below());
            return;
        }else{
            spawner = null;
            entity = null;
        }
    }
}