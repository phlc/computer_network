import java.io.*;
import java.net.*;

class Event{
  private String name;
  private String city;
  private int available;

  Event(String name, String city, int available){
    this.name = name;
    this.city = city;
    this.available = available;
  }

  public int getAvailable(){
    return this.available;
  }

  public boolean buy() {
    boolean ok = false;
    if(this.available>0){
      this.available--;
      ok = true;
    }
    return ok;
  }

  @Override
  public String toString() {
      return this.name + ", " + this.city + " - Disponíveis: " + this.available;
  }
}

//Para adotar todos os comprtamentos dessa classe
public class Server extends Thread {

  //Static Fields
  private static Event[] events = new Event[5];
  private static ServerSocket server;
  private static long ids = 0;

  //Object Fields
  private Socket con;
  private long id;
  private BufferedReader socketReader;
  private BufferedWriter socketWriter;

  // Constructor
  public Server(Socket con){
    this.id = ++ids;
    this.con = con;
    try {
      socketReader = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
      socketWriter = new BufferedWriter(new OutputStreamWriter(this.con.getOutputStream()));
    
      //informar id
      socketWriter.write(this.id + "\n");
      socketWriter.flush();
    }catch (Exception e) {}
  }

  // listAvailables
  private void listAvailables() throws IOException{
    socketWriter.write("Nossos Eventos Disponíveis:\n");
    for(int i=0; i<events.length; i++){
      if(events[i].getAvailable() > 0){
        socketWriter.write(i + "- " + events[i].toString() + "\n");
      }
    }
    socketWriter.write("Para Comprar Digite o Número do Evento e aperte ENTER\n");
    socketWriter.flush();
  }


  // run
  public void run(){
    String inMsg = null;    
    try{
      //Listar 
      listAvailables();      

      while(!"end".equalsIgnoreCase(inMsg)){
        inMsg = socketReader.readLine();
        
      }

      System.out.println("Cliente "+ this.id + " desconectado.");

    }catch (Exception e) {}
  }//end método run




  // Main
  public static void main(String []args) {
    int port = 12345;

    events[0] = new Event("Cruzeiro x Criciúma", "Belo Horizonte", 10);
    events[1] = new Event("Show Skank", "Belo Horizonte", 2);
    events[2] = new Event("Rock in Rio", "Rio de Janeiro", 3);
    events[3] = new Event("U2", "Nova Iorque", 4);
    events[4] = new Event("2Cello", "Roma", 2);


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