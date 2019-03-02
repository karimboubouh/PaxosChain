package core;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Global {

    public static List<Host> servers() throws UnknownHostException {
        List<Host> servers = new ArrayList<>();
        servers.add(new Host(1, 7501));
        servers.add(new Host(2, 7502));
        servers.add(new Host(3, 7503));
        return servers;
    }
    public static List<Host> clients() throws UnknownHostException {
        List<Host> clients = new ArrayList<>();
        clients.add(new Host(1, 8501));
        clients.add(new Host(2, 8502));
        clients.add(new Host(3, 8503));
        return clients;
    }

}
