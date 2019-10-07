package client;
import server.TCPConnection;
import server.TCPConnectionObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionObserver {

    private static final String ip = "localhost";
    private static final int port = 53545;
    private static final int width = 600;
    private static final int height = 400;

    private final JTextArea textArea = new JTextArea();
    private  final JTextField input = new JTextField();

    private TCPConnection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    public ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(width, height);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setForeground(Color.GREEN);
        textArea.setBackground(Color.BLACK);

        add(textArea, BorderLayout.CENTER);
        input.addActionListener(this);
        add(input, BorderLayout.SOUTH);
        add(new JScrollPane(textArea));

        try {
            connection = new TCPConnection(this, ip, port);
        } catch (IOException e) {
            printMessage("Connection unexpectedError: " + e);
        }

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String message = input.getText();

        if (message.equals("")) return;

        input.setText(null);
        connection.sendMessage(message);

    }

    @Override
    public void connectionReady(TCPConnection tcpConnection) {
        printMessage("You have entered lobby..." + "\n         /JOIN [your nickname] [nickname of player in game] - to start game" + "\n         /ASK - to see all present players" + "\n         /QUIT - return to lobby" );
    }

    @Override
    public void recievedMessage(TCPConnection tcpConnection, String message) {
        printMessage(message);
    }

    @Override
    public void disconnected(TCPConnection tcpConnection) {
        printMessage("Disconnected");
    }

    @Override
    public void unexpectedError(TCPConnection tcpConnection, Exception e) {
        printMessage("Connection unexpectedError: " + e);
    }

    private synchronized void printMessage(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(message + "\n");
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        });
    }


}
