import java.io.*;
public class Test{


    public static void main(String[] args){
        new Thread(){
            public void run(){
                while(true){
                    System.out.print("A");
                    try {Thread.sleep(100);}
                    catch(Exception e) {break;}
                }
            }
        }.start();
        System.out.println("HEOJAOIHAOEHJGA");
    }
}