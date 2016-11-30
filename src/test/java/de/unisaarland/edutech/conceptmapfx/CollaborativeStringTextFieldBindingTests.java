/*******************************************************************************
 * conceptmap-fx a concept mapping prototype for research.
 * Copyright (C) Tim Steuer (master's thesis 2016)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, US
 *******************************************************************************/
package de.unisaarland.edutech.conceptmapfx;

public class CollaborativeStringTextFieldBindingTests {

//	@Test
//	public void testCollaborativeStringTextFieldBinding() {
//		// given
//		User u = new User("Tim", "tim@localhost.de");
//		CollaborativeString source = new CollaborativeString(u, "hallo ");
//		StringProperty dest = new SimpleStringProperty();
//
//		// when
//		CollaborativeStringTextFieldBinding b = CollaborativeStringTextFieldBinding.createBinding(source, dest);
//
//		// then
//		assertEquals(dest.get(), source.getContent());
//
//	}
//
//	@Test
//	public void testAppend() {
//		// given
//		String hallo = "hallo ";
//		String welt = "welt";
//
//		User u = new User("Tim", "tim@localhost.de");
//		CollaborativeString source = new CollaborativeString(u, hallo);
//		StringProperty dest = new SimpleStringProperty();
//		CollaborativeStringTextFieldBinding b = CollaborativeStringTextFieldBinding.createBinding(source, dest);
//
//		String expected = hallo + welt;
//		// when
//
//		for (char c : welt.toCharArray())
//			b.append(u, c);
//
//		// then
//		assertEquals(expected, source.getContent());
//		assertEquals(source.getContent(), dest.get());
//	}
//
//	@Test
//	public void testRemoveLast() {
//		// given
//		String hallo = "hallo ";
//		String expected = "ha";
//
//		User u = new User("Tim", "tim@localhost.de");
//		CollaborativeString source = new CollaborativeString(u, hallo);
//		StringProperty dest = new SimpleStringProperty();
//		CollaborativeStringTextFieldBinding b = CollaborativeStringTextFieldBinding.createBinding(source, dest);
//
//		// when
//
//		for (int i = 0; i < hallo.length() - expected.length(); i++)
//			b.removeLast();
//
//		// then
//		assertEquals(expected, source.getContent());
//		assertEquals(source.getContent(), dest.get());
//		
//	}
//	@Test
//	public void testRemoveLastOnEmpty() {
//		// given
//		String hallo = "";
//		String expected = "";
//
//		User u = new User("Tim", "tim@localhost.de");
//		CollaborativeString source = new CollaborativeString(u, hallo);
//		StringProperty dest = new SimpleStringProperty();
//		CollaborativeStringTextFieldBinding b = CollaborativeStringTextFieldBinding.createBinding(source, dest);
//
//		// when
//
//		b.removeLast();
//		b.removeLast();
//		b.removeLast();
//
//		// then
//		assertEquals(expected, source.getContent());
//		assertEquals(source.getContent(), dest.get());
//		
//	}
}
