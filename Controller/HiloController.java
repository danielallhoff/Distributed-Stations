import java.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class HiloController extends Thread{
    
    private String controlador = "controladorSD";
    private String versionHTTP = "HTTP1.1";
    private int puertoRMI;
    private String dirRMI;
    private Socket skCliente;
    private Registry registroRMI;
    public HiloController(Socket skCliente, String dirRMI,  int puertoRMI){
        this.puertoRMI = puertoRMI;
        this.skCliente = skCliente;
        this.dirRMI = dirRMI;

    }
    //Contenido HTML que devuelve en cualquier respuesta al HTTP
    private String contenidoHTMLEstatico(String contenido){
        String arriba = "<html> <head> <style> body{padding-top:100; font-family: Arial, Helvetica, sans-serif; background-color: #e6e6ff; } a{text-align:center;width: 60%; max-width: 500px; margin: 9 auto;} .centrado{background-color: lightblue; width: 60%; max-width: 500px; margin: 9 auto; border: 4px inset black; padding: 10; } .error{color:red; text-transform:uppercase; } h2{text-align:center; } ul{list-style: square outside none; } .estacion{border: 4px inset black; padding:5px; margin-top:10px; background-color: #f5f5f0 } table,td{padding-left: 5; padding-right: 15; vertical-align: top; } .pantalla{height:100px; } </style> </head> <body> <div class=\"centrado\">";
        String dinamico = contenido;
        String links= "<div align=\"center\"><a href=\"/index.html\"> Inicio</a></div><div align=\"center\"><a href=\"/controladorSD\">Acceso a estaciones</a></div>"; 
        String abajo = links+"</div></body></html>";

        return arriba + dinamico + abajo;
    }   
    //Encodificar en html todas las estaciones
    private String visualizarEstaciones(ArrayList<InterfazRMI> estaciones){
        String contenido ="";
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        if(estaciones.isEmpty()){
            contenido="<h2 class=\"error\">No hay estaciones registradas</h2>";
        }else{
            contenido += "<h2>Estaciones</h2>";
            for(int i = 0; i < estaciones.size(); i++){
                InterfazRMI estacion = estaciones.get(i);
                try{
                    String nomEstacion = estacion.getNombre();
                    contenido += "<h3>"+ nomEstacion+"</h3>";
                    contenido += "<form name=\"" + nomEstacion + "\" action=\"controladorSD/set\">";
                    contenido += "<input type=\"hidden\" name=\"station\" value=\"" + nomEstacion + "\">";
                    contenido += "<ul> <table><tr><td><li>Temperatura </li></td> <td><input name=\"temperatura\" type=\"text\" value=\"" + estacion.getTemperatura() + "\"></td></tr>";   
                    
                    contenido += "<tr><td><li>Humedad </li></td> <td><input name=\"humedad\" type=\"text\" value=\"" + estacion.getHumedad() + "\"></td></tr>";   
                    contenido += "<tr><td><li>Luminosidad </li></td> <td><input name=\"luminosidad\" type=\"text\" value=\"" + estacion.getLuminosidad() + "\"></td></tr>";   
                    contenido += "<td><li>Pantalla </li></td> <td><textarea name=\"pantalla\" class=\"pantalla\">" + estacion.getPantalla() + "</textarea></td>";
                    contenido += "<td><button type=\"text\" value=\""+ nomEstacion + "\">Modificar</td>";
                                
                    contenido += "</table></ul></form>";
                }
                catch(Exception e){
                    contenido = "<h2 class=\"error\">" + e.toString() + "</h2>";
                }
                
            }
        }
        

        return contenidoHTMLEstatico(contenido);

    }
    //Modificar datos de una estación determinada
    private String modificarEstacion(String path) throws Exception{
        String [] paramSplitted = path.split("&");
        ArrayList<String> params = new ArrayList<>(Arrays.asList(paramSplitted));
        String [] datos  = params.get(0).split("=");
        System.out.println(datos[0] + " " + datos[1]);
        String nomEstacion ="estacionx";
        String res="";
        if(datos[0].equals("station")){
            nomEstacion = datos[1];
        }
        else{ throw new Exception();};
        Object obj = registroRMI.lookup(nomEstacion);
        InterfazRMI estacion = (InterfazRMI) obj;
        params.remove(0);
        for(String param: params){
            datos = param.split("=");
            switch(datos[0]){
                case "temperatura":estacion.setTemperatura(Integer.parseInt(datos[1]));break;             
                case "humedad":estacion.setHumedad(Integer.parseInt(datos[1]));break;
                case "luminosidad":estacion.setLuminosidad(Integer.parseInt(datos[1]));break;
                case "pantalla":estacion.setPantalla(datos[1]);break;
                default: res += "<h2 class=\"error\">Variable " + datos[0] + " no disponible</h2>";
            }
        }
        
        res = "<h2>La estacion introducida (" + nomEstacion + ") se ha modificado correctamente!"+ "</h2>";
        return res;     
    }
    //Obtener dato de una estación en concreto
    private String obtenerParametroEstacion(ArrayList<String> params) throws Exception{
        String res;
        boolean ok  = true;
        Object obj = registroRMI.lookup("estacion"+params.get(0));
        InterfazRMI estacion = (InterfazRMI) obj;
        switch(params.get(1)){
            case "temperatura":res = Integer.toString(estacion.getTemperatura());break;                
            case "humedad":res = Integer.toString(estacion.getHumedad());break;
            case "luminosidad":res = Integer.toString(estacion.getLuminosidad());break;
            case "pantalla":res = estacion.getPantalla();break;

            default: res = "<h2 class=\"error\">Operación " + params.get(1) + " no disponible</h2>";
                        ok = false;
        }
        if(ok == true){
            res = "<h2> La estacion introducida (estacion"+params.get(0)+ ") tiene "+ params.get(1)+"= " + res +".</h2>"; 
        }
        return res;
    }
    //Obtener todas las estaciones
    private String obtenerEstaciones(){
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        ArrayList<InterfazRMI> estaciones = new ArrayList<InterfazRMI>();
        String res = "";
        try{
            String[] nombresObjetosRemotos = registroRMI.list();
            for(String nombreObjetoRemoto : nombresObjetosRemotos){
                Object obj = registroRMI.lookup(nombreObjetoRemoto);
                if(obj instanceof InterfazRMI){
                    InterfazRMI objetoRMI = (InterfazRMI) obj;
                    estaciones.add(objetoRMI);
                }
            }
            res = visualizarEstaciones(estaciones);
        }
        catch(Exception e){
            res = "<h2 class=\"error\">No se ha podido obtener las estaciones</h2>";    
        }
        System.out.println(res);
        return res;
    }
    //Manejo de petición dinamica
    public String manejarPeticionDinamica(String path){
        String res = "";
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        //Acceso index
        try{
            this.registroRMI = LocateRegistry.getRegistry(dirRMI, puertoRMI);
            if(path.equals("index")){
                res = obtenerEstaciones();
                return res;
            }
            else{
                String [] paramSplitted = path.split(",");
                ArrayList<String> params = new ArrayList<>(Arrays.asList(paramSplitted));
                System.out.println(path);
                //Obtener parámetros
                if(params.get(0).equals("get")){
                    params.remove(0);
                    res = obtenerParametroEstacion(params); 
                }
                //Modificar
                else{
                    params.remove(0);
                    res = modificarEstacion(params.get(0));
                }
            }
        }catch(NotBoundException e1){
            res = "<h2 class=\"error\"> La estacion introducida no existe.</h2>"; 
        }catch(RemoteException e2){
            res = "<h2 class=\"error\">Error acceso a servidor de estaciones metereologicas: " + e2.toString() + " no disponible</h2>";
        }catch(Exception e3){
            res = "<h2 class=\"error\">Error url mal formada</h2>";    
        }            
        return contenidoHTMLEstatico(res);
    }

    //Lo que ejecuta cada hilo
    public void run(){				
        try{

            String peticion = lecturaSocket(skCliente);
            String resultado = manejarPeticionDinamica(peticion);
            //System.out.println(resultado);
            //Resultado HTTP;
            escrituraSocket(skCliente, resultado);
            skCliente.close();
        }
        catch(Exception e){
           System.out.println("Error: " + e.toString());
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
            e.printStackTrace();
        }
        return result;
        
    }
    
     public static void escrituraSocket(Socket sk, String mensaje){
        try
        {
            OutputStream outputStream = sk.getOutputStream();
            PrintWriter out =
                new PrintWriter(new OutputStreamWriter(outputStream), true);
            out.println(mensaje); 
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }    
    }

}
