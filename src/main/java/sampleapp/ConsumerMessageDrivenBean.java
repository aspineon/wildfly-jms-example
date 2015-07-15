package sampleapp;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.logging.Logger;

/**
 * Created by eduda on 13.7.2015.
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                propertyValue = "java:jboss/exported/jms/queue/AsyncQueue"),
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "javax.jms.Queue")
})
public class ConsumerMessageDrivenBean implements MessageListener {

    private static final Logger logger = Logger.getLogger(ConsumerMessageDrivenBean.class.getName());

    @Inject
    private Counter counter;

    @Override
    public void onMessage(Message message) {
        logger.info("onMessage");
        counter.setCount(counter.getCount() + 1);
    }
}
