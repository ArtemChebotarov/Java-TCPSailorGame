package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final TCPConnectionObserver tcpConnectionObserver;
    private String nickname;
    private boolean isReady = false;
    private boolean turn = false;
    private int number;
    private boolean isNumberSet;
    private int caesarKey;
    private String introductory;



    public TCPConnection(TCPConnectionObserver tcpConnectionObserver, String ip, int port) throws IOException{
        this(tcpConnectionObserver, new Socket(ip, port));
        setCaesarKey();
    }

    public TCPConnection(TCPConnectionObserver tcpConnectionObserver, Socket socket) throws IOException {
        setCaesarKey();
        this.socket = socket;
        this.tcpConnectionObserver = tcpConnectionObserver;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        this.rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tcpConnectionObserver.connectionReady(TCPConnection.this);

                    while (!rxThread.isInterrupted()) {
                        String message = in.readLine();
                        tcpConnectionObserver.recievedMessage(TCPConnection.this, message);
                    }

                } catch (IOException e) {
                    tcpConnectionObserver.unexpectedError(TCPConnection.this, e);
                }
                finally {
                    tcpConnectionObserver.disconnected(TCPConnection.this);
                }
            }
        });

        rxThread.start();
    }


    public synchronized void sendMessage(String message) {
        try {
            out.write(message + "\r\n");
            out.flush();
        } catch (IOException e) {
            tcpConnectionObserver.unexpectedError(TCPConnection.this, e);
            disconnect();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            tcpConnectionObserver.unexpectedError(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return getNickname() + socket.getInetAddress() + ": " + socket.getPort();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isNumberSet() {
        return isNumberSet;
    }

    public void setNumberSet(boolean numberSet) {
        isNumberSet = numberSet;
    }

    public int getCaesarKey() {
        return caesarKey;
    }

    public void setCaesarKey() {
        this.caesarKey = (int) (Math.random() * 100);
    }

    public String getIntroductory() {
        return introductory;
    }

    public void setIntroductory(String introductory) {
        this.introductory = introductory;
    }
}
