package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by ahmedatef on 11/29/15.
 */
public class Server implements PacketHandler {
  public enum ServerState {
    OFF,
    CONNECTING,
    FAILED_TO_CONNECT,
    LISTENING,
  }

  private DatagramSocket udpServer;

  private Thread udpThread;

  private ServerState serverState = ServerState.OFF;

  public Server(int udpPort) {
    serverState = ServerState.CONNECTING;

    log(String.format("Attempting UDP socket connection on port %d...", udpPort));
    try {
      udpServer = new DatagramSocket(udpPort);
    } catch(SocketException e) {
      log(e.getMessage());
      serverState = ServerState.FAILED_TO_CONNECT;
      return;
    }

    boolean udpConnected = packetAwaiter();

    if(udpConnected) {
      serverState = ServerState.LISTENING;
      log("Server is ready and listening...");

      udpThread.start();
    } else {
      serverState = ServerState.FAILED_TO_CONNECT;
      log("Server couldn't connect, try changing port numbers.");
    }
  }

  @Override
  public boolean packetAwaiter() {
    udpThread = new Thread() {
      public void run() {
        try {
          while(true) {
            byte[] dataAwaiting = new byte[512];
            final DatagramPacket receivePacket = new DatagramPacket(dataAwaiting,
                    dataAwaiting.length);

            udpServer.receive(receivePacket);

            // Resolving the client request on a detached thread.
            Thread resolve = new Thread() {
              public void run() {
                try {
                  System.out.println("Some Packet Received in the Server");
                  resolvePacket(receivePacket);
                } catch(Exception e) {
                  log(e.getMessage());
                }
              }
            };
            resolve.start();
          }
        } catch(IOException e) {
          log(e.getMessage());
        }
      }
    };
    return true;
  }

  private void resolvePacket(DatagramPacket receivedDatagram) throws IOException {
    if(serverState != ServerState.LISTENING) return;
    System.out.println("got some packet");
    byte[] b = receivedDatagram.getData();
    System.out.println("In Between and size of received data from datagram: " + b.length);
    Packet receivedPacket = new Packet(b);
    System.out.println("## " + receivedPacket.getBody());

//    Packet newPacket = new Packet(Packet.PacketType.DATA, 1, "SOME STRING");

  }

  private void sendUdpPacket(Packet packet) {
    //    DatagramPacket sendPacket = new DatagramPacket(
    //            newPacket.getRaw(),
    //            newPacket.getRaw().length,
    //            receivedDatagram.getAddress(),
    //            receivedDatagram.getPort());
    //
    //    udpServer.send(sendPacket);
    //    log("Some packet sent to a client");
  }

  public ServerState getServerState() {
    return serverState;
  }

  private void log(String log) {
    System.out.println("=> " + log);
  }

  public boolean turnOff() {
    log("Attempting to turnOff server.");

    if(serverState != ServerState.LISTENING) {
      log("Server was not up to shut it OFF.");
      return false;
    }

    udpServer.close();

    boolean allClosed = udpServer.isClosed();
    if(allClosed) {
      serverState = ServerState.OFF;
      log("Server is off.");
    } else {
      log("Server failed to turnOff.");
    }
    return allClosed;
  }
}