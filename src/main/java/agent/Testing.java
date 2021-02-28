package agent;

import java.util.ArrayList;
import java.util.List;

public class Testing {

    public static void main(String[] args) {
        List<Block> blockchain = new ArrayList<>();

        print(blockchain);

    }

    public static void print(List<Block> b) {
        System.out.println("Ya Fayiz this is the latest update:" + b.toString());

    }


}
