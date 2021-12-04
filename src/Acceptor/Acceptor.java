package Acceptor;

import Utility.Promise;

import java.io.IOException;
import java.rmi.RemoteException;

public interface Acceptor extends java.rmi.Remote {
    Promise prepare(long sequenceNum) throws IOException, RemoteException, InterruptedException;
    boolean accept(long sequenceNum, String value) throws IOException, RemoteException;
    void reset() throws IOException, RemoteException;
}
