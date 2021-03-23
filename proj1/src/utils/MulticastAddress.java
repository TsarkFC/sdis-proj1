package utils;

public class MulticastAddress {
    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    String address;
    Integer port;

    public MulticastAddress(String address, Integer port) {
        this.address = address;
        this.port = port;
    }
}
