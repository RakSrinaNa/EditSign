package fr.raksrinana.editsign.config;

import fr.raksrinana.editsign.config.validator.ItemId;
import fr.raksrinana.editsign.config.validator.ItemIdRunner;
import fr.raksrinana.editsign.config.validator.ValidatorRunner;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Gui.Tooltip;
import me.sargunvohra.mcmods.autoconfig1u.gui.registry.GuiRegistry;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import static fr.raksrinana.editsign.EditSignUtils.getAsItems;
import static java.util.stream.Collectors.toList;

@Config(name = "editsign")
public class Configuration implements ConfigData{
	@ConfigEntry.Gui.Excluded
	private static final ItemIdRunner ITEM_ID_RUNNER = new ItemIdRunner();
	@ConfigEntry.Gui.Excluded
	private static final List<ValidatorRunner<?>> RUNNERS = Collections.singletonList(ITEM_ID_RUNNER);
	@Tooltip(count = 4)
	@Comment("Required item to edit signs")
	@ItemId(allowEmpty = true)
	public String requiredItemId = "";
	
	public static Configuration register(){
		Configuration configuration = AutoConfig.register(Configuration.class, JanksonConfigSerializer::new).getConfig();
		
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
			registerGui();
		}
		return configuration;
	}
	
	@Environment(EnvType.CLIENT)
	private static void registerGui(){
		GuiRegistry registry = AutoConfig.getGuiRegistry(Configuration.class);
		
		RUNNERS.forEach(runner -> registry.registerAnnotationTransformer((guis, i13n, field, config, defaults, guiProvider) -> guis.stream()
				.peek(gui -> gui.setErrorSupplier(() -> runner.apply(gui.getValue(), field)))
				.collect(toList()), runner.getAnnotationClass()));
	}
	
	@Override
	public void validatePostLoad() throws ValidationException{
		runValidators(Configuration.class, this, "general");
	}
	
	private static <T> void runValidators(Class<T> categoryClass, T category, String categoryName) throws ValidationException{
		try{
			for(Field field : categoryClass.getDeclaredFields()){
				for(ValidatorRunner<?> validator : RUNNERS){
					if(!validator.validateIfAnnotated(field, category)){
						throw new ValidationException("EditSign config field " + categoryName + "." + field.getName() + " is invalid");
					}
				}
			}
		}
		catch(ReflectiveOperationException | RuntimeException e){
			throw new ValidationException(e);
		}
	}
	
	public Collection<Item> getRequiredItem(){
		return getAsItems(requiredItemId);
	}
}