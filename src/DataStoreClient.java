import ProxyServer.ProxyServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class DataStoreClient {
    private final ArrayList<String> initialRequests;
    private final ArrayList<String> servers;

    public DataStoreClient() {
        this.servers = new ArrayList<>();
        this.initialRequests = new ArrayList<>();
        this.populateInitialRequests();
        this.populateServers();
    }

    /**
     * method to send initial requests to the server. Perform 5 PUT, 5 GET and 4 DELETE operations
     */
    private void populateInitialRequests() {
        this.initialRequests.add("PUT 2 3");
        this.initialRequests.add("PUT 3 4");
        this.initialRequests.add("PUT 4 5");
        this.initialRequests.add("PUT 5 6");
        this.initialRequests.add("PUT 6 7");
        this.initialRequests.add("GET 2");
        this.initialRequests.add("GET 3");
        this.initialRequests.add("GET 4");
        this.initialRequests.add("GET 5");
        this.initialRequests.add("GET 6");
        this.initialRequests.add("DELETE 2");
        this.initialRequests.add("DELETE 3");
        this.initialRequests.add("DELETE 4");
        this.initialRequests.add("DELETE 5");
        this.initialRequests.add("DELETE 6");
    }

    private void populateServers() {
        this.servers.add("5000");
        this.servers.add("5010");
        this.servers.add("5020");
        this.servers.add("5030");
        this.servers.add("5040");
    }

    public ArrayList<String> getInitialRequests () {
        return this.initialRequests;
    }
    public ArrayList<String> getServers () { return this.servers; }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger("DataStoreClient");
        logger.info(new Timestamp(System.currentTimeMillis()) + " Client is up and running!");
        logger.info(new Timestamp(System.currentTimeMillis()) + " Sending initial requests");
        DataStoreClient client = new DataStoreClient();
        Random random = new Random();
        String assignedServer = null;

        // send initial requests
        for (String request: client.getInitialRequests()) {
            try {
                assignedServer = client.getServers().get(random.nextInt(client.getServers().size()));
                logger.info(new Timestamp(System.currentTimeMillis()) + " Sending request to server port: " + assignedServer);
                // access localhost at docker's host machine through host.docker.internal
                ProxyServer c = (ProxyServer) Naming.lookup("rmi://host.docker.internal:" + assignedServer + "/ProxyServer");
//                ProxyServer c = (ProxyServer) Naming.lookup("rmi://localhost:" + assignedServer + "/ProxyServer");
                String result = c.operate(request);
//                Note that to avoid these initial requests to overlap with each other (stuck in the same Paxos round) as much as possible,
//                thread will sleep for 15 second before sending out the next request to account for the acceptor failures.
//                Therefore, the initial requests take some time to finish
                Thread.sleep(15000);
                logger.info(new Timestamp(System.currentTimeMillis()) + result );
            }
            catch (MalformedURLException murle) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " MalformedURLException " + murle);
            }
            catch (RemoteException re) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " RemoteException " + re);
            }
            catch (NotBoundException nbe) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " NotBoundException " + nbe);
            }
            catch (Exception e) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " UnexpectedException " + e);
            }
        }

        // continue to receive following requests until forced to stop
        while (true) {
            try {
                System.out.println("Please enter a valid operation below:");
                System.out.println("----Valid operations include PUT (key value)/GET (key)/DELETE (key), e.g. PUT 2 3, GET 2, DELETE 2----");
                // retrieve user input
                DataInputStream input = new DataInputStream(System.in);
                String userInput = input.readLine();

                // randomly select a server
                assignedServer = client.getServers().get(random.nextInt(client.getServers().size()));
//                ProxyServer c = (ProxyServer) Naming.lookup("rmi://localhost:" + assignedServer + "/ProxyServer");
                ProxyServer c = (ProxyServer) Naming.lookup("rmi://host.docker.internal:" + assignedServer + "/ProxyServer");

                String response = c.operate(userInput);
                System.out.println( response );
            }
            catch (MalformedURLException murle) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " MalformedURLException " + murle);
            }
            catch (RemoteException re) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " RemoteException " + re);
            }
            catch (NotBoundException nbe) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " NotBoundException " + nbe);
            }
            catch (IOException ioe) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " IOException " + ioe);
            }
            catch (Exception e) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception " + e);
            }
        }
    }
}
