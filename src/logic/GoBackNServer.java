package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ahmedatef on 12/13/15.
 */
public class GoBackNServer extends Server {

  private int windowSize;
  private final ReschedulableTimer timer;

  public GoBackNServer(int udpPort, int windowSize, int lossProbability, int timeout) throws
          SocketException {
    super(udpPort, lossProbability, timeout);
    this.windowSize = windowSize;

    timer = new ReschedulableTimer(timeout);
  }

  @Override
  public synchronized void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    Packet packet = new Packet(receivedDatagram.getData());

    if(packet.getType() == PacketType.SIGNAL) {
      switch((Signal) packet.getBody()) {
        case TRANSMISSION_COMPLETED_RECEIVED:
          transmissionTime = System.currentTimeMillis() - transmissionTime;
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

    for(int i = 0; i <= chunkId; ++i) {
      vps.set(i, PacketState.ACK_RECEIVED);
    }
    reschedule(chunkId + 1);
    for(int i = 1; i < windowSize + 2 && (i + chunkId) < vps.size(); ++i) {
        trySendChunk(i + chunkId);
    }
  }

  private synchronized void reschedule(final int chunkId) {
    timer.reschedule(new Runnable() {
      @Override
      public void run() {
        for(int i = 0; i < windowSize && (i + chunkId) < vps.size(); ++i) {
          try {
            trySendChunk(i + chunkId);
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
        reschedule(chunkId + 1);

      }
    });
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
    if(true || new Random().nextInt(lossProbability) != 0) {
      sendPacket(packet, diagrams[0].getAddress(), diagrams[0].getPort());
    }
    if(vps.get(chunkId) == PacketState.VIRGIN) vps.set(chunkId, PacketState.AWAITING_RESPONSE);
    reschedule(chunkId);
  }

  public void startFileTransmission(DatagramPacket receivedDatagram) throws IOException {
    fi = new FileInstance("/home/ahmedatef/img.jpg", windowSize);
    for(int i = 0; i < fi.getChunkCount(); ++i) {
      vps.add(PacketState.VIRGIN);
    }

    timer.schedule(new Runnable() {
      @Override
      public void run() {
        System.out.println("____________TIMER TICKED");
        for(int i = 0; i < windowSize && (i) < vps.size(); ++i) {
          try {
            trySendChunk(i);
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
      }
    });

    diagrams[0] = receivedDatagram;
    for(int i = 0; i < windowSize; ++i) {
      sendChunk(i);
    }

//    System.out.println("TIMER MADE");
  }

  class ReschedulableTimer extends Timer {
    private Runnable task;
    private TimerTask timerTask;
    private int delay;

    public ReschedulableTimer(int delay) {
      this.delay = delay;
    }

    public void schedule(Runnable runnable) {
      task = runnable;
      makeTimerTask();
    }

    public void reschedule(Runnable runnable) {
      task = runnable;
      timerTask.cancel();
      makeTimerTask();
    }

    private void makeTimerTask() {
      timerTask = new TimerTask() {
        public void run() { task.run(); }
      };
      new Timer().schedule(timerTask, delay);
    }
  }
}


