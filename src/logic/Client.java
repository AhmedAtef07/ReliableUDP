package logic;

import java.io.IOException;
import java.net.*;
import java.util.Vector;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Client extends PacketHandler {
  private static int count = 0;

  private InetAddress serverAddress;
  private int port;
  private int id;

  private Vector<Packet> receivedPackets;

  public Client(String serverName, int port) throws SocketException, UnknownHostException {
    super(new DatagramSocket(), "Client");
    this.serverAddress = InetAddress.getByName(serverName);
    this.port = port;
    this.id = count++;
  }

  public void sendToServer(String s) throws IOException {
    Packet packet = new Packet(Packet.PacketType.DATA, 1, s);
    sendPacket(packet, this.serverAddress, this.port);
  }
}
