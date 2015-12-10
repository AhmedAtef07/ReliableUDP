package logic;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

  public Client(String serverName, int port) throws IOException, InterruptedException {
    super(new DatagramSocket(), "Client");
    this.serverAddress = InetAddress.getByName(serverName);
    this.port = port;
    this.id = count++;

//    loadImage(Paths.get("/home/ahmedatef/img.jpg"));
  }

  public void sendToServer(String s) throws IOException {
    Packet packet = new Packet(Packet.PacketType.DATA, 1, s);
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
    Packet packet = new Packet(Packet.PacketType.DATA, 1, image);
    sendPacket(packet, this.serverAddress, this.port);
  }

  public void sendImageToServer(String imagePath) throws IOException {
    Path path = Paths.get("/home/ahmedatef/img.jpg");
    byte[] data = Files.readAllBytes(path);
    Packet packet = new Packet(Packet.PacketType.DATA, 1, Arrays.copyOf(data, 30000));
    sendPacket(packet, this.serverAddress, this.port);
  }

}
