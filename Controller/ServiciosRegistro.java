import java.rmi.*;
import java.*;

public interface ServiciosRegistro extends java.rmi.Remote{
	public void registrar(InterfazRMI rmistation) throws RemoteException;
}