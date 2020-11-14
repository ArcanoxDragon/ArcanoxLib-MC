package me.arcanox.lib.client.util.extensions

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import me.arcanox.lib.util.LazyCache
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraftforge.client.model.data.EmptyModelData
import java.util.*


/**
 * Renders this IBakedModel using the provided parameters with basic lighting and color
 */
fun IBakedModel.renderSimple(vertexBuilder: IVertexBuilder,
                             matrixStack: MatrixStack,
                             blockState: BlockState,
                             random: Random,
                             combinedLight: Int,
                             combinedOverlay: Int) =
	// Simply render a model with standard lighting and colors by iterating through its quads and adding them to the vertex builder
	this.getQuads(blockState, null, random, EmptyModelData.INSTANCE).forEach {
		vertexBuilder.addVertexData(matrixStack.last, it, 1f, 1f, 1f, combinedLight, combinedOverlay, false);
	}

/**
 * Renders this IBakedModel using the provided parameters with basic lighting and color
 */
fun LazyCache<IBakedModel>.renderSimple(vertexBuilder: IVertexBuilder,
                                        matrixStack: MatrixStack,
                                        blockState: BlockState,
                                        random: Random,
                                        combinedLight: Int,
                                        combinedOverlay: Int) =
	this.value.renderSimple(vertexBuilder, matrixStack, blockState, random, combinedLight, combinedOverlay);