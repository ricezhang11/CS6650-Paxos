import Acceptor.*;
import Learner.*;
import ProxyServer.*;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.sql.Timestamp;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class DataStoreServer {
    Logger logger = Logger.getLogger("DataStoreServer");
    Timer timer = new Timer();
    String port;

    /**
     * this inner class would allow tasks to fail randomly by unbinding/rebinding at random intervals
     */
    class Task extends TimerTask {
        @Override
        public void run() {
            int delay = (3 + new Random().nextInt(3)) * 1000;
            timer.schedule(new Task(), delay);
            logger.info(new Timestamp(System.currentTimeMillis()) + " Delay scheduled for " + delay + "at port " + port);
            try {
                Naming.unbind("rmi://localhost:" + port + "/Acceptor");
                logger.info(new Timestamp(System.currentTimeMillis()) + " Acceptor unbinded at port " + port);
                Acceptor a = new AcceptorImpl(port);
                // according to homework, resume after a short delay (set as 5 ms), can increase if needed
                Thread.sleep(5);
                Naming.rebind("rmi://localhost:" + port + "/Acceptor", a);
                logger.info(new Timestamp(System.currentTimeMillis()) + " Acceptor rebinded at port " + port);
            } catch (Exception e) {
                logger.info(new Timestamp(System.currentTimeMillis()) + " Trouble: " + e);
            }

        }
    }

    // initialize all remote objects and register them
    public DataStoreServer(String port) {
        try {
            int portNum = Integer.parseInt(port);
            this.port = port;
            ProxyServer proxyServer = new ProxyServerImpl(port);
            Acceptor acceptor = new AcceptorImpl(port);
            Learner learner = new LearnerImpl(port);
            LocateRegistry.createRegistry(portNum);
            Naming.rebind("rmi://localhost:" + port + "/ProxyServer", proxyServer);
            Naming.rebind("rmi://localhost:" + port + "/Acceptor", acceptor);
            Naming.rebind("rmi://localhost:" + port + "/Learner", learner);
        } catch (Exception e) {
            logger.warning(new Timestamp(System.currentTimeMillis()) + " Trouble: " + e);
        }
    }


    public static void main(String args[]) {
        String port = args[0];
        DataStoreServer server = new DataStoreServer(port);
        server.new Task().run();
    }
}
