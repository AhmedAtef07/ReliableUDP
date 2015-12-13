package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * Created by ahmedatef on 12/13/15.
 */
public class GoBackNServer extends Server {

  private int windowSize;

  public GoBackNServer(int udpPort, int windowSize, int lossProbability) throws SocketException {
    super(udpPort, lossProbability);
    this.windowSize = windowSize;
  }

  @Override
  public void resolveDatagram(DatagramPacket receivedDatagram) throws IOException {

  }
}
