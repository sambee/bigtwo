package com.sixlegs.bigtwo.data;

import junit.framework.*;

public class CombinationGeneratorTest
extends TestCase
{
    public CombinationGeneratorTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(CombinationGeneratorTest.class);
    }

    public void testSimple()
    {
        int[] source = new int[]{ 0, 1, 2 };
        CombinationGenerator cg = new CombinationGenerator(3, 2);
        assertEquals(3, cg.getTotal());
        assertEquals(3, cg.getNumLeft());
        while (cg.hasMore()) {
            int[] a = cg.getNext();
            assertEquals(2, a.length);
            // TODO: test returned values
            // System.err.println("[" + source[a[0]] + ", " + source[a[1]] + "]");
        }
    }
}
