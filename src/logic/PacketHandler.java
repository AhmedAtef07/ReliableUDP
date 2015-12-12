package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ahmedatef on 12/6/15.
 */
public abstract class PacketHandler {
  public static final int PACKET_CHUNK_SIZE = Math.max(2, FileInstance.FILE_CHUNK_SIZE) +
          Packet.HEADER_LENGTH;

  private Thread udpThread;

  private DatagramSocket datagramSocket;

  private String name;

  public PacketHandler(DatagramSocket datagramSocket, String name) {
    this.datagramSocket = datagramSocket;
    this.name = name;
    initUdpThread();
  }

  public void initUdpThread() {
    this.udpThread = new Thread() {
      public void run() {
        try {
          while(true) {
            // Receiving pack expected size must not be less than 2 + header size, as signals are
            // shorts which are 2 bytes.
            byte[] dataAwaiting = new byte[PACKET_CHUNK_SIZE];
            final DatagramPacket receivedDatagram = new DatagramPacket(dataAwaiting,
                    dataAwaiting.length);

            log("Awaiting data...");
            datagramSocket.receive(receivedDatagram);
            log("Datagram received");

            // Resolving the client request on a detached thread.
            Thread resolver = new Thread() {
              public void run() {
                try {
                  resolveDatagram(receivedDatagram);
                } catch(Exception e) { }
              }
            };
            resolver.start();
          }
        } catch(IOException e) { }
      }
    };
    this.udpThread.start();
  }

  public abstract void resolveDatagram (DatagramPacket receivedDatagram) throws IOException;

  public synchronized void respond(DatagramPacket receivedDatagram, Packet newPacket)
          throws IOException {
//    new ImageViewer("Received in " + name, (byte[])receivedPacket.getBody());
    sendPacket(newPacket, receivedDatagram.getAddress(), receivedDatagram.getPort());
  }

  public synchronized void ackResponse(DatagramPacket receivedDatagram, Packet receivedPacket)
          throws IOException {
    Packet respondPacket = new Packet(PacketType.SIGNAL, receivedPacket.getPacketNumber(),
            Signal.PACKET_RECEIVED);
    sendPacket(respondPacket, receivedDatagram.getAddress(), receivedDatagram.getPort());
  }

  public synchronized void sendPacket(Packet packet, InetAddress inetAddress, int port)
          throws IOException {
    DatagramPacket sendPacket = new DatagramPacket(
            packet.getRaw(),
            packet.getRaw().length,
            inetAddress,
            port);

    datagramSocket.send(sendPacket);

    log(String.format("Datagram sent to %s:%d of type %s and length %d. Content: %s",
            inetAddress.getHostAddress(),
            port,
            packet.getType(),
            packet.getRaw().length,
            packet.getBody().toString()));
  }

  public synchronized void log(String l) {
    System.out.println(String.format("%s(%d, %s) => %s",
            name,
            Thread.currentThread().getId(),
            Thread.currentThread().getName(),
            l));
//    System.out.println("Alive threads: " + Thread.activeCount());
  }

  public synchronized String getName() {
    return name;
  }
}
