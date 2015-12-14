package logic;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Client extends PacketHandler {
  private ImageViewer imageViewer;

  private InetAddress serverAddress;
  private int port;

  public Vector<Packet> dataPackets;
  private String str;

  private FileOutputStream fos;
  public String outputFileName;

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
      for(Packet p : dataPackets) {
        if(p.getPacketId() == receivedPacket.getPacketId()) return;
      }
      dataPackets.add(receivedPacket);

      //      if(true) return;

      //      if(new Random().nextInt(2) == 0) return;
      ackResponse(receivedDatagram, receivedPacket);
    }

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal) receivedPacket.getBody()) {
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
            appendToFile((byte[]) p.getBody());
          }
          closeFile();
          break;
      }
    }
  }

  private void updateImage() throws IOException {
    int receivedLength = 0;
    for(Packet p : dataPackets) {
      receivedLength += ((byte[])p.getBody()).length;
    }
    ByteBuffer bb = ByteBuffer.allocate(receivedLength);
    System.out.println(receivedLength);

    for(Packet p : dataPackets) {
      bb.put((byte[])p.getBody());
    }

    //    imageViewer.setImageData(bb.array(), "[" + titleInfo + "]");
    imageViewer.setImageData(bb.array());
  }

  public void initDataPackets() throws IOException {
    outputFileName = "received_file_" + new Random().nextInt(20) + "_" + new Random().nextInt(20);
    generateFile(outputFileName);
    dataPackets = new Vector<Packet>();
  }

  public void generateFile(String fileName) throws FileNotFoundException {
    fos = new FileOutputStream(fileName);
  }

  public synchronized void appendToFile(byte[] data) throws IOException {
    fos.write(data);
  }

  public void closeFile() throws IOException {
    fos.flush();
    fos.close();
    System.out.println(outputFileName);
    System.out.println(new BufferedReader(new FileReader(new File(outputFileName))).readLine());
  }
}
