package ProxyServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

// this is accessible to client, this is a proxy that takes client request and forward it on to the Proposer
public interface ProxyServer extends java.rmi.Remote{
    String operate(String input) throws IOException, NotBoundException, RemoteException, InterruptedException;
}
