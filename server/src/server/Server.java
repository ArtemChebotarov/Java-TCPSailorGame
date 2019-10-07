package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements TCPConnectionObserver{

    public static final ArrayList<TCPConnection> connections = new ArrayList<>(); //здесь хранятся все подключения
    private boolean inGame;
    private boolean isGateOpen = true;
    private boolean timerStarted;
    int randomCountFrom;
    List<List<TCPConnection>> rounds;
    List copyOfConnections;
    Results results;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(53545)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept()); //вечный цикл, который создает новое подключение при каждом запуске клиента
                }
                catch (IOException e) {
                    System.out.println("TCPConnection unexpectedError: " + e);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void connectionReady(TCPConnection tcpConnection) {
            if(connections.size() == 0) {
                connections.add(tcpConnection); // таким образом инициализирую первого игрока
            }

            boolean status = false; //отправляю сообщения только тем, кто подключены к лобби
            for(int i = 0; i < connections.size(); i++) {
                if(tcpConnection.getSocket().getPort() == connections.get(i).getSocket().getPort()){
                    status = true;
                    break;
                }
            }

            if(status) {
                sendToAllMessage(getCurrentTime() + " Player connected: " + tcpConnection + ", players online: " + connections.size());
                tcpConnection.sendMessage(getCurrentTime() + " You are first player! Please, FIRSTLY set your nickname by using /SET [nickname]");
            }

    }

    @Override
    public synchronized void recievedMessage(TCPConnection tcpConnection, String message) {

        if(!message.contains("/")) { // здесь отправляю сообщения всем, если они не касаются команд
            boolean status = false;
            for(int i = 0; i < connections.size(); i++) {  //отправляем только тогда, когда уже подключился к лобби
                if(tcpConnection.getSocket().getPort() == connections.get(i).getSocket().getPort()){
                    status = true;
                    break;
                }
            }
            if(isGateOpen && status) {
                sendToAllMessage(getCurrentTime() + " " + tcpConnection.getNickname() + ": " + message);

            }
            if(!isGateOpen && tcpConnection.isTurn()) {
                try {
                    int num = Integer.parseInt(message);
                    if(!tcpConnection.isNumberSet()) {
                        tcpConnection.setNumber(num + tcpConnection.getCaesarKey());
                        tcpConnection.setNumberSet(true);
                        tcpConnection.sendMessage(getCurrentTime() + " Your number is " + (tcpConnection.getNumber() - tcpConnection.getCaesarKey()) + "\n");
                    }
                    else
                        tcpConnection.sendMessage("You have already set your number");

                    int firstNum = rounds.get(0).get(0).getNumber() - rounds.get(0).get(0).getCaesarKey();
                    int secondNum = rounds.get(0).get(1).getNumber() - rounds.get(0).get(1).getCaesarKey();

                    if(rounds.size() != 0) {
                        if (rounds.get(0).get(0).isNumberSet() && rounds.get(0).get(1).isNumberSet()) {
                            if((firstNum + secondNum) % 2 == 0) {
                                if(randomCountFrom == 0) {
                                    rounds.get(0).get(0).sendMessage(getCurrentTime() + " Player " +  rounds.get(0).get(1).getNickname() + " won!\n");
                                    rounds.get(0).get(1).sendMessage(getCurrentTime() + " Player " +  rounds.get(0).get(1).getNickname() + " won!\n");
                                    results.addResult(rounds.get(0).get(0), rounds.get(0).get(1), 1);
                                }
                                else {
                                    rounds.get(0).get(0).sendMessage(getCurrentTime() + " Player " +  rounds.get(0).get(0).getNickname() + " won!\n");
                                    rounds.get(0).get(1).sendMessage(getCurrentTime() + " Player " +  rounds.get(0).get(0).getNickname() + " won!\n");
                                    results.addResult(rounds.get(0).get(0), rounds.get(0).get(1), 0);
                                }
                            }
                            else if ((firstNum + secondNum) % 2 == 1) {
                                if(randomCountFrom == 0) {
                                    rounds.get(0).get(0).sendMessage(getCurrentTime() + " Player " +  rounds.get(0).get(0).getNickname() + " won!\n");
                                    rounds.get(0).get(1).sendMessage(getCurrentTime() + " Player " +  rounds.get(0).get(0).getNickname() + " won!\n");
                                    results.addResult(rounds.get(0).get(0), rounds.get(0).get(1), 0);
                                }
                                else {
                                    rounds.get(0).get(0).sendMessage(getCurrentTime() + " Player " +  rounds.get(0).get(1).getNickname() + " won!\n");
                                    rounds.get(0).get(1).sendMessage(getCurrentTime() + " Player " +  rounds.get(0).get(1).getNickname() + " won!\n");
                                    results.addResult(rounds.get(0).get(0), rounds.get(0).get(1), 1);
                                }
                            }

                            rounds.get(0).get(0).setNumberSet(false);
                            rounds.get(0).get(1).setNumberSet(false);
                            rounds.get(0).get(0).setTurn(false);
                            rounds.get(0).get(1).setTurn(false);

                            rounds.remove(0);
                            if(rounds.size() != 0) {
                                continueGame();
                            }
                            else {
                                inGame = false;
                                isGateOpen = true;
                                sendToAllMessage(results.toString());
                                startGame();
                            }
                        }
                    }



                }catch (NumberFormatException e) {
                    tcpConnection.sendMessage("Please, choose an INTEGER");
                }
            }
        }
        else { // здесь я обрабатываю команды
            if(message.equals("/ASK")) {  //команда показывает всех игроков онлайн
                String output = "Players online:";
                for (TCPConnection connection : connections) {
                    output += "\n                   " + connection.toString() + " intoductory: " + connection.getIntroductory();

                }
                tcpConnection.sendMessage(output);
            }
            else if(message.matches("/JOIN(.*)")){
                Pattern p = Pattern.compile("/JOIN\\s([A-Za-z]+)\\s([A-Za-z]+)");
                Matcher m = p.matcher(message);
                String nickname = "";

                String playerInGame = "";
                    while (m.find()) {
                        nickname = m.group(1);
                        playerInGame = m.group(2);

                        break;
                    }


                boolean nicknameStatus = true;
                for(int i = 0; i < connections.size(); i++) {
                    if(nickname.equals(connections.get(i).getNickname())){
                        nicknameStatus = false;
                        break;
                    }
                }



                boolean connectionStatus = true;
                for(int i = 0; i < connections.size(); i++) {
                    if(tcpConnection.getSocket().getPort() == connections.get(i).getSocket().getPort()){
                        connectionStatus = false;
                        break;
                    }
                }

                boolean isPlayerExist = false;
                for(int i = 0; i < connections.size(); i++) {
                    if(playerInGame.equals(connections.get(i).getNickname())){
                        isPlayerExist = true;
                        break;
                    }
                }

                boolean isFirstReady = true;
                if(!connections.get(0).isReady()) {
                    isFirstReady = false;
                }

                // ПРОВЕРКА. Подключившийся клиент не может подключаться снова
                if (connectionStatus && isPlayerExist && nicknameStatus && isFirstReady && isGateOpen) { // индивидуальное имя + нельзя подключиться снова
                    tcpConnection.setReady(true);                                                     //если уже находишься в игре + проверка игрока с данным портом
                    tcpConnection.setNickname(m.group(1));
                    tcpConnection.setIntroductory(playerInGame);//+ проверка готовности первого игрока + проверка того, открыт ли набор игроков
                    connections.add(tcpConnection);
                    sendToAllMessage(getCurrentTime() + " Player connected: " + tcpConnection + ", players online: " + connections.size());
                    if(!inGame && !timerStarted)
                        startGame();

                }
                else if(!nicknameStatus) {
                    tcpConnection.sendMessage("Nickname already in use");
                }
                else if(!connectionStatus) {
                    tcpConnection.sendMessage("You are already connected");
                }
                else if(!isPlayerExist) {
                    tcpConnection.sendMessage("Player with such nickname does not exist in game");
                }
                else if(!isFirstReady) {
                    tcpConnection.sendMessage("Introductory is not ready, try to connect later");
                }
                else if(!isGateOpen){
                    tcpConnection.sendMessage("Game is started. Please, try to connect later");
                }


            }

            else if(message.equals("/QUIT")) {
                boolean connectionStatus = true;
                for(int i = 0; i < connections.size(); i++) {
                    if(tcpConnection.getSocket().getPort() == connections.get(i).getSocket().getPort()){
                        connectionStatus = false;
                        break;
                    }
                }
                if(!inGame) {
                    connections.remove(tcpConnection);
                    if (!connectionStatus) {
                        sendToAllMessage(getCurrentTime() + " Player disconnected: " + tcpConnection + ", players online: " + connections.size());
                        tcpConnection.sendMessage("You have been disconected");  //рассылаем сообщение только тем, кто в игре
                    }
                }
                else
                    tcpConnection.sendMessage("You can't leave until the end of game");
            }

            else if(message.matches("/SET(.*)")) {
                Pattern p = Pattern.compile("/SET\\s([A-Za-z]+)");  //установка имени для первого игрока
                Matcher m = p.matcher(message);

                if(tcpConnection == connections.get(0)) {
                    while (m.find()) {

                        tcpConnection.setNickname(m.group(1));
                        tcpConnection.setIntroductory(tcpConnection.getNickname());
                        tcpConnection.sendMessage("You chose nickname " + m.group(1) + ". Now you can start playing!");
                        tcpConnection.setReady(true);
                        break;
                    }

                }
            }
            else {
                tcpConnection.sendMessage("No such command: " + message);
            }
        }

    }

    @Override
    public synchronized void disconnected(TCPConnection tcpConnection) {

        boolean connectionStatus = true;
        for(int i = 0; i < connections.size(); i++) {
            if(tcpConnection.getSocket().getPort() == connections.get(i).getSocket().getPort()){
                connectionStatus = false;
                break;
            }
        }
        connections.remove(tcpConnection);

        if(inGame) {
            inGame = false;
            isGateOpen = true;
            timerStarted = false;

            sendToAllMessage(getCurrentTime() + " One of players disconnected. Game will be started again");
            if(connections.size() >= 2)
                startGame();
            else
                sendToAllMessage(getCurrentTime() + " Waiting for players...");

        }

        if(!connectionStatus)
            sendToAllMessage(getCurrentTime() + " Player disconnected: " + tcpConnection + ", players online: " + connections.size());
    }

    @Override
    public synchronized void unexpectedError(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection unexpectedError: " + e);
    }



    private void sendToAllMessage(String message) {
        for (TCPConnection connection : connections) {
            connection.sendMessage(message);
        }
    }

    private String getCurrentTime() {
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm:ss a");

        return formatForDateNow.format(dateNow);
    }

    private void startGame() {
        Timer timer = new Timer();



        if(connections.size() >= 2) {
            timerStarted = true;
            sendToAllMessage(getCurrentTime() + " The game will start in 15 seconds");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timerStarted = false;
                    inGame = true;

                    results = new Results();

                    if(connections.size() >= 2) {
                        copyOfConnections = new ArrayList();
                        copyOfConnections.addAll(connections);
                        rounds = rounds(copyOfConnections);
                        sendToAllMessage("\nFamiliarize with list of queues: " + rounds + "\n");

                        isGateOpen = false;

                        continueGame();
                    }
                    else {
                        inGame = false;
                        sendToAllMessage(getCurrentTime() + " Waiting for players...");
                    }

                }
            }, 15000);
        }

    }

    private void continueGame() {
            randomCountFrom = (int) (Math.random() * 2);

            rounds.get(0).get(0).sendMessage(getCurrentTime() + " You play with player " + rounds.get(0).get(1).getNickname() + "... Counting from player " + rounds.get(0).get(randomCountFrom).getNickname() +
                    "\n\t\t\t\t\t\t\t\tPlease, choose one integer\n");
            rounds.get(0).get(1).sendMessage(getCurrentTime() + " You play with player " + rounds.get(0).get(0).getNickname() + "... Counting from player " + rounds.get(0).get(randomCountFrom).getNickname() +
                    "\n\t\t\t\t\t\t\t\tPlease, choose one integer\n");

            rounds.get(0).get(0).setTurn(true);
            rounds.get(0).get(1).setTurn(true);

        }

    private List<List<TCPConnection>> rounds(List<TCPConnection> list) {
            List<List<TCPConnection>> out = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                for (int j = 1; j < list.size(); j++) {
                    List<TCPConnection> tmp = new ArrayList();
                    tmp.add(list.get(i));
                    tmp.add(list.get(j));
                    out.add(tmp);
                }
                list.remove(i);
                i--;
            }
            return out;
    }
}
