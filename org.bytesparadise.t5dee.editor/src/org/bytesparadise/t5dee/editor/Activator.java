package org.bytesparadise.t5dee.editor;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.bytesparadise.t5dee.editor";

	private Map<String, Image> images = new HashMap<String, Image>();

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		for(Entry<String, Image> entry : images.entrySet()) {
			entry.getValue().dispose();
			images.remove(entry.getKey());
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Return an image given its path within the bundle
	 * 
	 * @param path
	 * @return
	 */
	public Image getImage(String path) {
		if(!images.containsKey(path)) {
			URL url = getBundle().getResource(path);
			Image image = (Image)ImageDescriptor.createFromURL(url).createResource(Display.getDefault());
			images.put(path, image);
		} 
		return images.get(path); 
		
	}

}
