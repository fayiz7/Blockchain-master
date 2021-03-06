package agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static agent.Message.MESSAGE_TYPE.INFO_NEW_BLOCK;
import static agent.Message.MESSAGE_TYPE.READY;
import static agent.Message.MESSAGE_TYPE.REQ_ALL_BLOCKS;
import static agent.Message.MESSAGE_TYPE.RSP_ALL_BLOCKS;

public class Agent {
    public String hash1="";
    public String hash2="";
    public String hash3="";
    public String hash4="";
    private String name;
    private String address;
    private int port;
    private List<Agent> peers;
    public List<Block> blockchain = new ArrayList<>();

    private ServerSocket serverSocket;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    private boolean listening = true;


    public Agent() {
    }

    Agent(final String name, final String address, final int port, final Block root, final List<Agent> agents) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.peers = agents;
        System.out.println("agent const");
        blockchain.add(root);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public List<Block> getBlockchain() {
        return blockchain;
    }

    Block createBlock() {
        if (blockchain.isEmpty()) {
            return null;
        }

        Block previousBlock = getLatestBlock();
        if (previousBlock == null) {
            return null;
        }

        final int index = previousBlock.getIndex() + 1;
        final int order = previousBlock.getid()+1;
        hash1= previousBlock.getHash();
        final int a = Integer.parseInt(Integer.toString(previousBlock.getid()) + Integer.toString(1));
        final Block block = new Block(index, order,previousBlock.getHash(), name);
        System.out.println(String.format("%s created new block %s", name, block.toString()));
        broadcast(INFO_NEW_BLOCK, block);
        return block;
    }

    void addBlock(Block block) {
        if (isBlockValid(block)) {
            blockchain.add(block);
            Testing.print(blockchain);
            
        }
    }

    void startHost() {
        executor.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println(String.format("Server %s started", serverSocket.getLocalPort()));
                listening = true;
                while (listening) {
                    System.out.println(String.format("Server2 %s started", serverSocket.getLocalPort()));
                    final AgentServerThread thread = new AgentServerThread(Agent.this, serverSocket.accept());
                    thread.start();
                    System.out.println(String.format("Server3 %s started", serverSocket.getLocalPort()));
                }
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Could not listen to port " + port);
            }
        });
        broadcast(REQ_ALL_BLOCKS, null);
    }

    void stopHost() {
        listening = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Block getLatestBlock() {
        if (blockchain.isEmpty()) {
            return null;
        }
        // four arraylist. (n E W S)
        //blockchain.get(blockhainnorth.size()-1) //
        // base node has n, e, w, s
        // you add a node to east
        // now how mnay north options you have? TWO (one from base and one from the east blcok)
        // if you want to reset
        // store ht eincomplete blocks in n arraylist
        // at any node, i get the north of that node.
        // if you want to move early node, then go through the arraylist and get the node and get the north or whaver of it.
        // teh arraylist should be a structure (node id, north, e , w, s)
        // if you wadd a norde to the west, you change the structure (node i, noth, new node id, south)
        for (int i = 0, j = 0 ;true;i++){
            Block b=blockchain.get(i).getChildren().get(j);
            for ( j = 0 ; j<4 ; j++){
                if (blockchain.get(i).getChildren().get(j)==null){
                    System.out.println("got the lateStblock ");
                    return b;
                }

                b = blockchain.get(i).getChildren().get(j);

            }
        }
        //return blockchain.get(blockchain.size() - 1);

    }

    private boolean isBlockValid(final Block block) {
        final Block latestBlock = getLatestBlock();
        if (latestBlock == null) {
            return false;
        }
        final int expected = latestBlock.getIndex() + 1;
        if (block.getIndex() != expected) {
            System.out.println(String.format("Invalid index. Expected: %s Actual: %s", expected, block.getIndex()));
            return false;
        }
        if (!Objects.equals(block.getPreviousHash(), latestBlock.getHash())) {
            System.out.println("Unmatched hash code");
            return false;
        }
        return true;
    }

    private void broadcast(Message.MESSAGE_TYPE type, final Block block) {
        System.out.println("broadcast");
        peers.forEach(peer -> sendMessage(type, peer.getAddress(), peer.getPort(), block));
    }

    private void sendMessage(Message.MESSAGE_TYPE type, String host, int port, Block... blocks) {
        try (
                final Socket peer = new Socket(host, port);
                final ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(peer.getInputStream())) {
            Object fromPeer;
            System.out.println("*** AGENT");
            while ((fromPeer = in.readObject()) != null) {
                if (fromPeer instanceof Message) {
                    final Message msg = (Message) fromPeer;
                    System.out.println(String.format("%d received: %s", this.port, msg.toString()));
                    if (READY == msg.type) {
                        out.writeObject(new Message.MessageBuilder()
                                .withType(type)
                                .withReceiver(port)
                                .withSender(this.port)
                                .withBlocks(Arrays.asList(blocks)).build());
                    } else if (RSP_ALL_BLOCKS == msg.type) {
                        if (!msg.blocks.isEmpty() && this.blockchain.size() == 1) {
                            blockchain = new ArrayList<>(msg.blocks);
                        }
                        break;
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unknown host %s %d", host, port));
        } catch (IOException e) {
            System.err.println(String.format("%s couldn't get I/O for the connection to %s. Retrying...%n", getPort(), port));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
