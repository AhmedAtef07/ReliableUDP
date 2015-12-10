package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ahmedatef on 12/6/15.
 */
public abstract class PacketHandler {
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
            byte[] dataAwaiting = new byte[512];
            final DatagramPacket receivedDatagram = new DatagramPacket(dataAwaiting,
                    dataAwaiting.length);

            log("Awaiting data");
            datagramSocket.receive(receivedDatagram);
            log("data received");

            // Resolving the client request on a detached thread.
            Thread resolver = new Thread() {
              public void run() {
                try {
                  log("data will be resolved now on a new thread");
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
    log("Thread is up!");
  }

  private void resolveDatagram(DatagramPacket receivedDatagram) {
    byte[] b = receivedDatagram.getData();
    Packet receivedPacket = new Packet(b);
    log("## " + receivedPacket.getBody());
  }

  public void sendPacket(Packet packet, InetAddress inetAddress, int port) throws IOException {
    DatagramPacket sendPacket = new DatagramPacket(
            packet.getRaw(),
            packet.getRaw().length,
            inetAddress,
            port);
    datagramSocket.send(sendPacket);
    log("Data sent");
  }

  public void log(String l) {
    System.out.println(String.format("%s(%d, %s) => %s",
            name,
            Thread.currentThread().getId(),
            Thread.currentThread().getName(),
            l));
  }
}
