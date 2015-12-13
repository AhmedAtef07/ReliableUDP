package logic;

import java.net.SocketException;

/**
 * Created by ahmedatef on 12/12/15.
 */
public class StopAndWaitServer extends SelectiveRepeatServer {
  public StopAndWaitServer(int udpPort, int lossProbability, long timeout) throws SocketException {
    super(udpPort, 1, lossProbability, timeout);
  }
}
