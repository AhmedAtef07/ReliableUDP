package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Client extends PacketHandler {
  private InetAddress serverAddress;
  private int port;

  private Vector<Packet> dataPackets;
  private ImageViewer imageViewer;

  public Client(String serverName, int port) throws IOException, InterruptedException {
    super(new DatagramSocket(), "Client");
    this.serverAddress = InetAddress.getByName(serverName);
    this.port = port;

    sendShakeHandPacketToServer();
  }

  private void sendShakeHandPacketToServer() throws IOException {
    sendPacket(new Packet(PacketType.SIGNAL, 0, Signal.SHAKEHAND_PACKET), serverAddress, port);
  }

  public void requestImageFromServer() throws IOException {
    Packet packet = new Packet(PacketType.SIGNAL, 1, Signal.REQUEST_IMAGE);
    sendPacket(packet, this.serverAddress, this.port);
  }

  public void sendToServer(String s) throws IOException {
    Packet packet = new Packet(PacketType.DATA, 1, s);
    sendPacket(packet, this.serverAddress, this.port);
  }

  public void loadImage(Path path) throws IOException, InterruptedException {
    byte[] data = Files.readAllBytes(path);
    new ImageViewer("Sending...", data);
//    int byteCount = 1000;
//    ImageViewer img1 = new ImageViewer(Arrays.copyOf(data, byteCount));
//
//    while(byteCount <= data.length) {
//      byteCount += 1000;
//      Thread.sleep(200);
//      img1.setImageData(Arrays.copyOf(data, byteCount));
//    }
  }

  public void sendBytesToServer(byte[] image) throws IOException {
    Packet packet = new Packet(PacketType.DATA, 1, image);
    sendPacket(packet, this.serverAddress, this.port);
  }

  public void sendImageToServer(String imagePath) throws IOException {
    Path path = Paths.get("/home/ahmedatef/img.jpg");
    byte[] data = Files.readAllBytes(path);
    Packet packet = new Packet(PacketType.DATA, 1, Arrays.copyOf(data, 30000));
    sendPacket(packet, this.serverAddress, this.port);
  }

  @Override
  public synchronized void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    byte[] b = receivedDatagram.getData();
    Packet receivedPacket = new Packet(b);

    if(receivedPacket.getType() == PacketType.DATA) {
      dataPackets.add(receivedPacket);
      updateImage();
      ackResponse(receivedDatagram, receivedPacket);
    }

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal)receivedPacket.getBody()) {
        case STARTING_IMAGE_TRANSMISSION:
          initDataPackets();
          break;
        case TRANSMISSION_COMPLETED:
          respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0, Signal.TRANSMISSION_COMPLETED_RECEIVED));
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

    imageViewer.setImageData(bb.array());
  }
  private void initDataPackets() throws IOException {
    dataPackets = new Vector<Packet>();
    imageViewer = new ImageViewer("Received in " + getName(), new byte[0]);
  }
}
