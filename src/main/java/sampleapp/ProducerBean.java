package sampleapp;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;
import java.util.logging.Logger;

/**
 * Created by eduda on 13.7.2015.
 */
@Singleton
@Startup
public class ProducerBean {

    private static final Logger logger = Logger.getLogger(ProducerBean.class.getName());

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:jboss/exported/jms/queue/SyncQueue")
    private Queue queue;

    @Schedule(second = "*/10", minute = "*", hour = "*")
    public void onShedule() {
        logger.info("onShedule");
        try (JMSContext context = connectionFactory.createContext()) {
            context.createProducer().send(queue, context.createMessage());
        }
    }

}
