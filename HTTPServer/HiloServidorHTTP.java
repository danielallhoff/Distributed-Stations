import java.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class HiloServidorHTTP extends Thread{
    
    private String controlador = "controladorSD";
    private String versionHTTP = "HTTP1.1";
    private int puertoControlador;
    private String dirControlador;
    private String estadoHTTP = "200 OK";
    private Socket skCliente;
    private MyHTTPServer server;
    public HiloServidorHTTP(MyHTTPServer server, Socket skCliente, int puertoControlador, String dirControlador){
        this.skCliente = skCliente;
        this.puertoControlador = puertoControlador;
        this.dirControlador = dirControlador;
        this.server = server;
    }
    //Resultado de la petición
    private String respuestaHTTP(String contenido){
        try{
            String lineaEstado = versionHTTP + " " + estadoHTTP + "\n";
            String cabeceras = "Connection: close\n";
            cabeceras+="Content-Length: "  + contenido.getBytes("UTF-8").length+"\n";
            cabeceras+="Content-Type: text/html; charset=utf-8\n";
            cabeceras+="Server: Apache\n\n";
            return lineaEstado + cabeceras + contenido;
        }
        catch(Exception e){
            System.out.println(e.toString());
        }

        return null;
        
    }

    //Manejar peticiones estáticas
    private String estaticoHTTP(String path){
        String contenido = "";
        if(path==null || path.length() == 0) path = "index.html";
        try{
            File archivo = new File(path);

            FileReader fr = new FileReader(archivo);
            BufferedReader br = new BufferedReader(fr);
            String linea;
            while((linea=br.readLine()) !=null){
                contenido += linea;
            }
            if( null != fr ){   
                   fr.close();
            }
        }catch(FileNotFoundException e){
            estadoHTTP = "404 Not Found";
        }catch(Exception e){
            estadoHTTP = "500 Internal Server Error";
        }
        
        return contenido;
    }
    //Encapsular path a controlador
    private String encapsularPath(String path){
        String res = path;
        if(path == "" || path.length() == 0 ){
          res = "index";
        }
        if(!res.equals("index")){
            String [] parametros = path.split("\\?");

            String variable = parametros[0];
            String resto = parametros[1];
            if(variable.equals("set")){
                res = "set,"+resto; 
            }
            else{
                res = "get,"+ resto.split("=")[1] +"," + variable;
            }
        }

        return res;
    }
    //Manejar peticiones dinamicas
    private String dinamicoHTTP(String path){
        System.out.println("Path dinamica:" + path);
        String salida = "";
        try{
            Socket skControlador = new Socket(dirControlador,puertoControlador);
            escrituraSocket(skControlador, encapsularPath(path));
            salida = lecturaSocket(skControlador,true);
            skControlador.close();
        }
        catch(Exception e){
            estadoHTTP = "500 Internal Server Error";
            e.printStackTrace();

        }
        
        return salida;
    }

    //Manejo de peticioens http: Peticion formada por MÉTODO(SALTO LINEA)CABECERAS(\N)(LINEA BLANCO)(CUERPO)
    private String manejarPeticionHTTP(String petHTTP){
        System.out.println(petHTTP);
        String[] partes = petHTTP.split(" ");
        String resultado = "";
        String contenido;
        if(partes[0].equals("GET")){
            //Leer recurso estático
            try{
                String path = partes[1];
                path = path.substring(1);

                String version = partes[2];
                //Estático o dinámico
                if(path.equals(this.controlador)){
                    contenido = dinamicoHTTP("index");
                }
                else if(path.contains(this.controlador)){
                    contenido = dinamicoHTTP(path.substring(this.controlador.length()+1));
                }
                else{
                    contenido = estaticoHTTP(path);
                }
                return respuestaHTTP(contenido);


            }catch(Exception e){
                estadoHTTP = "500 Internal Server Error";
            }  
        }
        else{
            estadoHTTP = "501 Not implemented";
        }
        return resultado;
    }
    //Ejecución de cada hilo
    public void run(){
        try{
            String peticionHTTP = lecturaSocket(skCliente, false);
            System.out.println("Peticion http: " + peticionHTTP);
            String resultado = manejarPeticionHTTP(peticionHTTP);
            //Resultado HTTP;
            escrituraSocket(skCliente, resultado);
            skCliente.close();
            server.liberarHilo();
        }
        catch(Exception e){
            estadoHTTP = "500 Internal Server Error";
            System.out.println("Error run:" + e.toString());
            e.printStackTrace();
            server.liberarHilo();
        }
    }
    
    public static String lecturaSocket(Socket sk, boolean variasLineas){
        String result = "";
        try{
            InputStream inputStream = sk.getInputStream();
            BufferedReader d = new BufferedReader(
                    new InputStreamReader(inputStream));
            if(variasLineas){
                String linea;
                while((linea = d.readLine())!=null){
                    result += linea + "\n";    
                }
            }
            else{
                result= d.readLine();
            }            
        }catch(Exception e){
            System.out.println("Error lectura :" + e.toString());
        }
        return result;
    }

    public static void escrituraSocket(Socket sk, String mensaje){
        try
        {
            PrintWriter out =
                new PrintWriter(sk.getOutputStream(), true);
            out.println(mensaje); 
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }    
    }

}
