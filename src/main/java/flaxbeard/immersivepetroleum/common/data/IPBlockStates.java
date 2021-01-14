package flaxbeard.immersivepetroleum.common.data;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader;
import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.blocks.GasGeneratorBlock;
import flaxbeard.immersivepetroleum.common.util.fluids.IPFluid;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;

public class IPBlockStates extends BlockStateProvider{
	/** ResourceLocation("forge","obj") */
	private static final ResourceLocation FORGE_LOADER=new ResourceLocation("forge","obj");
	
	final IPLoadedModels loadedModels;
	final ExistingFileHelper exFileHelper;
	public IPBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper, IPLoadedModels loadedModels){
		super(gen, ImmersivePetroleum.MODID, exFileHelper);
		this.loadedModels=loadedModels;
		this.exFileHelper=exFileHelper;
	}
	
	@Override
	protected void registerStatesAndModels(){
		// Dummy Oil Ore
		ModelFile dummyOilOreModel=cubeAll(IPContent.Blocks.dummyOilOre);
		getVariantBuilder(IPContent.Blocks.dummyOilOre).partialState()
			.setModels(new ConfiguredModel(dummyOilOreModel));
		itemModelWithParent(IPContent.Blocks.dummyOilOre, dummyOilOreModel);
		
		// Dummy Pipe
		ModelFile dummyPipeModel=getExistingFile(modLoc("block/dummy_pipe"));
		getVariantBuilder(IPContent.Blocks.dummyPipe).partialState()
			.setModels(new ConfiguredModel(dummyPipeModel));
		itemModelWithParent(IPContent.Blocks.dummyPipe, dummyPipeModel);
		
		// Dummy Conveyor
		ModelFile dummyConveyorModel=getExistingFile(modLoc("block/dummy_conveyor"));
		getVariantBuilder(IPContent.Blocks.dummyConveyor).partialState()
			.setModels(new ConfiguredModel(dummyConveyorModel));
		getItemBuilder(IPContent.Blocks.dummyConveyor)
			.parent(dummyConveyorModel)
			.texture("particle", new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/conveyor"));
		
		// Multiblocks
		distillationtower();
		pumpjack();
		
		// "Normal" Blocks
		simpleBlockWithItem(IPContent.Blocks.asphalt);
		gasGenerator();
		
		autolubricator();
		
		// Fluids
		for(IPFluid f:IPFluid.LIST){
			ResourceLocation still=f.getAttributes().getStillTexture();
			ModelFile model = getBuilder("block/fluid/"+f.getRegistryName().getPath()).texture("particle", still);
			
			getVariantBuilder(f.block).partialState().setModels(new ConfiguredModel(model));
		}
		
		loadedModels.backupModels();
	}
	
	private void distillationtower(){
		ResourceLocation idleTexture=modLoc("multiblock/distillation_tower");
		ResourceLocation modelNormal=modLoc("models/multiblock/obj/distillationtower.obj");
		ResourceLocation modelMirrored=modLoc("models/multiblock/obj/distillationtower_mirrored.obj");
		
		LoadedModelBuilder normal=multiblockModel(IPContent.Multiblock.distillationtower, modelNormal, idleTexture, "_idle");
		LoadedModelBuilder mirrored=multiblockModel(IPContent.Multiblock.distillationtower, modelMirrored, idleTexture, "_mirrored_idle");
		
		createMultiblock(IPContent.Multiblock.distillationtower, normal, mirrored, idleTexture);
	}
	
	private void pumpjack(){
		ResourceLocation texture=modLoc("multiblock/pumpjack_base");
		ResourceLocation modelNormal=modLoc("models/multiblock/obj/pumpjack.obj");
		ResourceLocation modelMirrored=modLoc("models/multiblock/obj/pumpjack_mirrored.obj");
		
		LoadedModelBuilder normal=multiblockModel(IPContent.Multiblock.pumpjack, modelNormal, texture, "");
		LoadedModelBuilder mirrored=multiblockModel(IPContent.Multiblock.pumpjack, modelMirrored, texture, "_mirrored");
		
		createMultiblock(IPContent.Multiblock.pumpjack, normal, mirrored, texture);
	}
	
	private LoadedModelBuilder multiblockModel(Block block, ResourceLocation model, ResourceLocation texture, String add){
		LoadedModelBuilder re=this.loadedModels.withExistingParent(getMultiblockPath(block)+add, mcLoc("block"))
				.texture("texture", texture)
				.texture("particle", texture)
				.additional("flip-v", true)
				.additional("model", model)
				.additional("detectCullableFaces", false)
				.loader(FORGE_LOADER);
		return re;
	}
	
	private void autolubricator(){
		ResourceLocation texture=modLoc("models/lubricator");
		
		BlockModelBuilder lube_empty=withExistingParent("lube_empty", new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_empty"))
				.texture("particle", texture);
		
		LoadedModelBuilder lubeModel=this.loadedModels.withExistingParent(getPath(IPContent.Blocks.auto_lubricator),
				mcLoc("block"))
				.loader(FORGE_LOADER)
				.additional("model", modLoc("models/block/obj/autolubricator.obj"))
				.additional("flip-v", true)
				.texture("texture", texture)
				.texture("particle", texture);
		
		VariantBlockStateBuilder lubeBuilder = getVariantBuilder(IPContent.Blocks.auto_lubricator);
		for(Direction dir:AutoLubricatorBlock.FACING.getAllowedValues()){
			int rot = (90 * dir.getHorizontalIndex()) + 90 % 360;
			
			lubeBuilder.partialState()
				.with(AutoLubricatorBlock.SLAVE, false)
				.with(AutoLubricatorBlock.FACING, dir)
				.setModels(new ConfiguredModel(lubeModel, 0, rot, false));
			
			lubeBuilder.partialState()
				.with(AutoLubricatorBlock.SLAVE, true)
				.with(AutoLubricatorBlock.FACING, dir)
				.setModels(new ConfiguredModel(lube_empty));
		}
	}
	
	private void gasGenerator(){
		JsonObject basemodel=new JsonObject();
		basemodel.addProperty("loader", "forge:obj");
		basemodel.addProperty("model", modLoc("models/block/obj/generator.obj").toString());
		basemodel.addProperty("flip-v", true);
		
		ResourceLocation texture=modLoc("block/obj/generator");
		LoadedModelBuilder model=loadedModels.getBuilder(getPath(IPContent.Blocks.gas_generator))
			.texture("texture", texture)
			.texture("particle", texture)
			.loader(ConnectionLoader.LOADER_NAME)
			.additional("base_model", basemodel)
			.additional("layers", Arrays.asList("CUTOUT", "SOLID"))
			;
		
		
		VariantBlockStateBuilder builder=getVariantBuilder(IPContent.Blocks.gas_generator);
		Direction.Plane.HORIZONTAL.forEach(dir->{
			int rotation = (90 * dir.getHorizontalIndex() + 90) % 360;
			
			builder.partialState()
				.with(GasGeneratorBlock.FACING, dir)
				.addModels(new ConfiguredModel(model, 0, rotation, false));
		});
	}
	
	/** Used basicly for every multiblock-block */
	private final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(
			new ExistingModelFile(new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_empty"), existingFileHelper)
	);
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} 
	 * @param idleTexture */
	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel, ResourceLocation particleTexture){
		createMultiblock(b, masterModel, mirroredModel, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, IEProperties.MIRRORED, 180, particleTexture);
	}
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} */
	private void createMultiblock(Block b, ModelFile masterModel, @Nullable ModelFile mirroredModel, IProperty<Boolean> isSlave, EnumProperty<Direction> facing, @Nullable IProperty<Boolean> mirroredState, int rotationOffset, ResourceLocation particleTex){
		Preconditions.checkArgument((mirroredModel == null) == (mirroredState == null));
		VariantBlockStateBuilder builder = getVariantBuilder(b);
		builder.partialState()
			.with(isSlave, true)
			.setModels(new ConfiguredModel(
					withExistingParent(getMultiblockPath(b)+"_empty", EMPTY_MODEL.model.getLocation())
					.texture("particle", particleTex)));
		
		boolean[] possibleMirrorStates;
		if(mirroredState != null)
			possibleMirrorStates = new boolean[]{false, true};
		else
			possibleMirrorStates = new boolean[1];
		for(boolean mirrored:possibleMirrorStates)
			for(Direction dir:facing.getAllowedValues()){
				final int angleY;
				final int angleX;
				if(facing.getAllowedValues().contains(Direction.UP)){
					angleX = -90 * dir.getYOffset();
					if(dir.getAxis() != Axis.Y)
						angleY = getAngle(dir, rotationOffset);
					else
						angleY = 0;
				}else{
					angleY = getAngle(dir, rotationOffset);
					angleX = 0;
				}
				
				ModelFile model = mirrored ? mirroredModel : masterModel;
				PartialBlockstate partialState = builder.partialState()
						.with(isSlave, false)
						.with(facing, dir);
				
				if(mirroredState != null)
					partialState = partialState.with(mirroredState, mirrored);
				
				partialState.setModels(new ConfiguredModel(model, angleX, angleY, true));
			}
	}
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} */
	private int getAngle(Direction dir, int offset){
		return (int) ((dir.getHorizontalAngle() + offset) % 360);
	}
	
	private String getMultiblockPath(Block b){
		return "multiblock/"+getPath(b);
	}
	
	private String getPath(Block b){
		return b.getRegistryName().getPath();
	}
	
	private void itemModelWithParent(Block block, ModelFile parent){
		getItemBuilder(block).parent(parent)
			.texture("particle", modLoc("block/"+getPath(block)));
	}
	
	private void simpleBlockWithItem(Block block){
		ModelFile file=cubeAll(block);
		
		getVariantBuilder(block).partialState()
			.setModels(new ConfiguredModel(file));
		itemModelWithParent(block, file);
	}
	
	private BlockModelBuilder getItemBuilder(Block block){
		return getBuilder(modLoc("item/"+getPath(block)).toString());
	}
}