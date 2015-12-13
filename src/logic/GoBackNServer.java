package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
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

    for(int i = 1; i < windowSize + 2 && (i + chunkId) < vps.size(); ++i) {
      if(vps.get(i + chunkId) == PacketState.VIRGIN) {
        trySendChunk(i + chunkId);
      }
    }

    reschedule(chunkId + 1);
  }

  private void reschedule(final int chunkId) {
    timer.reschedule(new Runnable() {
      @Override
      public void run() {
        if(vps.get(chunkId) == PacketState.AWAITING_RESPONSE) {
          for(int i = 0; i < windowSize + 1 && (i + chunkId) < vps.size(); ++i) {
            try {
              trySendChunk(i + chunkId);
            } catch(IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
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

    timer.schedule(new Runnable() {
      @Override
      public void run() {
        if(vps.get(0) == PacketState.AWAITING_RESPONSE) {
          for(int i = 0; i < windowSize + 1 && (i) < vps.size(); ++i) {
            try {
              trySendChunk(i);
            } catch(IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
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


