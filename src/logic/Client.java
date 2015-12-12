package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Vector;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Client extends PacketHandler {
  private InetAddress serverAddress;
  private int port;

  private Vector<Packet> dataPackets;
  private String str;

  public Client(String serverName, int port) throws IOException, InterruptedException {
    super(new DatagramSocket(), "Client");
    this.serverAddress = InetAddress.getByName(serverName);
    this.port = port;
  }

  public void sendShakeHandPacketToServer() throws IOException {
    sendPacket(new Packet(PacketType.SIGNAL, 0, Signal.SHAKEHAND_PACKET), serverAddress, port);
  }

  @Override
  public synchronized void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    byte[] b = receivedDatagram.getData();
    Packet receivedPacket = new Packet(b);

    if(receivedPacket.getType() == PacketType.DATA) {
//      if(true) return;
//      try {
//        Thread.sleep(500);
//      } catch(InterruptedException e) {
//        e.printStackTrace();
//      }
      if(new Random().nextBoolean()) return;
      dataPackets.add(receivedPacket);
      str += new String((byte[])receivedPacket.getBody());
//      System.out.println("## RECEIVED STRING: " + new String((byte[])receivedPacket.getBody()));
      ackResponse(receivedDatagram, receivedPacket);
    }

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal)receivedPacket.getBody()) {
        case STARTING_IMAGE_TRANSMISSION:
          initDataPackets();
          break;
        case TRANSMISSION_COMPLETED:
          System.out.println();
          System.out.println("The whole sentence: '" + str +  "'");
          System.out.println();
          respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0,
                  Signal.TRANSMISSION_COMPLETED_RECEIVED));
          System.out.println();
          System.out.println();
          for(Packet p : dataPackets) {
            System.out.println(new String((byte[])p.getBody()));
          }
          break;
      }
    }
  }

  private void initDataPackets() throws IOException {
    dataPackets = new Vector<Packet>();
    str = "";
  }
}
