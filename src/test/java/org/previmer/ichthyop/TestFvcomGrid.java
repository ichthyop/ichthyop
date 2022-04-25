package org.previmer.ichthyop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.previmer.ichthyop.dataset.FvcomDataset;
import org.junit.jupiter.api.BeforeAll;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class TestFvcomGrid extends SimulationManagerAccessor {

    private FvcomDataset dataset;

    @BeforeAll
    public void prepareData() throws Exception {
        String fileName = getClass().getClassLoader().getResource("fvcom/test-fvcom.xml").getFile();
        getSimulationManager().getParameterManager().setConfigurationFile(new File(fileName));
        dataset = new FvcomDataset();
        dataset.setUp();
    }

    @Test
    public void testDims() {
        assertEquals(16012, dataset.getNNodes());
        assertEquals(25019, dataset.getNTriangles());
    }

    @Test
    public void testBarycenter() {

        // testing dimensions
        assertEquals(25019, dataset.getXBarycenter().length);
        assertEquals(25019, dataset.getYBarycenter().length);

        double delta = 1;

        int i = 0;
        assertEquals(410871.8333333333, dataset.getXBarycenter(i), delta);
        assertEquals(4921664.6666666670, dataset.getYBarycenter(i), delta);

        i = 50;
        assertEquals(364483.3333333333, dataset.getXBarycenter(i));
        assertEquals( 5150171.0000000000, dataset.getYBarycenter(i));

        i = 10000;
        assertEquals(449900.8333333333, dataset.getXBarycenter(i), delta);
        assertEquals( 5420727.0000000000, dataset.getYBarycenter(i), delta);

        i = 20000;
        assertEquals(377839.7083333333, dataset.getXBarycenter(i), delta);
        assertEquals(5545715.3333333330, dataset.getYBarycenter(i), delta);

        i = 25000;
        assertEquals(496360.3333333333, dataset.getXBarycenter(i), delta);
        assertEquals(5231497.3333333330, dataset.getYBarycenter(i), delta);

    }

    @Test
    public void testNodes() {

        double[] xNodes = dataset.getXNodes();
        double[] yNodes = dataset.getYNodes();

        assertEquals(16012, xNodes.length);
        assertEquals(16012, yNodes.length);

        int i = 0;
        assertEquals(413209.8125, xNodes[i]);
        assertEquals(4916091.5, yNodes[i]);

        i = 100;
        assertEquals(383032.0312500000, xNodes[i]);
        assertEquals(5030811.0000000000, yNodes[i]);

        i = 500;
        assertEquals(331825.5937500000, xNodes[i]);
        assertEquals(5365191.0000000000, yNodes[i]);

        i = 5000;
        assertEquals(458287.4375000000, xNodes[i]);
        assertEquals(5405299.0000000000, yNodes[i]);

        i = 15000;
        assertEquals(516110.0000000000, xNodes[i]);
        assertEquals(5220771.0000000000, yNodes[i]);

    }

}
