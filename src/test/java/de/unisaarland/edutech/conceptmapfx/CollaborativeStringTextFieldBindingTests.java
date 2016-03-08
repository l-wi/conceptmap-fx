package de.unisaarland.edutech.conceptmapfx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CollaborativeStringTextFieldBindingTests {

	@Test
	public void testCollaborativeStringTextFieldBinding() {
		// given
		User u = new User("Tim", "tim@localhost.de");
		CollaborativeString source = new CollaborativeString(u, "hallo ");
		StringProperty dest = new SimpleStringProperty();

		// when
		CollaborativeStringTextFieldBinding b = CollaborativeStringTextFieldBinding.createBinding(source, dest);

		// then
		assertEquals(dest.get(), source.getContent());

	}

	@Test
	public void testAppend() {
		// given
		String hallo = "hallo ";
		String welt = "welt";

		User u = new User("Tim", "tim@localhost.de");
		CollaborativeString source = new CollaborativeString(u, hallo);
		StringProperty dest = new SimpleStringProperty();
		CollaborativeStringTextFieldBinding b = CollaborativeStringTextFieldBinding.createBinding(source, dest);

		String expected = hallo + welt;
		// when

		for (char c : welt.toCharArray())
			b.append(u, c);

		// then
		assertEquals(expected, source.getContent());
		assertEquals(source.getContent(), dest.get());
	}

	@Test
	public void testRemoveLast() {
		// given
		String hallo = "hallo ";
		String expected = "ha";

		User u = new User("Tim", "tim@localhost.de");
		CollaborativeString source = new CollaborativeString(u, hallo);
		StringProperty dest = new SimpleStringProperty();
		CollaborativeStringTextFieldBinding b = CollaborativeStringTextFieldBinding.createBinding(source, dest);

		// when

		for (int i = 0; i < hallo.length() - expected.length(); i++)
			b.removeLast();

		// then
		assertEquals(expected, source.getContent());
		assertEquals(source.getContent(), dest.get());
		
	}
	@Test
	public void testRemoveLastOnEmpty() {
		// given
		String hallo = "";
		String expected = "";

		User u = new User("Tim", "tim@localhost.de");
		CollaborativeString source = new CollaborativeString(u, hallo);
		StringProperty dest = new SimpleStringProperty();
		CollaborativeStringTextFieldBinding b = CollaborativeStringTextFieldBinding.createBinding(source, dest);

		// when

		b.removeLast();
		b.removeLast();
		b.removeLast();

		// then
		assertEquals(expected, source.getContent());
		assertEquals(source.getContent(), dest.get());
		
	}
}
