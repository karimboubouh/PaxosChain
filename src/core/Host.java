package core;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host implements Serializable {
    private int id;
    private int port;
    private InetAddress address;

    public Host(int id, int port) throws UnknownHostException {
        this.id = id;
        this.port = port;
        this.address = InetAddress.getByName("localhost");
    }

    public Host(int id, int port, String address) throws UnknownHostException {
        this.id = id;
        this.port = port;
        this.address = InetAddress.getByName(address);
    }

    public Host(int id, int port, InetAddress address) {
        this.id = id;
        this.port = port;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Host{" + id + ", " + port + '}';
    }
}
