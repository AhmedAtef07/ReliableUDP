package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;

/**
 * Created by ahmedatef on 12/13/15.
 */
public class GoBackNClient extends Client {
  private ImageViewer imageViewer;

  public GoBackNClient(String serverName, int port) throws IOException, InterruptedException {
    super(serverName, port);
  }

  public synchronized void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {
    byte[] b = receivedDatagram.getData();
    Packet receivedPacket = new Packet(b);

    if(receivedPacket.getType() == PacketType.DATA) {
//      System.out.println("!!!!!!__________________________________________________ SOMETHING got received....");
      if(dataPackets.size() != receivedPacket.getPacketId()) return;
      dataPackets.add(receivedPacket);
      updateImage();
      try {
        Thread.sleep(800);
      } catch(InterruptedException e) {
        e.printStackTrace();
      }
      ackResponse(receivedDatagram, receivedPacket);
    }

    if(receivedPacket.getType() == PacketType.SIGNAL) {
      switch((Signal) receivedPacket.getBody()) {
        case STARTING_IMAGE_TRANSMISSION:
          initDataPackets();
          break;
        case TRANSMISSION_COMPLETED:
          respond(receivedDatagram, new Packet(PacketType.SIGNAL, 0,
                  Signal.TRANSMISSION_COMPLETED_RECEIVED));
          Collections.sort(dataPackets, new Comparator<Packet>() {
            @Override
            public int compare(Packet o1, Packet o2) {
              return o1.getPacketId() - o2.getPacketId();
            }
          });
          for(Packet p : dataPackets) {
            appendToFile((byte[]) p.getBody());
          }
          closeFile();
          updateImage();

          break;
      }
    }
  }

  private void updateImage() throws IOException {
    int receivedLength = 0;
    for(Packet p : dataPackets) {
      receivedLength += ((byte[])p.getBody()).length;
    }
    ByteBuffer bb = ByteBuffer.allocate(receivedLength);
    System.out.println(receivedLength);

    for(Packet p : dataPackets) {
      bb.put((byte[])p.getBody());
    }

//    imageViewer.setImageData(bb.array(), "[" + titleInfo + "]");
    imageViewer.setImageData(bb.array());
  }

  public void initDataPackets() throws IOException {
    outputFileName = "received_file_go_back_n_" + new Random().nextInt(20) + "_" + new Random().nextInt(20);
    generateFile(outputFileName);
    dataPackets = new Vector<Packet>();
    imageViewer = new ImageViewer("Received in " + getName(), new byte[0]);
  }

}
