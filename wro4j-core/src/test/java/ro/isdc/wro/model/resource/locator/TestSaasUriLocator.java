package ro.isdc.wro.model.resource.locator;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class TestSaasUriLocator {

    @Test
    public void testSaasImport() throws Exception {
        final String base = new File("./src/test/resources/sass/").getAbsolutePath() + "/";
        SaasUriLocator saasUriLocator = new SaasUriLocator();
        
        assertTrue(saasUriLocator.accept(base + "_sassimport"));
        assertTrue(saasUriLocator.accept(base + "style.scss"));
        assertFalse(saasUriLocator.accept(base + "style.css"));
        
        assertNotNull(saasUriLocator.locate(base + "_sassimport"));
        assertNotNull(saasUriLocator.locate(base + "style"));
        
        assertNotNull(saasUriLocator.locate("file:" + base + "style"));
        assertEquals(new File(base + "style.scss"), saasUriLocator.getScssFile("file:/" + base + "style"));
    }
}
