package org.bytesparadise.t5dee.i18n;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("restriction")
public class MessagesTestCase {

	@Test
	public void testGetStringOk() {
		Assert.assertEquals("Wrong result", "T5dee", Messages.getString("Product.Name"));
	}
	
	@Test
	public void testGetStringKoNotFound() {
		Assert.assertEquals("Wrong result", "!invalid.key!", Messages.getString("invalid.key"));
	}
	
	@Test
	public void testGetStringKoNullParameter() {
		Assert.assertNull("Wrong result", Messages.getString(null));
	}

}
