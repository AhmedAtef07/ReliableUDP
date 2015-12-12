package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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

  private final DatagramPacket[] diagrams = new DatagramPacket[1];

  private final HashSet<Integer> receivedAcks = new HashSet<Integer>();
  private FileInstance fi;

  public Server(int udpPort) throws SocketException {
    super(new DatagramSocket(udpPort), "Server");
  }

  @Override
  public synchronized void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    byte[] b = receivedDatagram.getData();
    Packet receivedPacket = new Packet(b);

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal) receivedPacket.getBody()) {
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
    if(fi.hasNextAfter(nextChunkIdToBeSent.get())) {
      sendChunkNumber(receivedDatagram, nextChunkIdToBeSent.getAndIncrement());
    } else {
      respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0, Signal.TRANSMISSION_COMPLETED));
    }

  }

  private void startFileTransmission(DatagramPacket receivedDatagram) throws IOException {
    fi = new FileInstance("/home/ahmedatef/txt_one_line", 1);
    nextChunkIdToBeSent.set(0);
    sendNextDataChunk(receivedDatagram);
  }

  private void sendChunkNumber(DatagramPacket receivedDatagram, int chunkNumber)
          throws IOException {
    Packet packet = new Packet(PacketType.DATA, chunkNumber, fi.getChunk(chunkNumber));
    sendPacket(packet, receivedDatagram.getAddress(), receivedDatagram.getPort());
    scheduleTimeout(1000, chunkNumber);
  }

  private synchronized void scheduleTimeout(int timeout, final int packetNumber) {
    Timer timeoutTimer = new Timer();
    timeoutTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        System.out.println("____Timer ticked: " + packetNumber + " " + receivedAcks.contains
                (packetNumber));
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