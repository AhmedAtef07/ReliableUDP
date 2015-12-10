package logic;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Server extends PacketHandler {
  public Server(int udpPort) throws SocketException {
    super(new DatagramSocket(udpPort), "Server");
  }
}