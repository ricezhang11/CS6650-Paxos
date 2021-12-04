package Learner;

import Utility.Response;

import java.rmi.RemoteException;

public interface Learner extends java.rmi.Remote {
    Response commit(String operation) throws RemoteException;
}
