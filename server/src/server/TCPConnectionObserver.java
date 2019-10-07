package server;

public interface TCPConnectionObserver {
    void connectionReady(TCPConnection tcpConnection);
    void recievedMessage(TCPConnection tcpConnection, String message);
    void disconnected(TCPConnection tcpConnection);
    void unexpectedError(TCPConnection tcpConnection, Exception e);
}