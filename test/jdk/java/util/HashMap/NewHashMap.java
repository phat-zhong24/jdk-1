/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.Assert.*;

/*
 * @test
 * @bug 8285405
 * @summary Tests HashMap.newHashMap(int) method
 * @run testng NewHashMap
 */
public class NewHashMap {
    private static final Class<IllegalArgumentException> IAE = IllegalArgumentException.class;

    @DataProvider(name = "negatives")
    private static Object[][] negatives() {
        return new Object[][]{new Object[]{-1},
                new Object[]{Integer.MIN_VALUE},
                new Object[]{-42}};
    }

    @DataProvider(name = "nonNegatives")
    private static Object[][] nonNegatives() {
        return new Object[][]{new Object[]{0},
                new Object[]{42}};
    }

    /**
     * Test the {@link HashMap#newHashMap(int)} method with negative values
     * passed for the {@code numMappings} parameter
     */
    @Test(dataProvider = "negatives")
    public void testNewHashMapNegative(final int val) {
        assertThrows(IAE, () -> HashMap.newHashMap(val));
    }

    /**
     * Test the {@link HashMap#newHashMap(int)} method with zero and positive values
     * passed for the {@code numMappings} parameter
     */
    @Test(dataProvider = "nonNegatives")
    public void testNewHashMapNonNegative(final int val) {
        var h = HashMap.newHashMap(val);
        assertNotNull(h);
        assertEquals(h.size(), 0, "Unexpected size of HashMap created with numMappings: " + val);
    }
}