package fr.raksrinana.editsign;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SignItem;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = EditSign.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEventSubscriber{
	private static final String[] IS_EDITABLE_FIELDS = {
			"field_145916_j",
			"isEditable",
			};
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
		BlockPos pos = event.getPos();
		TileEntity tileentity = event.getWorld().getTileEntity(pos);
		if(tileentity instanceof SignTileEntity){
			PlayerEntity player = event.getPlayer();
			if(!player.isCrouching() && player instanceof ServerPlayerEntity){
				ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
				GameType gameMode = serverPlayerEntity.interactionManager.getGameType();
				if(gameMode == GameType.SURVIVAL || gameMode == GameType.CREATIVE){
					SignTileEntity sign = (SignTileEntity) tileentity;
					setSignEditable(sign);
					if(sign.getIsEditable()){
						player.openSignEditor(sign);
					}
					else{
						player.sendMessage(new TranslationTextComponent("edit_sign.action.not_editable"));
					}
				}
			}
		}
	}
	
	private static void setSignEditable(SignTileEntity sign){
		for(String field : IS_EDITABLE_FIELDS){
			try{
				ObfuscationReflectionHelper.setPrivateValue(SignTileEntity.class, sign, true, field);
				return;
			}
			catch(ObfuscationReflectionHelper.UnableToFindFieldException e){
				EditSign.LOGGER.debug("Failed to get field {}", field);
			}
		}
		EditSign.LOGGER.debug("Couldn't set sign editable");
	}
	
	private static boolean isHoldingEditor(PlayerEntity player){
		for(ItemStack stack : player.getHeldEquipment()){
			if(stack.getItem() instanceof SignItem){
				return true;
			}
		}
		return false;
	}
}
