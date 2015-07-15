package sampleapp;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by eduda on 13.7.2015.
 */
@WebServlet(urlPatterns = "/send")
public class ProducerServlet extends HttpServlet {

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:jboss/exported/jms/queue/AsyncQueue")
    private Queue queue;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try (JMSContext context = connectionFactory.createContext()) {
            context.createProducer().send(queue, context.createMessage());
        }

        resp.setContentType("text/html");
        try (PrintWriter out = resp.getWriter()) {
            out.print("<html><body><h1>");
            out.print("Message was sent.");
            out.print("</h1></body></html>");
        }

    }
}
