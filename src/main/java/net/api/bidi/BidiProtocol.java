package net.api.bidi;

import net.srv.Client;

public class BidiProtocol implements BidiMessagingProtocol<String>{
    private int connectionId;
    private ConnectionsImpl connections;
    private boolean terminate = false;
    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl) connections;
    }

    @Override
    public void process(String message) {
        String[] strings = message.split(" ");
        Client c;
        Short op = Short.parseShort(strings[0]);
        switch (op) {
            case 1: // register
                    if(connections.register(new Client(strings[1], strings[2], strings[3]))){
                        connections.send(connectionId,"10 1");
                    }
                    else{
                        connections.send(connectionId, "11 1");
                    }
                break;
            case 2: //Login
                if(strings[3] == "1"){
                    c = connections.getClient(strings[1]);
                    if(c == null || !c.login(strings[2])){
                        connections.send(connectionId, "11 2");
                    }
                    c.setConnectionID(connectionId);
                } else {
                    connections.send(connectionId, "11 2");
                }
                break;
            case 3: // logout
                c = connections.getClientByID(connectionId);
                if(c != null && c.logout()){
                    connections.send(connectionId,"10 3");
                    c.setConnectionID(-1);
                }
                connections.send(connectionId,"11 3");
                break;
            case 4: // follow/unfollow
                c = connections.getClientByID(connectionId);
                if(c!= null && c.isLoggedIn()){
                    boolean success;
                    if(strings[1] == "0") {
                        success = c.follow(connections.getClient(strings[2]));
                    } else{
                        success = c.unfollow(connections.getClient(strings[2]));
                    }
                    if(success){
                        connections.send(connectionId,"10 4 " + strings[2]);
                    } else {
                        connections.send(connectionId,"11 4");
                    }
                } else {
                    connections.send(connectionId,"11 4");
                }
                break;
            case 5: // post message
                //TODO save message to database on server
                c = connections.getClientByID(connectionId);
                if(c != null && c.isLoggedIn()){ //send to followers
                    c.incrementPost();
                    for(Client client : c.getFollowers()){
                        connections.send(client.getConnectionID(), 9 + " " + 1 + " " + c.getUsername() + " " + strings[1]);
                    }
                    int index = strings[1].indexOf("@");
                    while (index >= 0) { //send to @'s
                        String name = strings[1].substring(index + 1, strings[1].indexOf(" "));
                        Client client = connections.getClient(name);
                        connections.send(client.getConnectionID(),9 + " " + 1 + " " + c.getUsername() + " " + strings[1]);
                        index = strings[1].indexOf("@", strings[1].indexOf(" ")+1);
                    }
                } else{
                    connections.send(connectionId,"11 5");
                }
                break;
            case 6: //PM message
                String censured = strings[1]; //TODO censure
                c = connections.getClientByID(connectionId);
                Client recipient = connections.getClient(strings[1]);
                if(c != null && c.isLoggedIn() && c.isFollowing(recipient)){
                    connections.send(recipient.getConnectionID(),9 + " " + 0 + " " + c.getUsername() + " " + censured);
                }
                connections.send(connectionId,"11 6");
                break;
            case 7:
                c = connections.getClientByID(connectionId);
                if(c != null && c.isLoggedIn()) {
                    Client[] cl = connections.getLoggedInUsers();
                    String multiAck = "";
                    for (Client client : cl) {
                        multiAck+= "10 7 " + " " + client.getAge() + " " + client.getPosts() + " " + client.getNumFollowers() + " " + client.getNumFollowing() + "\0";
                    }
                    connections.send(connectionId,multiAck);
                } else{
                    connections.send(connectionId, "11 7");
                }
                break;
            case 8:
                String[] usernames = strings[1].split("\\|");
                c = connections.getClientByID(connectionId);
                if(c!=null && c.isLoggedIn()){
                    String multiAck= "";
                    for (String username: usernames) {
                        //TODO change according to reply in forum
                        Client client = connections.getClient(username);
                        if(client!= null){
                            multiAck+= "10 8 " + " " + client.getAge() + " " + client.getPosts() + " " + client.getNumFollowers() + " " + client.getNumFollowing() + "\0";
                        }
                        connections.send(connectionId,multiAck);
                    }
                } else {
                    connections.send(connectionId, "11 8");
                }
                break;
            case 12:
                Client client = connections.getClient(strings[1]);
                if(client != null){
                    connections.getClientByID(connectionId).block(client);
                } else {
                    connections.send(connectionId,"11 12");
                }
                break;
        }
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
