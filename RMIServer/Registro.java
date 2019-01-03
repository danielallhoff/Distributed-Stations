import java.rmi.*;
import java.*;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
//Ejecutar en máquina con servidor registro
public class Registro extends UnicastRemoteObject implements ServiciosRegistro, Serializable{

	int puertoRMI;
	public static void main(String args[]) throws Exception{
        try           
        {   
        	int puerto = 1099;
        	if(args.length >= 1){
        		puerto = Integer.parseInt(args[0]);
        	}
        	if (System.getSecurityManager() == null) {
            	System.setSecurityManager(new SecurityManager());
        	}

		   	String URLRegistro = "/Registro";
		   	Registro registroRMI = new Registro(puerto);
		   	Naming.rebind (URLRegistro, registroRMI);
		   
		   	System.out.println("Servidor de objeto registro listo.");
		   
        }            
        catch (Exception ex)            
        {                  
            System.out.println("Error registro: " + ex.toString());            
        }     
	}
	//
	public Registro(int puerto) throws RemoteException{
		super();
		puertoRMI = puerto;
	}	
	public void registrar(InterfazRMI rmistation) throws RemoteException{
		try{
			if (System.getSecurityManager() == null) {
            	System.setSecurityManager(new SecurityManager());
        	}
		   	String URLRegistro = "/"+rmistation.getNombre();
		   	Naming.rebind (URLRegistro, rmistation);
		   	System.out.println("Registrado :" + rmistation.getNombre());
		}
		catch(Exception e){
			System.out.println("Error registrar: " + e.toString());	
		}
		
	}

}