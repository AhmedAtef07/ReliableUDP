package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
          transmissionTime =  System.currentTimeMillis() - transmissionTime;
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

  public synchronized void trySendChunk(int chunkId) throws IOException {
    if(fi.chunkExists(chunkId)) {
      sendChunk(chunkId);
    } else if(countOfStateInVps(PacketState.ACK_RECEIVED) == fi.getChunkCount()) {
      sendFileTransmissionCompleted();
    }
  }

  public synchronized void sendChunk(int chunkId)
          throws IOException {
    if(transmissionCompleted) return;
    Packet packet = new Packet(PacketType.DATA, chunkId, fi.getChunk(chunkId));
    if(new Random().nextInt(lossProbability) != 0) {
      sendPacket(packet, diagrams[0].getAddress(), diagrams[0].getPort());
    }
    if(vps.get(chunkId) == PacketState.VIRGIN) vps.set(chunkId, PacketState.AWAITING_RESPONSE);
    scheduleTimeout(timeout, chunkId);
  }

  private synchronized void scheduleTimeout(int timeout, final int chunkId) {
    Timer timeoutTimer = new Timer();
    timeoutTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if(vps.get(chunkId) == PacketState.AWAITING_RESPONSE) {
          try {
            System.out.println("______Timer resending packet number: " + chunkId);
            sendChunk(chunkId);
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
      }
    }, timeout);
  }
}
