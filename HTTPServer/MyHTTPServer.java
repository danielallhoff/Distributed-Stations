import java.*;
import java.io.*;
import java.net.*;

public class MyHTTPServer{
		private int numHilos;
		public void liberarHilo(){--numHilos;};
        //Abrimos httpserver en un puerto de forma concurrente + fichero configuracion servidor http
		public MyHTTPServer(int puertoHTTP, int totalHilos, int puertoControlador, String dirControlador){
			try{
				ServerSocket server = new ServerSocket(puertoHTTP);
				numHilos = 0;

					//Para cada cliente se crea un hilo de ejecución
                    for(;;){
                        if(numHilos < totalHilos){
                        	Socket skCliente = server.accept();
                        	numHilos++;
                        	Thread t = new HiloServidorHTTP(this, skCliente, puertoControlador, dirControlador);
                        	t.start();
                        	
                        }
                    }
				
			}
			catch(Exception e){
				System.out.println("Error hilo: " + e.toString());
			}
			
		}
			//Puerto de escucha y puerto de conexión a controller, tambien el número de hilos. puerto= , hilos= , controlador=
		public static void main(String args[]){
			int puertoHTTP = 8000, puertoControlador = 2001, totalHilos = Integer.MAX_VALUE;
			String dirControlador = "localhost";
			try{
				for(String arg: args){
					String [] params = arg.split("=");
					switch(params[0]){
						case "puerto": puertoHTTP = Integer.parseInt(params[1]); break;
						case "hilos": totalHilos = Integer.parseInt(params[1]); break;
						case "controlador": 
							String [] direccion = params[1].split(":");
							dirControlador = direccion[0];
							puertoControlador = Integer.parseInt(direccion[1]);
						break;
							default: System.out.println("Argumento no disponible: " + params[0]);
					}
				}
				System.out.println("Args: puerto="+ puertoHTTP + " hilos=" + totalHilos + " controlador="+dirControlador + ":"+puertoControlador);
				MyHTTPServer myserver = new MyHTTPServer(puertoHTTP, totalHilos, puertoControlador, dirControlador);
			}
			catch(Exception e){
				System.out.println("Args: puerto=x hilos=y controlador=0.0.0.0:0");
			}
		}

		public static String lecturaSocket(Socket sk){
			String result = "";
	        try{
	             InputStream inputStream = sk.getInputStream();
	            BufferedReader d = new BufferedReader(
	                    new InputStreamReader(inputStream));
	            result = d.readLine();
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
	            /*OutputStream outputStream = sk.getOutputStream();
	            DataOutputStream dataoutput = new DataOutputStream(outputStream);
	            dataoutput.writeUTF(mensaje);            */
	        }
	        catch (Exception e)
	        {
	            System.out.println(e.toString());
	        }    
	    }
}