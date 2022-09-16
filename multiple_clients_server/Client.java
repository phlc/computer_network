
import java.io.*;
import java.net.Socket;

/*
 * Client Class
 */

public class Client {
  // Static Methods
  /*
   * Clear - Limpa a tela do terminal
   */
  private static void clear(){
    System.out.print("\033[H\033[2J");  
    System.out.flush();  
  }

  //Object Fields
  private BufferedReader clientReader; //Leitor de inputs do terminal
  private String ip = "127.0.0.1";
  private int port = 12345;
  private Socket socket;
  private BufferedReader socketReader; //Escritor de output do socket
  private BufferedWriter socketWriter; //Leitor de inputs do socket
  private boolean ended;
  private long id;

 
  
  //Constructor
  public Client(){
    clientReader = new BufferedReader(new InputStreamReader(System.in));
    ended = false;
    id = -1;
  }

  /*
   * init - Limpa e mostra na tela mensagens iniciais
   */
  private void init() throws IOException{
    clear();
    System.out.println("Bem Vindo ao Sistemas de Compras Online");
    System.out.println("Conectando ao Servidor...");
  }

  /*
   * connect - Metodo que estabelece a conexão com o servidor
   * Se sucesso: chama o método online
   * Se erro: da opção de tentar novamente ou encerrar
   */
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

  /*
   * online - Metodo executa em loop durante a conexão até ler input "sair".
   * Le constantemente os buffers de entrada do socket e do terminal
   * receive() e read() utilizam o metodo .ready() da BufferedReader
   * que retorna true somente quando .read() não for bloquear a execução
   */
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

  /*
   * read - Testa se há algo no buffer de entrada do teclado 
   * @return null se buffer vazio || string lida
   */
  private String read() throws IOException{
    String msg = null;
    if (clientReader.ready()){
      msg = clientReader.readLine();
    }
    return msg;
  }

  /*
   * receive - Testa se há algo no buffer de entrada do socket 
   * @return null se buffer vazio || string lida
   */
  public String receive() throws IOException{
    String msg = null;
    if(socketReader.ready()){
      msg = socketReader.readLine();
    }
    return msg;
  }

  /*
   * send - Escreve e flush uma string no buffer de saída do socket
   */
  public void send(String msg) throws IOException{
    socketWriter.write(msg);
    socketWriter.flush();
  }

  

  /*
   * close - fecha a conexão
   */
  private void close(){
    try{
      send("end\n");
      socketReader.close();
      socketWriter.close();
      socket.close();
    }catch(Exception e){}

  }
  
  /*
   * main - Cria um cliente e conecta ao servidor
   */
  public static void main(String []args) throws IOException{

    Client app = new Client();
    app.connect();

  }
} 
