import java.rmi.*;
import java.*;

public interface InterfazRMI extends java.rmi.Remote{
	public int getTemperatura()  throws RemoteException;
	public int getHumedad()  throws RemoteException;
	public int getLuminosidad()  throws RemoteException;
	public String getPantalla()  throws RemoteException;
	public void setTemperatura(int temperatura)  throws RemoteException;
	public void setHumedad(int humedad)  throws RemoteException;
	public void setLuminosidad(int luminosidad)  throws RemoteException;
	public void setPantalla(String pantalla)  throws RemoteException;
	public String getNombre() throws RemoteException;
}