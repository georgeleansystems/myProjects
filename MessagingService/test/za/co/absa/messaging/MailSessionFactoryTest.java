package za.co.absa.messaging;

import javax.mail.Session;

import org.junit.Test;

import za.co.absa.messaging.MailSessionFactory;


public class MailSessionFactoryTest {
	
	@Test
	public void testGetInstance() throws Exception{
		
		MailSessionFactory instance = MailSessionFactory.getInstance();
		org.junit.Assert.assertNotNull(instance);
		
		Session session = instance.getSession();

		org.junit.Assert.assertNotNull(session);
	}

}
