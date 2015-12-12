package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Server extends PacketHandler {
  private byte[] fileData;
  private final AtomicInteger nextChunkIdToBeSent = new AtomicInteger();
  private Timer timeoutTimer;

  private final DatagramPacket[] diagrams = new DatagramPacket[1];

  private final HashSet<Integer> receivedAcks = new HashSet<Integer>();

  public Server(int udpPort) throws SocketException {
    super(new DatagramSocket(udpPort), "Server");
  }

  @Override
  public synchronized void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    byte[] b = receivedDatagram.getData();
    Packet receivedPacket = new Packet(b);

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal)receivedPacket.getBody()) {
        case SHAKEHAND_PACKET:
          diagrams[0] = receivedDatagram;
          respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0,
                  Signal.STARTING_IMAGE_TRANSMISSION));
          startFileTransmission(receivedDatagram);
          break;
        case PACKET_RECEIVED:
          if(receivedPacket.getPacketNumber() != nextChunkIdToBeSent.get() - 1) return;
          receivedAcks.add(receivedPacket.getPacketNumber());
          sendNextDataChunk(receivedDatagram);

      }
    }
//    respond(receivedDatagram, receivedPacket);
  }

  private synchronized void sendNextDataChunk(DatagramPacket receivedDatagram) throws IOException {
    if(nextChunkIdToBeSent.get() * CHUNK_SIZE >= fileData.length) {
      respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0, Signal.TRANSMISSION_COMPLETED));
    } else {
      sendChunkNumber(receivedDatagram, nextChunkIdToBeSent.getAndIncrement());
    }

  }

  private void startFileTransmission(DatagramPacket receivedDatagram) throws IOException {
    loadFile("/home/ahmedatef/txt_one_line");
    nextChunkIdToBeSent.set(0);
    sendNextDataChunk(receivedDatagram);
  }

  private void sendChunkNumber(DatagramPacket receivedDatagram, int chunkNumber)
          throws IOException {
    Packet packet = new Packet(PacketType.DATA, chunkNumber, getChunk(chunkNumber));
    sendPacket(packet, receivedDatagram.getAddress(), receivedDatagram.getPort());
    scheduleTimeout(1000, chunkNumber);
  }

  private void loadFile(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    fileData = Files.readAllBytes(path);
    log(String.format("Numbers of packets to be sent: %d / %d = %d", fileData.length, CHUNK_SIZE,
            fileData.length / CHUNK_SIZE));
  }

  private byte[] getChunk(int chunkNumber) {
    return Arrays.copyOfRange(fileData, CHUNK_SIZE * chunkNumber,
            Math.min(fileData.length, CHUNK_SIZE * (chunkNumber + 1)));
  }

  private synchronized void scheduleTimeout(int timeout, final int packetNumber) {
    timeoutTimer = new Timer();
    timeoutTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        System.out.println("____Timer ticked: " + packetNumber + " " + receivedAcks.contains(packetNumber));
        if(!receivedAcks.contains(packetNumber)) {
          try {
            sendChunkNumber(diagrams[0], packetNumber);
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
      }
    }, timeout);
  }


}