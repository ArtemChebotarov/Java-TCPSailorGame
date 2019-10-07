package server;


import java.util.ArrayList;
import java.util.List;

public class Results extends ArrayList<ArrayList> {

    public void addResult(TCPConnection player0, TCPConnection player1, int winner) {
        List<Object> newResult = new ArrayList<>();
        newResult.add(player0);
        newResult.add(player1);
        newResult.add(winner);
        this.add((ArrayList) newResult);
    }

    @Override
    public String toString() {
        String result = "";
        for(int i = 0; i < this.size(); i++) {
            result += this.get(i).get(0) + " VS " + this.get(i).get(1) + " winner: " + this.get(i).get((int)this.get(i).get(2)) + "\n";
        }
        return "Results:\n" + result ;
    }
}
