package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Server extends PacketHandler {
  private byte[] fileData;
  private int nextChunkToBeSent;
  private Timer timeoutTimer;

  private DatagramPacket lastReceivedDatagram;

  public Server(int udpPort) throws SocketException {
    super(new DatagramSocket(udpPort), "Server");
  }

  @Override
  public synchronized void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    lastReceivedDatagram = receivedDatagram;

    byte[] b = receivedDatagram.getData();
    Packet receivedPacket = new Packet(b);

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal)receivedPacket.getBody()) {
        case SHAKEHAND_PACKET:
          respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0,
                  Signal.STARTING_IMAGE_TRANSMISSION));
          startFileTransmission(receivedDatagram);
          break;
        case PACKET_RECEIVED:
          if(receivedPacket.getPacketNumber() != nextChunkToBeSent - 1) return;
          sendNextDataChunk(receivedDatagram);

      }
    }
//    respond(receivedDatagram, receivedPacket);
  }

  private void sendNextDataChunk(DatagramPacket receivedDatagram) throws IOException {
    if(nextChunkToBeSent * CHUNK_SIZE >= fileData.length) {
      respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0, Signal.TRANSMISSION_COMPLETED));
    } else {
      sendChunkNumber(receivedDatagram, nextChunkToBeSent++);
    }

  }

  private void startFileTransmission(DatagramPacket receivedDatagram) throws IOException {
    loadFile("/home/ahmedatef/img.jpg");
    sendChunkNumber(receivedDatagram, nextChunkToBeSent++);
  }

  private void sendChunkNumber(DatagramPacket receivedDatagram, int chunkNumber) throws IOException {
    Packet packet = new Packet(PacketType.DATA, chunkNumber, getChunk(chunkNumber));
    sendPacket(packet, receivedDatagram.getAddress(), receivedDatagram.getPort());
    timeoutTimer.cancel();
//    scheduleTimeout(500);
  }

  private void loadFile(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    fileData = Files.readAllBytes(path);
    nextChunkToBeSent = 0;
    timeoutTimer = new Timer();
  }

  private byte[] getChunk(int chunkNumber) {
//    System.out.println(chunkNumber);
    return Arrays.copyOfRange(fileData, CHUNK_SIZE * chunkNumber, CHUNK_SIZE * (chunkNumber + 1));
  }

  private void scheduleTimeout(int timeout) {

    timeoutTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          nextChunkToBeSent--;
          sendNextDataChunk(lastReceivedDatagram);
        } catch(IOException e) {
          e.printStackTrace();
        }
      }
    }, timeout);
  }


}