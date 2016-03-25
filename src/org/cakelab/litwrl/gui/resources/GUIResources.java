package org.cakelab.litwrl.gui.resources;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cakelab.litwrl.gui.GUIUtils;


public class GUIResources {

	private static final String RESOURCES_PATH = "org/cakelab/litwrl/gui/resources/";
	
	public static final String APPLICATION_ICON = RESOURCES_PATH + "appicon.png";

	public static final String PANORAMA1_IMAGE = RESOURCES_PATH + "panorama-1.png";
	public static final String PANORAMA2_IMAGE = RESOURCES_PATH + "panorama-2.png";
	public static final String PANORAMA3_IMAGE = RESOURCES_PATH + "panorama-3.png";
	public static final String PANORAMA4_IMAGE = RESOURCES_PATH + "panorama-4.png";
	public static final String PANORAMA5_IMAGE = RESOURCES_PATH + "panorama-5.png";
	public static final String PANORAMA6_IMAGE = RESOURCES_PATH + "panorama-6.png";
	public static final String PANORAMA7_IMAGE = RESOURCES_PATH + "panorama-7.png";

	public static final String[] PANORAMA_IMAGES = new String[]{
		PANORAMA1_IMAGE,
		PANORAMA2_IMAGE,
		PANORAMA3_IMAGE,
		PANORAMA4_IMAGE,
		PANORAMA5_IMAGE,
		PANORAMA6_IMAGE,
		PANORAMA7_IMAGE
	};
	
	public static final String LOGO_200_IMAGE = RESOURCES_PATH + "logo-200h.png";
	
	public static final String FOLDER_ICON = RESOURCES_PATH + "folder.png";

	public static final String CREDITS_PAGE = RESOURCES_PATH + "credits/credits.html";

	public static final String GUIDE_PAGE = RESOURCES_PATH + "doc/doc.html";

	public static final String NEWS_PAGE = RESOURCES_PATH + "news/news.html";

	
	
	public static InputStream asInputStream(String resource) {
		return 	GUIUtils.class.getClassLoader().getResourceAsStream(resource);
	}
	public static Image asImage(String resource) throws IOException {
		return ImageIO.read(asInputStream(resource));
	}
	public static Icon asIcon(String resource) throws IOException {
		return new ImageIcon(asImage(resource));
	}
	public static URL getURL(String resource) {
		return GUIResources.class.getClassLoader().getResource(resource);
	}
	public static String randomPanoramaImage() {
		Random rng = new Random();
		int i = rng.nextInt(PANORAMA_IMAGES.length);
		return PANORAMA_IMAGES[i];
	}
}
