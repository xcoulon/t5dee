The org.eclipse.wst.common.frameworks.ui bundle is required.
It provides an extension to uiTester (provided by bundle org.eclipse.jem.util)
Without this bundle, Eclipse is considered to run in headless mode, which means that the
UI Context Sensitive Class 'org.eclipse.wst.xml.core.internal.emf2xml.EMF2DOMSSERendererFactory' 
is not loaded in bundle org.eclipse.wst.xml.core. 
In turn, the default renderer for web components is an instance of EMF2DOMRenderer instead of an 
instance of EMF2DOMSSERenderer (see WebAppResourceFactory.rendererFactory), 
which in the end fails to load the web.xml resource file...  