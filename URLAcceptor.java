package PdfFile;
import java.util.Scanner;
public class URLAcceptor {
    private Node head;
    private Node tail;

    public URLAcceptor() {
        head = null;
        tail = null;
    }

    // Function to add a URL to the end of the linked list
    public void addURL(String url) {
        Node newNode = new Node(url);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
    }

    // Function to remove and return the first URL from the Queue
    public String removeURL() {
        if (head == null) {
            throw new IllegalStateException("Queue is empty");
        }
        String url = head.URL;
        head = head.next;
        if (head == null) {
            tail = null; // Update tail if the list becomes empty
        }
        return url;
    }

    // Function to check if the linked list is empty
    public boolean isEmpty() {
        return head == null;
    }

    // Function to print all URLs in the linked list (for debugging purposes)
    public void printURLs() {
        Node current = head;
        while (current != null) {
            System.out.println(current.URL);
            current = current.next;
        }
    }
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        URLAcceptor u1 = new URLAcceptor();
        System.out.println("How many URL's do you want to Audit: ");
        int num=sc.nextInt();


        for(int i=0;i<num;i++){
            System.out.println("Enter URL Number "+(i+1));
            String url=sc.next();
            u1.addURL(url);
        }
        System.out.println("Printing the added urls");
        u1.printURLs();
    }
}

class Node {
    String URL;
    Node next;
    Node(String url) {
        this.URL = url;
        this.next = null;
    }
}
