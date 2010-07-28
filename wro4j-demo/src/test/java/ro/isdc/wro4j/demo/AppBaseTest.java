package ro.isdc.wro4j.demo;


import junit.framework.TestCase;

/**
 * local service test
 */
public class AppBaseTest extends TestCase {

    /**
     * setup env
     *
     * @throws Exception exception
     */
    @Override
    public void setUp() throws Exception {
//        ApiProxy.setEnvironmentForCurrentThread(new TestEnvironment());
//        ApiProxy.setDelegate(new ApiProxyLocalImpl(new File(".")) {
//        });
    }

    /**
     * cleanup
     *
     * @throws Exception exception
     */
    @Override
    public void tearDown() throws Exception {
//        ApiProxy.setDelegate(null);
//        ApiProxy.setEnvironmentForCurrentThread(null);
    }


}