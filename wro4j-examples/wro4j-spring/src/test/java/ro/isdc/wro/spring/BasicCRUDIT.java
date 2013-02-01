package ro.isdc.wro.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ro.isdc.wro.spring.model.MyEntity;
import ro.isdc.wro.spring.service.EntityService;

import com.force.sdk.connector.ForceServiceConnector;
import com.sforce.ws.ConnectionException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/app-context.xml")
public class BasicCRUDIT {

	@Inject
	private EntityService entityService;

	@Inject
	private ForceServiceConnector connector;

	@Test
	public void testEntityCRUD() {

		String entityId = null;

		try {
			// This little trick turns on traces for both the connector *and* the
			// entity manager because they share the same underlying connection object.
			// When trace is on, you'll see the SOAP requests and responses on stdout.
			connector.getConnection().getConfig().setTraceMessage(true);
			connector.getConnection().getConfig().setPrettyPrintXml(true);

			MyEntity entity = new MyEntity();
			entity.setName("A Test Entity");

			entityService.save(entity);

			assertNotNull(entity.getId());
			entityId = entity.getId();

			entity = entityService.findEntity(entityId);

			assertEquals("A Test Entity", entity.getName());

			entity.setName("A Modified Test Entity");

			entityService.save(entity);

			entity = entityService.findEntity(entityId);

			assertEquals("A Modified Test Entity",entity.getName());

			entityService.delete(entityId);

			entity = entityService.findEntity(entityId);

			assertNull(entity);

			// In case the entity is not null, the finally block will try to clean up using
			// the native connection
			if(entity==null) {
        entityId = null;
      }


		} catch (final ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if(entityId!=null) {
				try {

					connector.getConnection().delete(new String[] {entityId });
				} catch (final ConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
