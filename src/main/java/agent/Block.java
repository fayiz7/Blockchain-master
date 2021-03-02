package agent;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Block implements Serializable {
    private static final long serialVersionUID = 1L;

    private int index;
    private int id;
    private Long timestamp;
    private String hash;
    private String previousHash;
    private String creator;
    public Block parent;
    public List<Block> children;
    public Block() {
    }
    public void addChild(Block child) {
        child.setParent(this);
        children.add(child);
    }

    public List<Block> getChildren() {
        return children;
    }

    public Block getParent() {
        return parent;
    }

    public void setParent(Block block) {
        this.parent=block;
    }


    @Override
    public String toString() {
        return "Block{" +
                "index=" + index +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", creator=" + creator +
//                ", hash='" + hash + '\'' +
//                ", previousHash='" + previousHash + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Block block = (Block) o;
        return index == block.index
                && timestamp.equals(block.timestamp)
                && hash.equals(block.hash)
                && previousHash.equals(block.previousHash)
                && creator.equals(block.creator);
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + id;
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + hash.hashCode();
        result = 31 * result + previousHash.hashCode();
        result = 31 * result + creator.hashCode();
        return result;
    }

    public Block(int index,int id, String preHash, String creator) {
        this.index = index;
        this.id = id;
        this.previousHash = preHash;
        this.creator = creator;
        this.children=new ArrayList<>();
        timestamp = System.currentTimeMillis();
        hash = calculateHash(String.valueOf(index) + previousHash + String.valueOf(timestamp));
        System.out.println("index "+index);
        System.out.println("id "+id);
        System.out.println("preHash "+preHash);
        System.out.println("hash "+hash);
        System.out.println("creator "+creator);
        System.out.println("timestamp "+timestamp);

        System.out.println("here");
        if (this.getParent()!=null){
            System.out.println(" parent id "+this.parent.getid());
        }
        System.out.println(this.getChildren());

        for (int i =0 ; i <4 ; i++){
            Block b = new Block();
            this.children.add(b);
            //Block b = this.getChildren().get(i);//=new Block();
            this.getChildren().get(i).setParent(this);
        }
        //System.out.println(this.parent);
    }

    public String getCreator() {
        return creator;
    }

    public int getIndex() {
        return index;
    }

    public int getid() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    private String calculateHash(String text) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return "HASH_ERROR";
        }

        final byte bytes[] = digest.digest(text.getBytes());
        final StringBuilder hexString = new StringBuilder();
        for (final byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

/*

Ideas:

i can once i mine a block , immediately create its Arraylist of children
blocks with parent hash of current block , and leave these children blocks with only parent hash
so i can come back and fill other data members



 */