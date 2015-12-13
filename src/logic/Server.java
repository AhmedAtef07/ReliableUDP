package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by ahmedatef on 11/29/15.
 */
public abstract class Server extends PacketHandler {
  private int lossProbability;
  private int timeout;

  public final DatagramPacket[] diagrams = new DatagramPacket[1];
  public FileInstance fi;

  public final Vector<PacketState> vps = new Vector<PacketState>();

  boolean transmissionCompleted = false;

  public Server(int udpPort, int lossProbability, int timeout) throws SocketException {
    super(new DatagramSocket(udpPort), "Server");
    this.lossProbability = Math.min(lossProbability, 2);
    this.timeout = timeout;
  }

  public synchronized void sendFileTransmissionCompleted() throws IOException {
    respond(diagrams[0], new Packet(PacketType.SIGNAL, 0, Signal.TRANSMISSION_COMPLETED));
    transmissionCompleted = true;
  }

  public synchronized int countOfStateInVps(PacketState packetState) {
    int ackCount = 0;
    for(PacketState ps : vps) {
      if(ps == packetState) ackCount++;
    }
    return ackCount;
  }

  public void printVps() {
    String s = "";
    for(int i = 0; i < vps.size(); ++i) {
      s += vps.get(i) + ", ";
    }
    System.out.println(s);
  }

  public int firstIndexOfState(PacketState packetState) {
    int ackCount = -1;
    for(int i = 0; i < vps.size(); ++i) {
      if(vps.get(i) == packetState) return i;
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