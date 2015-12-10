package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Server extends PacketHandler {

  private static int CHUNK_SIZE = 4 * 1024;
  private byte[] fileData;

  public Server(int udpPort) throws SocketException {
    super(new DatagramSocket(udpPort), "Server");
  }

  @Override
  public void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    byte[] b = receivedDatagram.getData();
    Packet receivedPacket = new Packet(b);

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal)receivedPacket.getBody()) {
        case SHAKE_HANK_PACKET:
          respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0,
                  Signal.STARTING_IMAGE_TRANSMISSION));
          startFileTransmission(receivedDatagram);
          break;
        case PACKET_RECEIVED:
//          System.out.println(receivedPacket.getPacketNumber() * CHUNK_SIZE  + "  "  + fileData.length);
          if((receivedPacket.getPacketNumber() + 1) * CHUNK_SIZE >= fileData.length) {
            System.out.println("HERE");
            respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0, Signal.TRANSMISSION_COMPLETED));
          } else {
            sendChunkNumber(receivedDatagram, receivedPacket.getPacketNumber() + 1);
//            System.out.println(receivedPacket.getPacketNumber());

          }

      }
    }
//    respond(receivedDatagram, receivedPacket);
  }

  private void startFileTransmission(DatagramPacket receivedDatagram) throws IOException {
    loadFile("/home/ahmedatef/img.jpg");
    sendChunkNumber(receivedDatagram, 0);
  }

  private void sendChunkNumber(DatagramPacket receivedDatagram, int chunkNumber) throws IOException {
    Packet packet = new Packet(PacketType.DATA, chunkNumber, getChunk(chunkNumber));
    sendPacket(packet, receivedDatagram.getAddress(), receivedDatagram.getPort());
  }

  private void loadFile(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    fileData = Files.readAllBytes(path);
  }

  private byte[] getChunk(int chunkNumber) {
//    System.out.println(chunkNumber);
    return Arrays.copyOfRange(fileData, CHUNK_SIZE * chunkNumber, CHUNK_SIZE * (chunkNumber + 1));
  }


}