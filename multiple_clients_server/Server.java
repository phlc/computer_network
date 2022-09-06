import java.io.*;
import java.net.*;

class Hotel{
  private String hotel;
  private String city;
  private long reserved;

  Hotel(String hotel, String city){
    this.hotel = hotel;
    this.city = city;
    this.reserved = -1;
  }

  public void setReserved(long reserved) {
      this.reserved = reserved;
  }

  public long getReserved() {
      return reserved;
  }

  @Override
  public String toString() {
      return hotel + ", " + city;
  }
}

//Para adotar todos os comprtamentos dessa classe
public class Server extends Thread {

  //Static Fields
  private static Hotel[] hotels = new Hotel[5];
  private static ServerSocket server;
  private static long ids = 0;

  //Object Fields
  private Socket con;
  private BufferedReader socketReader;
  private BufferedWriter socketWriter;

  // Constructor
  public Server(Socket con){
    this.con = con;
    try {
      socketReader = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
      socketWriter = new BufferedWriter(new OutputStreamWriter(this.con.getOutputStream()));
    
      //informar id
      socketWriter.write(++ids + "\n");
      socketWriter.flush();
    }catch (Exception e) {}
  }

  // listAvailables
  private void listAvailables() throws IOException{
    socketWriter.write("Nossos Hotes Disponíveis:\n");
    for(int i=0; i<hotels.length; i++){
      if(hotels[i].getReserved() == -1){
        socketWriter.write(i + "- " + hotels[i].toString() + "\n");
      }
    }
    socketWriter.write("Para Reservar Digite o Número e aperte ENTER\n");
    socketWriter.flush();
  }


  // run
  public void run(){
    String inMsg = null;    
    try{

      listAvailables();      

      while(!"sair".equalsIgnoreCase(inMsg)){
        inMsg = socketReader.readLine();
        
      }

    }catch (Exception e) {}
  }//end método run




  // Main
  public static void main(String []args) {
    int port = 12345;

    hotels[0] = new Hotel("Ibis", "Belo Horizonte");
    hotels[1] = new Hotel("Ouro Minas", "Belo Horizonte");
    hotels[2] = new Hotel("Ibis", "Rio de Janeiro");
    hotels[3] = new Hotel("Hyatt", "Nova Iorque");
    hotels[4] = new Hotel("Seasons", "Roma");


    try{
      server = new ServerSocket(port);
      System.out.println("Servidor ativo na porta: " + port);

      while(true){
        System.out.println("Aguardando conexão...");
        Socket con = server.accept();
        System.out.println("Cliente conectado...");
        Thread t = new Server(con);
          t.start();
      }
    }
    catch (Exception e){}
  }
}