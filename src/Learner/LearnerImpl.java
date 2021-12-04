package Learner;

import Utility.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * The learner role, reused previous project code; this has the data store and all the operation logic that
 * comes with it, including PUT, GET, DELETE
 */
public class LearnerImpl extends java.rmi.server.UnicastRemoteObject implements Learner {
    String port;
    Store store;
    ExecutorService pool;
    Logger logger = Logger.getLogger("LearnerImpl");

    public LearnerImpl(String port) throws java.rmi.RemoteException {
        super();
        this.port = port;
        this.store = new Store();
        this.pool = Executors.newFixedThreadPool(5);
    }

    /**
     * multithreaded learner for committing the operations
     * @param operation the consensus result
     * @return a Response object that contains the result of the operation
     */
    @Override
    public Response commit(String operation) {
        // send initial requests
        Process myProcess = new Process(this.store, operation);
        Future future;
        // submit a task to the thread pool and this will return a Future object
        future = this.pool.submit(myProcess);
        // keep checking until the task is finished and then return the response to client
        for (;;) {
            if (future.isDone()) {
                // when task is finished, return the response object
                logger.info(operation);
                logger.info(myProcess.getResponse().serialize());
                return myProcess.getResponse();
            }
        }
    }
}

/**
 * This is the actual data store. The data is stored inside a hashmap. This class also defines
 * put, delete and get operations that can be performed on the hashmap.
 */
class Store {
    HashMap<String, String> storage;
    public Store() {
        this.storage = new HashMap<String, String>();
    }

    // put operation need to be synchronized
    public synchronized void put(String key, String value) {
        this.storage.put(key, value);
    }

    // delete operation need to be synchronized
    public synchronized void delete(String key) {
        this.storage.remove(key);
    }
    // get operation need to be synchronized
    public synchronized String get(String key) {
        return this.storage.get(key);
    }
}

/**
 * This is a class that implements the Runnable interface. It will run on a separate thread.
 * This class will take the client input and call the correct method to perform the operation
 * towards the data store.
 */
class Process implements Runnable {
    Store store;
    String input;
    // store the response as a class variable, so that we can return the responses to the client
    Response response;

    public Process (Store store, String str) {
        this.store = store;
        this.input = str;
        this.response = null;
    }

    public Response getResponse() {
        return this.response;
    }

    // check the validity of the input
    private boolean checkValidity(List<String> inputList) {
        List<String> validOperations = new ArrayList<>();
        validOperations.add("PUT");
        validOperations.add("GET");
        validOperations.add("DELETE");

        if (inputList.size() == 0 || !validOperations.contains(inputList.get(0))) {
            return false;
        } else if (inputList.get(0).equals("PUT") && inputList.size() == 3) {
            return true;
        } else if (inputList.get(0).equals("GET") && inputList.size() == 2) {
            return true;
        } else return inputList.get(0).equals("DELETE") && inputList.size() == 2;
    }

    // this method will perform the actual operations
    public void run() {
        List<String> inputList = Arrays.asList(this.input.split(" "));
        Logger logger = Logger.getLogger("DataStoreImpl");
        // first it checks the validity of the input
        if (this.checkValidity(inputList)) {
            String operation = inputList.get(0);
            String key = inputList.get(1);
            // perform PUT operation
            if (operation.equals("PUT")) {
                String value = inputList.get(2);
                this.store.put(key, value);
                this.response = new Response("200", operation, "success", key, this.store.get(key));
            } else {
                // check if the key exists
                if (!this.store.storage.containsKey(key)) {
                    logger.warning(new Timestamp(System.currentTimeMillis()) + " " + " client input contains invalid key; client input: " + this.input);
                    this.response = new Response("400", operation, "not_a_valid_key", key);
                } else {
                    // perform GET operation
                    if (operation.equals("GET")) {
                        this.response = new Response("200", operation, "success", key, this.store.get(key));
                        // perform DELETE operation
                    } else {
                        this.store.delete(key);
                        this.response = new Response("200", operation, "success", key);
                    }
                }
            }
            // take care of invalid operations
        } else {
            String operation = "NULL";
            if (inputList.size() >= 1) {
                operation = inputList.get(0);
            }
            logger.warning(new Timestamp(System.currentTimeMillis()) + " " + " unknown operation or malformed input; client input: " + this.input);
            this.response = new Response("400", operation, "Unknown_operation_or_malformed_input");
        }
    }
}

