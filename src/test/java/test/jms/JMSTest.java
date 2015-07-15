package test.jms;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import sampleapp.Counter;
import sampleapp.ProducerBean;
import sampleapp.ProducerServlet;

import javax.annotation.Resource;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Created by eduda on 14.7.2015.
 */
@RunWith(Arquillian.class)
public class JMSTest {

    private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String DEFAULT_DESTINATION = "java:/jms/queue/AsyncQueue";
    private static final String DEFAULT_USERNAME = "jmsuser";
    private static final String DEFAULT_PASSWORD = "jms123456";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL = "http-remoting://localhost:8080";

    @Deployment
    public static JavaArchive createDeploymnent() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(ProducerBean.class)
                .addClass(ProducerServlet.class)
                .addClass(Counter.class);
    }

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:jboss/exported/jms/queue/SyncQueue")
    private Queue syncQueue;

    @Test
    public void syncProducerTest() {

        JMSContext context = connectionFactory.createContext();

        try(JMSConsumer consumer = context.createConsumer(syncQueue)) {
            Message message = consumer.receive(12000);
            Assert.assertNotNull("Message was not received", message);
        }
    }

    @Test
    @RunAsClient
    public void asyncProducerTest(@ArquillianResource(ProducerServlet.class) URL baseUrl) throws IOException, NamingException {
        URL url = new URL(baseUrl, "./send");
        System.out.println(url.toExternalForm());

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();

        InputStream in = con.getInputStream();
        while (in.read() != -1);
        in.close();

        System.out.println("Response code: " + responseCode);

        InputStream inputStream = url.openStream();
        inputStream.close();

        ConnectionFactory connectionFactory = null;
        Destination destination = null;
        Context context = null;

        // Set up the context for the JNDI lookup
        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
        env.put(Context.SECURITY_PRINCIPAL, System.getProperty("username", DEFAULT_USERNAME));
        env.put(Context.SECURITY_CREDENTIALS, System.getProperty("password", DEFAULT_PASSWORD));
        context = new InitialContext(env);

        // Perform the JNDI lookups
        String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
        connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryString);

        String destinationString = System.getProperty("destination", DEFAULT_DESTINATION);
        destination = (Destination) context.lookup(destinationString);

        JMSContext jmsContext = connectionFactory.createContext(
                System.getProperty("username", DEFAULT_USERNAME),
                System.getProperty("password", DEFAULT_PASSWORD));

        try (JMSConsumer consumer = jmsContext.createConsumer(destination)) {
            Message message = consumer.receive(1000);
            Assert.assertNotNull("Message was not received", message);
        }
    }

    @Test
    @RunAsClient
    public void backupTest() throws InterruptedException, NamingException {
        final int MSG_COUNT = 10;
        ConnectionFactory connectionFactory = null;
        Destination destination = null;
        Context context = null;

        // Set up the context for the JNDI lookup
        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
        env.put(Context.SECURITY_PRINCIPAL, System.getProperty("username", DEFAULT_USERNAME));
        env.put(Context.SECURITY_CREDENTIALS, System.getProperty("password", DEFAULT_PASSWORD));
        context = new InitialContext(env);

        // Perform the JNDI lookups
        String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
        connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryString);

        String destinationString = System.getProperty("destination", DEFAULT_DESTINATION);
        destination = (Destination) context.lookup(destinationString);

        JMSContext jmsContext = connectionFactory.createContext(
                System.getProperty("username", DEFAULT_USERNAME),
                System.getProperty("password", DEFAULT_PASSWORD));

        for (int i = 0; i < MSG_COUNT; i++) {
            jmsContext.createProducer().send(destination, jmsContext.createMessage());
        }

        try (JMSConsumer consumer = jmsContext.createConsumer(destination)) {
            for (int i = 0; i < MSG_COUNT; i++) {
                try {
                    Message message = consumer.receive(2000);
                    Assert.assertNotNull("Message was not received", message);
                    System.out.println("Message " + i + " was received");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    i--;
                }
            }
        }
    }

}
