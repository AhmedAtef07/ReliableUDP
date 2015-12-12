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
          if(!receivedAcks.contains(packet.getPacketId())) {
            receivedAcks.add(packet.getPacketId());
            slideWindow(packet.getPacketId());
          }
          break;
      }
    }
  }

  public synchronized void slideWindow(int chunkId) throws IOException {
    System.out.println(String.format("IN SLIDE WINDOW_______________________" +
            "%d %d || %d %d || %s || %s", receivedAcks.size(), fi.getChunkCount(), windowIds.peek(),
            chunkId, getWindowSizeElements(), getAckResponses()));
    if(receivedAcks.size() == fi.getChunkCount()) {
      sendFileTransmissionCompleted();
      return;
    }

    if(windowIds.peek() == chunkId) {
      windowIds.remove(chunkId);
      for(int i = 0; i < windowSize; ++i) {
        int sow = 0;
        if(windowIds.isEmpty()) sow = receivedAcks.lastElement() + 1;
        else sow = windowIds.peek();

        if(!windowIds.contains(i + sow)) {
          System.out.println("____TRYING TO SEND ======================> " + (i + sow));
          trySendChunk(i + sow);
        }
      }
    } else {
      windowIds.remove(chunkId);
    }
    //    windowIds.remove(chunkId);
    //    trySendNextDataChunkOf(chunkId + windowSize);
  }

  private String getWindowSizeElements() {
    String s = "";
    Object[] os = windowIds.toArray();
    for(Object o : os) {
      s += o + ", " ;
    }
    return s;
  }

  private String getAckResponses() {
    String s = "";
    Object[] os = receivedAcks.toArray();
    for(Object o : os) {
      s += o + ", " ;
    }
    return s;
  }

  public void startFileTransmission(DatagramPacket receivedDatagram) throws IOException {
    fi = new FileInstance("/home/ahmedatef/txt_one_line", windowSize);
    diagrams[0] = receivedDatagram;
    for(int i = 0; i < windowSize; ++i) {
      sendChunk(i);
    }
  }
}
