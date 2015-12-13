package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * Created by ahmedatef on 12/12/15.
 */
public class SelectiveRepeatServer extends Server {


  private int windowSize;

  public SelectiveRepeatServer(int udpPort, int windowSize) throws SocketException {
    super(udpPort);
    this.windowSize = windowSize;
  }

  @Override
  public synchronized void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    Packet packet = new Packet(receivedDatagram.getData());

    if(packet.getType() == PacketType.SIGNAL) {
      switch((Signal) packet.getBody()) {
        case TRANSMISSION_COMPLETED_RECEIVED:
          break;
        case SHAKEHAND_PACKET:
          diagrams[0] = receivedDatagram;
          respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0,
                  Signal.STARTING_IMAGE_TRANSMISSION));
          startFileTransmission(receivedDatagram);
          break;
        case PACKET_RECEIVED:
          System.out.println(packet.getPacketId() + " " + vps.get(packet.getPacketId()));
          if(vps.get(packet.getPacketId()) == PacketState.AWAITING_RESPONSE) {
            slideWindow(packet.getPacketId());
          }
          break;
      }
    }
  }

  public synchronized void slideWindow(int chunkId) throws IOException {
    System.out.println("I am the SLIDING WINODW YOU HAV EBEEN DSERAHCING FOR ME !! " +
            countOfStateInVps(PacketState.ACK_RECEIVED) + " " + fi.getChunkCount());

    if(countOfStateInVps(PacketState.ACK_RECEIVED) + 1 == fi.getChunkCount()) {
      System.out.println("HEREHERHEHER");
      sendFileTransmissionCompleted();
      return;
    }

    int sow = startOfWindow();

    vps.set(chunkId, PacketState.ACK_RECEIVED);
    printVps();

    if(sow == chunkId) {
      System.out.println("~~ THis is SOW: " + sow);
      int newSow = startOfWindow();
      for(int i = 0; i < windowSize + 1 && (i + newSow) < vps.size(); ++i) {
        System.out.println("-_-_-_-_-_-_-_--_---___---_-TRYING TO SEND CHUNK ID " + (i + newSow) + " " + vps.get(i + newSow));
        if(vps.get(i + newSow) == PacketState.VIRGIN) {
          System.out.println("____TRYING TO SEND ======================> " + (i + newSow));
          trySendChunk(i + newSow);
        }
      }
    }

  }

  public void startFileTransmission(DatagramPacket receivedDatagram) throws IOException {
    fi = new FileInstance("/home/ahmedatef/txt_one_line", windowSize);
    for(int i = 0; i < fi.getChunkCount(); ++i) {
      vps.add(PacketState.VIRGIN);
    }
    diagrams[0] = receivedDatagram;
    for(int i = 0; i < windowSize; ++i) {
      sendChunk(i);
    }
  }
}
