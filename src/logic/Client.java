package logic;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Comparator;
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

  private FileOutputStream fos;
  private int expectedOrderedChunkId;
  private String outputFileName;

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
//        Thread.sleep(100);
//      } catch(InterruptedException e) {
//        e.printStackTrace();
//      }
//      if(new Random().nextInt(2) == 0) return;
      dataPackets.add(receivedPacket);
      ackResponse(receivedDatagram, receivedPacket);
    }

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal)receivedPacket.getBody()) {
        case STARTING_IMAGE_TRANSMISSION:
          initDataPackets();
          break;
        case TRANSMISSION_COMPLETED:
          respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0,
                  Signal.TRANSMISSION_COMPLETED_RECEIVED));
          Collections.sort(dataPackets, new Comparator<Packet>() {
            @Override
            public int compare(Packet o1, Packet o2) {
              return o1.getPacketId() - o2.getPacketId();
            }
          });
          for(Packet p : dataPackets) {
            appendToFile((byte[])p.getBody());
          }
          closeFile();
          break;
      }
    }
  }

  private void initDataPackets() throws IOException {
    outputFileName = "received_file_" + new Random().nextInt(20) + "_" + new Random().nextInt(20);
    generateFile(outputFileName);
    expectedOrderedChunkId = 0;
    dataPackets = new Vector<Packet>();
  }

  private void generateFile(String fileName) throws FileNotFoundException {
    fos = new FileOutputStream(fileName);
  }

  private synchronized void appendToFile(byte[] data) throws IOException {
    fos.write(data);
  }

  private void closeFile() throws IOException {
    fos.flush();
    fos.close();
    System.out.println(outputFileName);
    System.out.println(new BufferedReader(new FileReader(new File(outputFileName))).readLine());
  }
}
