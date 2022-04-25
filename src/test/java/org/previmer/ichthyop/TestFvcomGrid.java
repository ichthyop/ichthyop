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

}
