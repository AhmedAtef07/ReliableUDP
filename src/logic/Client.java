package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Client {
  private static int count = 0;

  private int port;
  private int id;

  private Vector<Packet> receivedPackets;

  public Client(int port) {
    this.port = port;
    this.id = count++;

    log("Client created");
    try {
      udpRequestFile(new Packet(Packet.PacketType.SIGNAL, 0, Signal.REQUEST_DATA_TRANSMISSION));
    } catch(IOException e) {
      e.printStackTrace();
    }
    log("Client terminated");
  }

  private void log(String log) {
    System.out.println(String.format("%d => %s", id, log));
  }

  private void udpRequestFile(Packet packet) throws IOException {
    DatagramSocket clientSocket = new DatagramSocket();

    InetAddress ipAddress = InetAddress.getByName("localhost");

    DatagramPacket sendPacket = new DatagramPacket(
            packet.getRaw(),
            packet.getRaw().length,
            ipAddress,
            this.port);

    clientSocket.send(sendPacket);
    System.out.println("PACKET SENT TO SERVER");

//    byte[] receiveData = new byte[Integer.BYTES];
//    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//
//    // Waiting for response...
//    try {
//      clientSocket.receive(receivePacket);
//    } catch(IOException e) {
//      log(e.getMessage());
//    }
//
//    Packet receivedPacket = new Packet(receiveData);
//    System.out.println(receivedPacket.getBody().toString());
//    receivedPackets.add(receivedPacket);
//
//    clientSocket.close();
  }
}
