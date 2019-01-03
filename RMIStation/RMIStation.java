import java.rmi.*;
import java.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

//Cada RMIStation se registra en el servicio de nombres
public class RMIStation extends java.rmi.server.UnicastRemoteObject  implements InterfazRMI, Serializable {
	private int temperatura;
	private String name;
	private int humedad;
	private int luminosidad;
	private String pantalla;
	private String fichero;
	private int numRMI;
	private boolean modificado = true;
	//Dirección del servidor registro rmi
	private String dirRegistroRMI;
	private int puertoRMI;
	
	public String getNombre() throws RemoteException{
		return "estacion"+Integer.toString(numRMI);
	}
	//Registramos cada rmi station en el servicio de registro con su nombre RMIStationx 
	// y crear su fichero si no está creado manualmente
	public RMIStation(String dirRegistroRMI, int puertoRMI, int numRMI) throws RemoteException{
		super();
	
		this.dirRegistroRMI = dirRegistroRMI;
		this.puertoRMI = puertoRMI;
		this.numRMI = numRMI;
	}
	//Obtener direccion RMI, puerto RMI
	public static void main(String args[]){
		try{
			String dirRegistroRMI = args[0];
			int puertoRMI = 1099;
			int numRMI = 0;
			if(args.length > 1){
				puertoRMI = Integer.parseInt(args[1]);
				numRMI = Integer.parseInt(args[2]);
			}
			RMIStation rmi = new RMIStation(dirRegistroRMI,puertoRMI, numRMI);
			File fichero = new File("estacion"+numRMI+".txt");
			if(fichero.exists()){
				rmi.leerFichero();
			}
			else{
				rmi.crearFichero();	
			}
			
			try{
				
				if (System.getSecurityManager() == null) {
	            	System.setSecurityManager(new SecurityManager());
	        	}
	        	
				Registry registroRMI = LocateRegistry.getRegistry(dirRegistroRMI, puertoRMI);
				String[] nombresObjetosRemotos = registroRMI.list();

				for(String nombreObjetoRemoto : nombresObjetosRemotos){
					String dir = "rmi://" + dirRegistroRMI+":"+puertoRMI+"/"+nombreObjetoRemoto;
					Object obj = registroRMI.lookup(nombreObjetoRemoto);
					if(obj instanceof ServiciosRegistro){
						
						ServiciosRegistro servidor = (ServiciosRegistro) obj;
						servidor.registrar(rmi);
					}
				}
			}
			catch(Exception e){
				System.out.println("Error create station: " + e.toString());
			}

		}
		catch(Exception e){
			System.out.println("Args: direccionRMI puertoRMI numRMI");
		}
	}

	public int getTemperatura(){
		leerFichero();
		return temperatura;
	}
	public int getHumedad(){
		leerFichero();
		return humedad;
	};
	public int getLuminosidad() {
		leerFichero();
		return luminosidad;
	};
	public String getPantalla() {
		if(modificado == true){
			leerFichero();
		}
		return pantalla;

	};

	//Modificar temperatura, humedad, luminosidad...etc
	public void setTemperatura(int temperatura) {
		escribirFichero(temperatura,humedad,luminosidad,pantalla);
	}
	public void setHumedad(int humedad) {
		escribirFichero(temperatura,humedad,luminosidad,pantalla);
	};
	public void setLuminosidad(int luminosidad){
		escribirFichero(temperatura,humedad,luminosidad,pantalla);
	};
	public void setPantalla(String pantalla) {
		escribirFichero(temperatura,humedad,luminosidad,pantalla);
	};

	private void escribirFichero(int temperatura, int humedad,int luminosidad,String pantalla){
		String contenido = "Temperatura="+Integer.toString(temperatura)+
		"\nHumedad="+Integer.toString(humedad)+
		"\nLuminosidad="+Integer.toString(luminosidad)+"\nPantalla="+ pantalla;
		FileWriter fichero = null;
		PrintWriter pw = null;
		try{
			fichero = new FileWriter("estacion"+numRMI+".txt");
			pw = new PrintWriter(fichero);
			String[] lineas = contenido.split("\n");
			for(String linea: lineas){
				pw.println(linea);	
			}
		}
		catch(Exception e){
			System.out.println(e.toString());
		}finally{
			 try{                    
	            if( null != pw ){   
	               pw.close();     
	            }                  
	         }catch (Exception e2){ 
	            e2.printStackTrace();
	         }
		}

	}

	private void crearFichero(){

		String contenido = "Temperatura=30\nHumedad=90\nLuminosidad=450\nPantalla=Hola, esta es la practica de SD";
		
		FileWriter fichero = null;
		PrintWriter pw = null;
		try{
			fichero = new FileWriter("estacion"+numRMI+".txt");
			pw = new PrintWriter(fichero);
			String[] lineas = contenido.split("\n");
			for(String linea: lineas){
				pw.println(linea);	
			}
		}
		catch(Exception e){
			System.out.println(e.toString());
		}finally{
			 try{                    
	            if( null != pw ){   
	               pw.close();     
	            }              
	         }catch (Exception e2){ 
	            e2.printStackTrace();
	         }
		}
	}

	private void leerFichero(){
		File fichero = null;
		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<String> resultado = new ArrayList<String>();
		try{
			fichero = new File("estacion"+numRMI+".txt");
			fr = new FileReader(fichero);
			br = new BufferedReader(fr);
			String linea;
			while((linea=br.readLine())!=null)
				resultado.add(linea.split("=")[1]);
			temperatura = Integer.parseInt(resultado.get(0));
			humedad = Integer.parseInt(resultado.get(1));
			luminosidad = Integer.parseInt(resultado.get(2));
			pantalla = resultado.get(3);
		}
		catch(Exception e){
			System.out.println(e.toString());
		}finally{
			 try{                    
	            if( null != fr ){   
	               fr.close();     
	            }
	            if( null != br ){   
	               br.close();     
	            }                    
	         }catch (Exception e2){ 
	            e2.printStackTrace();
	         }
		}

		modificado = false;
	}
	
}
