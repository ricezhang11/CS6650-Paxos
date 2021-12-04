package Acceptor;

import Utility.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * The acceptor role
 */
public class AcceptorImpl extends java.rmi.server.UnicastRemoteObject implements Acceptor {
    String port;
    long highestSequenceNumberPromised;
    long acceptedSequenceNumber;
    String acceptedValue;
    private final String fileName_1;
    private final String fileName_2;
    Logger logger = Logger.getLogger("AcceptorImpl");

    public AcceptorImpl(String port) throws IOException {
        super();
        this.port = port;
        // the highest sequence number already promised to some proposer
        this.highestSequenceNumberPromised = 0;
        // the sequence number of the proposal last accepted
        this.acceptedSequenceNumber = 0;
        // the value of the proposal last accepted
        this.acceptedValue = "";
        this.fileName_1 = "highestSequenceNumberPromised" + this.port + ".txt";
        this.fileName_2 = "proposalAccepted" + this.port + ".txt";
        // when an acceptor starts/restarts, it need to create the files (if non-existent) and read existing values from the files
        this.createFiles();
        this.readValuesFromFiles();
    }

    /**
     * this private method is called from the constructor
     * this will create the new files (if first time started) and will log if not (acceptor crashed/stopped, then restarted)
     * @throws IOException
     */
    private void createFiles() throws IOException {
        // change to relative path so that TA can run this
        File highestSequenceNumberPromisedFile = new File(System.getProperty("user.dir") + "/Acceptor/" + this.fileName_1);
        File proposalAcceptedFile = new File(System.getProperty("user.dir") + "/Acceptor/" + this.fileName_2);

        if (highestSequenceNumberPromisedFile.createNewFile()) {
            logger.info(new Timestamp(System.currentTimeMillis()) + " highestSequenceNumberPromisedFile successfully created at port " + this.port);
        } else {
            logger.info(new Timestamp(System.currentTimeMillis()) + " highestSequenceNumberPromisedFile already exists at port " + this.port);
        };

        if (proposalAcceptedFile.createNewFile()) {
            logger.info(new Timestamp(System.currentTimeMillis()) + " proposalAcceptedFile successfully created at port " + this.port);
        } else {
            logger.info(new Timestamp(System.currentTimeMillis()) + " proposalAcceptedFile already exists at port " + this.port);
        };
    }

    /**
     * this private method is called in the constructor; Everytime an acceptor restarts,
     * the acceptor should read all 'highestSequenceNumberPromised' and 'acceptedProposals' from the files
     * in the synchronous mode
     */
    private void readValuesFromFiles() throws FileNotFoundException {
        // read the highest sequence number seen by the acceptor
        File highestSequenceNumberPromisedFile = new File(System.getProperty("user.dir") + "/Acceptor/" + this.fileName_1);
        Scanner fileReader = new Scanner(highestSequenceNumberPromisedFile);
        String data = null;

        if (fileReader.hasNext()) {
            data = fileReader.nextLine();
        }

        if (data != null) {
            List<String> list = Arrays.asList(data.split(";"));
            this.highestSequenceNumberPromised = Long.parseLong(list.get(list.size() - 1));
            logger.info(new Timestamp(System.currentTimeMillis()) + " highest sequence number promised is: " + this.highestSequenceNumberPromised + " at port " + this.port);
        } else {
            logger.info(new Timestamp(System.currentTimeMillis()) + " no sequence number seen yet at port " + this.port);
        }

        File proposalAcceptedFile = new File(System.getProperty("user.dir") + "/Acceptor/" + this.fileName_2);
        fileReader = new Scanner(proposalAcceptedFile);
        data = null;

        if (fileReader.hasNext()) {
            data = fileReader.nextLine();
        }

        if (data != null) {
            List<String> list = Arrays.asList(data.split(";"));
            this.acceptedSequenceNumber = Long.parseLong(list.get(list.size() - 1).split(",")[0]);
            this.acceptedValue = list.get(list.size() - 1).split(",")[1];
            logger.info(new Timestamp(System.currentTimeMillis()) + " last accepted sequence number is: " + this.acceptedSequenceNumber + " at port " + this.port);
            logger.info(new Timestamp(System.currentTimeMillis()) + " last accepted value is: " + this.acceptedValue + " at port " + this.port);
        } else {
            logger.info(new Timestamp(System.currentTimeMillis()) + " there was no last accepted proposal at port " + this.port);
        }
    }

    /**
     * the 'prepare' phase of the Paxos algorithm
     * @param sequenceNum of the proposer
     * @return a Promise if this is the highest sequence number seen by the acceptor; otherwise null
     * @throws IOException
     */
    @Override
    public Promise prepare(long sequenceNum) throws IOException, InterruptedException {
        logger.info(new Timestamp(System.currentTimeMillis()) + " At the beginning of prepare phase, highest sequence number promised: " + this.highestSequenceNumberPromised +
                " accepted proposal sequence number: " + this.acceptedSequenceNumber + "accepted proposal value: " + this.acceptedValue);
        // un-comment this line to see the effect of Paxos algorithm
        Thread.sleep(1000);
        try {
            if (sequenceNum > this.highestSequenceNumberPromised) {
                this.highestSequenceNumberPromised = sequenceNum;
                logger.info(new Timestamp(System.currentTimeMillis()) + " Prepare phase -- Received a new highest sequence number: " + this.highestSequenceNumberPromised);
            } else {
                logger.info(new Timestamp(System.currentTimeMillis()) + " Prepare phase -- Not the highest sequence number seen, reject");
                return null;
            }

            // Asynchronously write to the file
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            String toWrite = this.highestSequenceNumberPromised + ";";
            buffer.put(toWrite.getBytes());
            buffer.flip();

            AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/Acceptor/" + this.fileName_1), StandardOpenOption.WRITE);
            Future<Integer> future = asyncChannel.write(buffer, 0);
            logger.info(new Timestamp(System.currentTimeMillis()) + " Prepare phase -- writing to file");
            // if no previously accepted proposal, this will just be 0 and "". In the proposer's logic, this will be ignored and the proposer will proceed with client request
            return new Promise(this.acceptedSequenceNumber, this.acceptedValue);
        } catch (Exception e) {
            logger.warning(new Timestamp(System.currentTimeMillis()) + " Exception happened in acceptor: " + e);
            throw e;
        }
    }

    /**
     * the 'accept' phase of the Paxos algorithm
     * @param sequenceNum of the proposal
     * @param value of the proposal
     * @return true if the acceptor is ready to take the proposal; otherwise false
     * @throws IOException
     */
    @Override
    public boolean accept(long sequenceNum, String value) throws IOException {
        // if the proposal is equal or larger than the highest sequence number promised by the acceptor, accept the proposal
        if (sequenceNum >= this.highestSequenceNumberPromised) {
            this.acceptedSequenceNumber = sequenceNum;
            this.acceptedValue = value;

            // Asynchronously write to the file
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            String toWrite = this.acceptedSequenceNumber + "," + this.acceptedValue + ";";
            buffer.put(toWrite.getBytes());
            buffer.flip();

            AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/Acceptor/" + this.fileName_2), StandardOpenOption.WRITE);
            asyncChannel.write(buffer, asyncChannel.size());
            logger.info(new Timestamp(System.currentTimeMillis()) + " Accept phase -- accepted proposal: " + toWrite + " and writing to file");
            return true;
        } else {
            logger.info(new Timestamp(System.currentTimeMillis()) + " Accept phase -- rejected proposal; the highest sequence number promised is: " + this.highestSequenceNumberPromised);
            return false;
        }
    }

    /**
     * Acceptor will write the operation into its file; and reset all 'highestSequenceNumberPromised' and 'accepted' proposal records from the current round of Paxos
     */
    @Override
    public void reset() throws IOException {
        this.highestSequenceNumberPromised = 0;
        this.acceptedSequenceNumber = 0;
        this.acceptedValue = "";
        logger.info(new Timestamp(System.currentTimeMillis()) + " Reset --- values are reset and file is being emptied");
        // delete current round records
        AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/Acceptor/" + this.fileName_1), StandardOpenOption.WRITE).truncate(0).close();
        AsynchronousFileChannel.open(Path.of(System.getProperty("user.dir") + "/Acceptor/" + this.fileName_2), StandardOpenOption.WRITE).truncate(0).close();
    }
}
