import java.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class Controller{

		public Controller(int puerto, String dirRMI, int puertoRMI){
			try{
				ServerSocket sk = new ServerSocket(puerto);
				for(;;){
					Socket skCliente = sk.accept();
					Thread t = new HiloController(skCliente, dirRMI, puertoRMI);
					t.start();
				}
				
			}catch(Exception e){
				System.out.println(e);
			}
		}
		//Puerto de escucha y puerto de conexión al registro RMI
		public static void main(String args[]){
			int puerto = 2001, puertoRMI = 1099;
			String dirRMI = "localhost";
			try{
				for(String arg: args){
					String [] params = arg.split("=");
					switch(params[0]){
						case "puerto": puerto = Integer.parseInt(params[1]); break;
						case "rmi": 
							String [] direccion = params[1].split(":");
							dirRMI = direccion[0];
							puertoRMI = Integer.parseInt(direccion[1]);
						break;
							default: System.out.println("Argumento no disponible: " + params[0]);
					}
				}
            	System.out.println("Args: puerto="+ puerto + " rmi="+dirRMI + ":"+puertoRMI);
				Controller controlador = new Controller	(puerto, dirRMI, puertoRMI);
            }catch(Exception e){
                System.out.println("Args: puerto rmi");
            }		
		}



}