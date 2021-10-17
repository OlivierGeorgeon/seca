package gwt.seca.client;

import gwt.g3d.client.gl2.enums.TextureMagFilter;
import gwt.g3d.client.gl2.enums.TextureMinFilter;
import gwt.g3d.resources.client.ExternalMeshResource;
import gwt.g3d.resources.client.ExternalTexture2DResource;
import gwt.g3d.resources.client.MagFilter;
import gwt.g3d.resources.client.MinFilter;
import gwt.g3d.resources.client.ShaderResource;
import gwt.seca.client.IResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ClientBundle.Source;

/** Resource files. */
interface IResources extends ClientBundleWithLookup {
	IResources INSTANCE = GWT.create(IResources.class);

	@Source({"shaders/seca0.vp", "shaders/seca0.fp"})
	ShaderResource shader();
	
	//Stone group
	@Source("textures/stone_black.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource stoneBlack();
	@Source("textures/stone_grey.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource stoneGrey();
	@Source("textures/stone_green.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource stoneGreen();
	@Source("textures/stone_orange.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource stoneOrange();
	@Source("textures/stone_yellow.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource stoneYellow();
	
	//Dirt group
	
	//Sand group
	@Source("textures/sand.png")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource sand();
	
	//Grass group
	@Source("textures/grass_green.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource grassGreen();
	
	//Others for building blocks
	@Source("textures/crate.gif")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource crate();
	@Source("textures/bricks.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource bricks();
	
	//Plant group
	@Source("textures/stoplight_yellow.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource plantStoplightTexYellow();
	@Source("textures/stoplight_cyan.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource plantStoplightTexCyan();
	@Source("textures/stoplight_magenta.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource plantStoplightTexMagenta();
	@Source("textures/stoplight_red.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource plantStoplightTexRed();
	@Source("textures/stoplight_green.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource plantStoplightTexGreen();
	@Source("textures/stoplight_blue.jpg")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource plantStoplightTexBlue();
	@Source("models/stoplight.obj")
    ExternalMeshResource plantStoplightObj();
	
	//Animal group
	@Source("textures/fish.png")
	@MagFilter(TextureMagFilter.LINEAR)
	@MinFilter(TextureMinFilter.LINEAR)
	ExternalTexture2DResource fishTex();
	@Source("models/fish.obj")
    ExternalMeshResource fishObj();
}
