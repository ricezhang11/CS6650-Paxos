package ProxyServer;

import Acceptor.*;
import Learner.Learner;
import Utility.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ProxyServerImpl
 */
public class ProxyServerImpl extends java.rmi.server.UnicastRemoteObject implements ProxyServer {
    String port;
    List<String> acceptorAddresses;
    List<String> learnerAddresses;
    Logger logger = Logger.getLogger("ProxyServerImpl");

    public ProxyServerImpl(String port) throws java.rmi.RemoteException {
        super();
        this.port = port;
        this.initializeAcceptorAddresses();
        this.initializeLearnerAddresses();
    }

    private void initializeAcceptorAddresses() {
        this.acceptorAddresses = new ArrayList<>();
        // add all addresses into the arraylist
//        this.acceptorAddresses.add("rmi://localhost:5000/Acceptor");
//        this.acceptorAddresses.add("rmi://localhost:5010/Acceptor");
//        this.acceptorAddresses.add("rmi://localhost:5020/Acceptor");
//        this.acceptorAddresses.add("rmi://localhost:5030/Acceptor");
//        this.acceptorAddresses.add("rmi://localhost:5040/Acceptor");

        this.acceptorAddresses.add("rmi://host.docker.internal:5000/Acceptor");
        this.acceptorAddresses.add("rmi://host.docker.internal:5010/Acceptor");
        this.acceptorAddresses.add("rmi://host.docker.internal:5020/Acceptor");
        this.acceptorAddresses.add("rmi://host.docker.internal:5030/Acceptor");
        this.acceptorAddresses.add("rmi://host.docker.internal:5040/Acceptor");
    }

    private void initializeLearnerAddresses() {
        this.learnerAddresses = new ArrayList<>();
        // add all addresses into the arraylist
//        this.learnerAddresses.add("rmi://localhost:5000/Learner");
//        this.learnerAddresses.add("rmi://localhost:5010/Learner");
//        this.learnerAddresses.add("rmi://localhost:5020/Learner");
//        this.learnerAddresses.add("rmi://localhost:5030/Learner");
//        this.learnerAddresses.add("rmi://localhost:5040/Learner");

        this.learnerAddresses.add("rmi://host.docker.internal:5000/Learner");
        this.learnerAddresses.add("rmi://host.docker.internal:5010/Learner");
        this.learnerAddresses.add("rmi://host.docker.internal:5020/Learner");
        this.learnerAddresses.add("rmi://host.docker.internal:5030/Learner");
        this.learnerAddresses.add("rmi://host.docker.internal:5040/Learner");
    }

    /**
     * This method is called by the client to pass into the client request
     * @param input the client request
     * @return a String, which is the response from the server
     * @throws IOException
     * @throws NotBoundException
     */
    @Override
    public String operate(String input) throws IOException, NotBoundException, InterruptedException {
        return this.initiatePaxos(input);
    }

    /**
     * this is the "proposer" role, which is a private method that is not accessible to the clients
     * @param input the client input
     * @return a String, which is the client response
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    private String initiatePaxos(String input) throws IOException, NotBoundException, InterruptedException {
        // use timestamp in the milliseconds as the sequence number. This way, the sequence number is ever increasing and each proposer's timestamp should be unique
        int quorum = (this.acceptorAddresses.size() / 2) + 1;
        // this is to count how many votes the proposer got in the prepare phase
        int voteForPrepare = 0;
        // this is to count how many votes the proposer got in the accept phase
        int voteForAccept = 0;
        // this is the new sequence number the proposer proposed
        long newSequenceNumber = 0;
        // this is to record the highest sequence number the acceptors returned in the prepare phase
        long highestSequenceNumber = 0;
        // this is to record the value in the highest numbered proposal the acceptors returned in the prepare phase
        String valueOfHighestNumberedProposal = "";
        long acceptedSequenceNumber = 0;
        // this is to record the final agreed on value
        String acceptedValue = "";


        // If the proposer didn't get a quorum in either the prepare phase or the accept phase, it'll keep trying.
        // This is assuming that if the proposer keeps trying and it will eventually get its turn. The eventual agreed on value
        // might be not the original client request, but will be something that all servers agreed on and successfully committed
        while (voteForPrepare < quorum || voteForAccept < quorum) {
            logger.info(new Timestamp(System.currentTimeMillis()) + " Initiating a new round of Paxos for client input " + input);
            // I used the system timestamp in millisecond as the sequence number;
            newSequenceNumber = System.currentTimeMillis();
            // reset all values for each new round of Paxos
            highestSequenceNumber = 0;
            valueOfHighestNumberedProposal = "";
            acceptedValue = "";
            voteForPrepare = 0;
            voteForAccept = 0;

            // start with the prepare phase
            try {
                for (String address : this.acceptorAddresses) {
                    Acceptor a = (Acceptor) Naming.lookup(address);
                    Promise response = null;
                    logger.info(new Timestamp(System.currentTimeMillis()) + " About to send prepare to the acceptor at address: " + address);
                    response = a.prepare(newSequenceNumber);
                    if (response != null) {
                        voteForPrepare += 1;
                        if (response.getSequenceNumber() > highestSequenceNumber) {
                            valueOfHighestNumberedProposal = response.getValue();
                            highestSequenceNumber = response.getSequenceNumber();
                        }
                    }
                }
            } catch (Exception e) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception happened:" + e);
//                throw e;
            }

            // if the proposer didn't get the quorum, don't move forward and retry the request
            if (voteForPrepare < quorum) {
                logger.info(new Timestamp(System.currentTimeMillis()) + " Couldn't achieve quorum for client request: " + input);
                continue;
            }

            logger.info(new Timestamp(System.currentTimeMillis()) + " Number of promise received in the prepare phase: " + voteForPrepare + "for client request: " + input);

            try {
                // if the proposer has the quorum, enter the accept phase
                for (String address : this.acceptorAddresses) {
                    Acceptor a = (Acceptor) Naming.lookup(address);
                    boolean result;
                    // if none of the acceptors returns any previous proposals, use the client request
                    if (highestSequenceNumber == 0 && valueOfHighestNumberedProposal.equals("")) {
                        result = a.accept(newSequenceNumber, input);
                        // otherwise, set the proposal to previously accepted value
                    } else {
                        result = a.accept(newSequenceNumber, valueOfHighestNumberedProposal);
                    }

                    if (result) {
                        voteForAccept += 1;
                    }
                }
            } catch (Exception e) {
                logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception happened:" + e);
            }


            if (voteForAccept >= quorum) {
                if (highestSequenceNumber == 0 && valueOfHighestNumberedProposal.equals("")) {
                    acceptedValue = input;
                } else {
                    acceptedValue = valueOfHighestNumberedProposal;
                }
                logger.info(new Timestamp(System.currentTimeMillis()) + " The proposer got " + voteForAccept + " votes from the acceptors for the accepted value: " + acceptedValue + ". The original client request is: " + input);
            } else {
                logger.info(new Timestamp(System.currentTimeMillis()) + " Got " + voteForAccept + "votes and couldn't achieve a quorum for the proposal; Go ahead and initiate another round of Paxos for client request: " + input);
            }
        }

        // the proposer asks all learners to perform the operation, gets their response and returns that to the client
        Response response = null;
        for (String address : this.learnerAddresses) {
            Learner l = (Learner) Naming.lookup(address);
            response = l.commit(acceptedValue);
        }

        logger.info(new Timestamp(System.currentTimeMillis()) + " Response from the learner is " + response.serialize());

        try {
            // reset the acceptors and let them know this round of Paxos is done
            for (String address : this.acceptorAddresses) {
                Acceptor a = (Acceptor) Naming.lookup(address);
                a.reset();
            }
        } catch (Exception e) {
            logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception happened:" + e);
        }


        logger.info(new Timestamp(System.currentTimeMillis()) + " Finished resetting all acceptors' logs");

        return response.serialize();
    }
}
