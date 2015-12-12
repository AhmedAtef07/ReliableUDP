package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

/**
 * Created by ahmedatef on 11/29/15.
 */
public abstract class Server extends PacketHandler {
  private byte[] fileData;

  public final DatagramPacket[] diagrams = new DatagramPacket[1];
  public FileInstance fi;

  public final PriorityQueue<Integer> windowIds = new PriorityQueue<Integer>();
  public final Vector<Integer> receivedAcks = new Vector<Integer>();

  boolean transmissionCompleted = false;

  public Server(int udpPort) throws SocketException {
    super(new DatagramSocket(udpPort), "Server");
  }

  public synchronized void sendFileTransmissionCompleted() throws IOException {
    respond(diagrams[0], new Packet(PacketType.SIGNAL, 0, Signal.TRANSMISSION_COMPLETED));
    transmissionCompleted = true;
  }

  public synchronized void trySendNextDataChunkOf(int chunkId) throws IOException {
    log("____THIS ID: " + chunkId + " hasNextValue? " + fi.hasNextAfter(chunkId));
    if(fi.hasNextAfter(chunkId)) {
      sendChunk(chunkId + 1);
    } else if(receivedAcks.size() == fi.getChunkCount()) {
      sendFileTransmissionCompleted();
    }
  }

  public synchronized void trySendChunk(int chunkId) throws IOException {
    if(fi.chunkExists(chunkId)) {
      sendChunk(chunkId);
    } else if(receivedAcks.size() == fi.getChunkCount()) {
      sendFileTransmissionCompleted();
    }
  }

  public void sendChunk(int chunkId)
          throws IOException {
    if(transmissionCompleted) return;
    Packet packet = new Packet(PacketType.DATA, chunkId, fi.getChunk(chunkId));
    sendPacket(packet, diagrams[0].getAddress(), diagrams[0].getPort());
    if(!windowIds.contains(chunkId)) windowIds.add(chunkId);
    scheduleTimeout(1000, chunkId);
  }

  private synchronized void scheduleTimeout(int timeout, final int chunkId) {
    Timer timeoutTimer = new Timer();
    timeoutTimer.schedule(new TimerTask() {
      @Override
      public void run() {
//        System.out.println("____Timer ticked: " + packetNumber + " " + receivedAcks.contains
//                (packetNumber));
        if(!receivedAcks.contains(chunkId)) {
          try {
            System.out.println("_____ Timer resending packet number: " + chunkId);
            sendChunk(chunkId);
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
      }
    }, timeout);
  }


}