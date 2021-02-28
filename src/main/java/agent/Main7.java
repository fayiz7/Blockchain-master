package agent;

public class Main7 {

    public static void main(String[] args) {
        System.out.println("teset");
        Node node = new Node(8);
        node.getChildren();
        node.getData();
        node.addChild(new Node(7));
        System.out.println(node.getChildren());
    }

}
