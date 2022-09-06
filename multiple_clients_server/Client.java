//Client

import java.io.*;
import java.net.Socket;

public class Client {
  // Static Methods
  // Clear
  private static void clear(){
    System.out.print("\033[H\033[2J");  
    System.out.flush();  
  }

  //Object Fields
  private BufferedReader clientReader;
  private String ip = "127.0.0.1";
  private int port = 12345;
  private Socket socket;
  private BufferedReader socketReader;
  private BufferedWriter socketWriter;
  private boolean ended;
  private long id;

 
  
  //Constructor
  public Client(){
    clientReader = new BufferedReader(new InputStreamReader(System.in));
    ended = false;
    id = -1;
  }

  // init
  private void init() throws IOException{
    clear();
    System.out.println("Bem Vindo ao Sistemas de Compras Online");
    System.out.println("Conectando ao Servidor...");
  }

  // connect
  public void connect() throws IOException{
    while (!ended){
      try{
        init();
        socket = new Socket(ip,port);
        socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        id = Long.parseLong(socketReader.readLine());
        if(id == -1) throw new Exception(); 
        System.out.println("Você está conectado");
        System.out.println("Seu id é: "+id);
        System.out.println("Aperte ENTER para Continuar");
        online();
        
      }catch(Exception e){
        System.out.println("Erro ao Conectar");
        System.out.println("Aperte \"ENTER\" para tentar novamente ou Digite \"Sair\" para desistir");
        if(clientReader.readLine().toLowerCase().equals("sair")) ended = true;
        clear();
      }
      finally{
        close();
      }
    }
  }

  // online
  private void online() throws IOException{
    String inMsg = null;
    String outMsg = null;
    while(!ended){
      inMsg = receive();
      if(inMsg != null){
        System.out.println(inMsg);
      }
      outMsg = read();
      if(outMsg != null){
        outMsg = outMsg.toLowerCase();
        send(outMsg+"\n");
        if(outMsg.equals("sair")){
          close();
          ended = true;
          clear();
          System.out.println("\n\nObrigado\n\n");
        }
      }
    }
  }

  private String read() throws IOException{
    String msg = null;
    if (clientReader.ready()){
      msg = clientReader.readLine();
    }
    return msg;
  }


  // send
  public void send(String msg) throws IOException{
    socketWriter.write(msg);
    socketWriter.flush();
  }



  // receive
  public String receive() throws IOException{
    String msg = null;
    if(socketReader.ready()){
      msg = socketReader.readLine();
    }
    return msg;
  }

  // close
  private void close(){
    try{
      send("end\n");
      socketReader.close();
      socketWriter.close();
      socket.close();
    }catch(Exception e){}

  }
  
  public static void main(String []args) throws IOException{

    Client app = new Client();
    app.connect();

  }
} 
