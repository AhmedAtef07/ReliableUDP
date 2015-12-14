package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Vector;

/**
 * Created by ahmedatef on 11/29/15.
 */
public abstract class Server extends PacketHandler {
  public int lossProbability;
  public int timeout;

  public final DatagramPacket[] diagrams = new DatagramPacket[1];
  public FileInstance fi;

  public final Vector<PacketState> vps = new Vector<PacketState>();

  boolean transmissionCompleted = false;

  public long transmissionTime;

  public Server(int udpPort, int lossProbability, int timeout) throws SocketException {
    super(new DatagramSocket(udpPort), "Server");
    this.lossProbability = Math.min(lossProbability, 2);
    this.timeout = timeout;
  }

  public synchronized void sendFileTransmissionCompleted() throws IOException {
    transmissionTime = System.currentTimeMillis();
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




}