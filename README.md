# wildfly-jms-example

Simple Java EE application that creates two JMS Producers invoked in Servlet and in EJB bean (by timer). Messages are received by Message driven bean and by standalone application, which is not included in this repo, but the code of the standalone app you can find in tests (see asyncProducerTest). Tests are done by Arquillian. Tests also contains test for failover testing, see backupTest.
