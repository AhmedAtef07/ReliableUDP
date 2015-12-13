package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * Created by ahmedatef on 12/12/15.
 */
public class SelectiveRepeatServer extends Server {

  private int windowSize;

  public SelectiveRepeatServer(int udpPort, int windowSize, int lossProbability, int timeout)
          throws SocketException {
    super(udpPort, lossProbability, timeout);
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
          if(vps.get(packet.getPacketId()) == PacketState.AWAITING_RESPONSE) {
            slideWindow(packet.getPacketId());
          }
          break;
      }
    }
  }

  public synchronized void slideWindow(int chunkId) throws IOException {
    if(countOfStateInVps(PacketState.ACK_RECEIVED) + 1 == fi.getChunkCount()) {
      sendFileTransmissionCompleted();
      return;
    }

    int sow = startOfWindow();

    vps.set(chunkId, PacketState.ACK_RECEIVED);

    if(sow == chunkId) {
      int newSow = startOfWindow();
      for(int i = 0; i < windowSize + 1 && (i + newSow) < vps.size(); ++i) {
        if(vps.get(i + newSow) == PacketState.VIRGIN) {
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

  public synchronized int startOfWindow() {
    int ackCount = -1;
    for(int i = 0; i < vps.size(); ++i) {
      if(vps.get(i) != PacketState.ACK_RECEIVED) return i;
    }
    return ackCount;
  }
}
