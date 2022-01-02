package net.api.bidi;

import net.srv.Client;
import net.srv.ConnectionHandler;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl  implements Connections<String>{
    private static ConcurrentHashMap<String, Client> clients;
    private static ConcurrentHashMap<Integer, ConnectionHandler> connections;

    public ConnectionsImpl(){
        if(connections == null){
            connections = new ConcurrentHashMap<>();
        }
        if(clients == null){
            clients = new ConcurrentHashMap<>();
        }
    }

    @Override
    public boolean send(int connectionId, String msg) {
        return false;
    }

    @Override
    public void broadcast(String msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }
    public boolean register(Client client){
        if(clients.containsKey(client.getUsername())){
            return false;
        }
        clients.put(client.getUsername(), client);
        return true;
    }
    public Client getClient(String username){
        return clients.get(username);
    }
    public Client getClientByID(int id){
        for (Map.Entry<String,Client> username: clients.entrySet()) {
            if(id ==username.getValue().getConnectionID())
                return username.getValue();
        }
        return null;
    }
    public Client[] getLoggedInUsers(){
        LinkedList<Client> cl= new LinkedList<>();
        for (Map.Entry<String,Client> username: clients.entrySet()) {
            if(username.getValue().isLoggedIn())
                cl.add(username.getValue());
        }
        return cl.toArray(new Client[cl.size()]);
    }
}












