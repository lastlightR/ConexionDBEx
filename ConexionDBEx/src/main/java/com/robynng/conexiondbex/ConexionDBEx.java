/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.robynng.conexiondbex;

import java.sql.Connection;
import java.sql.Date;
//import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 *
 * @author Robyn
 */
public class ConexionDBEx {
    
    //establecemos las constantes para la conexión
    static final String DB_URL = "jdbc:mysql://localhost:3306/jcvd";
    static final String USER = "robynng";
    static final String PASS = "1234";
    static final String QUERY = "SELECT * FROM videojuegos;";
    static PoolDataSource pds;

    public static void main(String[] args) {
        try {
            
            //inicializamos el pool de conexiones
            pds = PoolDataSourceFactory.getPoolDataSource();
            //asignamos los datos del pool de conexiones
            pds.setConnectionFactoryClassName ("com.mysql.cj.jdbc.Driver"); 
            pds.setURL (DB_URL); 
            pds.setUser (USER); 
            pds.setPassword (PASS);
            
            pds.setInitialPoolSize(5); //asignamos el tamaño del pool de conexiones
            
            //creamos la instancia Connection e inicializamos con PDS
            Connection connection = pds.getConnection();
            
            System.out.println("Conexión establecida.");
            
            //este statement realiza un simple SELECT *
            Statement statement = connection.createStatement();
            statement.execute(QUERY);
            
            //llamadas a métodos
            System.out.println("¿Existe el juego Valorant? " + buscaNombre("Valorant"));
            lanzaConsulta("SELECT * FROM videojuegos WHERE Compañia = 'Riot';");
            nuevoRegistro("Hades", "Roguelite", "2018-12-06", "Supergiant", 25);
            nuevoRegistro();
            boolean b = eliminarRegistro("God of War: Ragnarök");
            if (b) //mostraremos que se ha eliminado si es true
                System.out.println("Se ha eliminado el registro.");
            else System.out.println("No se ha eliminado el registro.");
            connection.close();
            connection = null;
            
            System.out.println("Conexión devuelta al pool de conexiones.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public static boolean buscaNombre(String nombre) { //método que busca un nombre en la tabla por parámetro y devuelve true si existe
        String query = "SELECT * FROM videojuegos WHERE Nombre = ?;"; //? será sustituido por el nombre que le asignemos
        int result = 0;
        Connection connection = null;
        try 
            {
            connection = pds.getConnection(); //establecemos la conexión
            PreparedStatement ps = connection.prepareStatement(query); //asignamos al PreparedStatement la consulta
            ps.setString(1, nombre); //la primera interrogación será el nombre por parámetro
            ResultSet rs = ps.executeQuery(); //ejecutamos la consulta con un ResultSet
            while (rs.next()){
                result++; //incrementamos al encontrar una coincidencia
            }
            if (connection != null)
                connection.close(); //cerramos la conexión
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result > 0; //devolverá true si es mayor y false si es 0
    }
    
    public static void lanzaConsulta(String query) { //lanza una consulta por parámetro
        //variables que se usan en la consulta
        int id;
        String nombre = "", genero = "", compania = "";
        float precio = 0;
        Date fecha;
        Connection connection = null;
        try 
            {
            connection = pds.getConnection(); //establecemos la conexión
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query); //ejecutamos la consulta del parámetro a través de un ResultSet
            System.out.println("ID\tNombre\t\t\t\tGénero\t\tFechaLanzamiento\tCompañía\tPrecio"); //columna header
            String tabulado = ""; //solución chapucera para la tabla, hay mejores formas de mostrar esto
            while (rs.next()){ //por cada fila encontrada obtenemos sus valores
                id = rs.getInt("Id");
                nombre = rs.getString("Nombre");
                if (nombre.length() < 8) //tabulados
                    tabulado = "\t\t\t\t";
                else if (nombre.length() < 16)
                    tabulado = "\t\t\t";
                else if (nombre.length() < 24)
                    tabulado = "\t\t";
                else    
                    tabulado = "\t";
                genero = rs.getString("Genero");
                fecha = rs.getDate("FechaLanzamiento");
                compania = rs.getString("Compañia");
                precio = rs.getFloat("Precio");
                System.out.println("--\t------\t\t\t\t------\t\t----------------\t--------\t-------"); //separación
                System.out.println(id+"\t"+nombre+tabulado+genero+"\t\t"+fecha+"\t\t"+compania+"\t\t"+precio); //fila con datos
            }
            if (connection != null)
                connection.close(); //cerramos conexión
        } catch (SQLException ex) {
            ex.printStackTrace();
        } 
    }
    
    //método que introduce un nuevo registro por parámetros
    public static void nuevoRegistro(String nombre, String genero, String fecha, String compania, float precio){
        String query = "INSERT INTO videojuegos"
                + " VALUES (null, ?, ?, ?, ?, ?);"; //null será el ID autoincrementado, el resto serán los valores de las filas
        Connection connection = null;
        try {
                connection = pds.getConnection(); //establecemos conexión
                PreparedStatement ps = connection.prepareStatement(query); //asignamos la consulta a un PreparedStatement
                
                //asignamos los parámetros a cada ? del query
                ps.setString(1, nombre);
                ps.setString(2, genero);
                ps.setString(3, fecha);
                ps.setString(4, compania);
                ps.setFloat(5, precio);
                //iniciamos el INSERT INTO
                ps.executeUpdate();
                
                if (connection != null)
                    connection.close(); //cerramos conexión
                System.out.println("Registro añadido con éxito.");
        } catch (SQLException ex){
            ex.printStackTrace();
        } catch (InputMismatchException ex){
            ex.printStackTrace();
        }
    }
    
    public static void nuevoRegistro(){ //método que introduce un nuevo registro por pantalla
        String query = "INSERT INTO videojuegos VALUES (null, ?, ?, ?, ?, ?);"; //null será el ID autoincrementado, el resto serán los valores de las filas
        Connection connection = null;
        
        Scanner scanner = new Scanner(System.in); //scanner para que el usuario introduzca por teclado
        System.out.println("-- Introduciendo nuevo registro --");
        try {
                connection = pds.getConnection(); //establecemos conexión
                PreparedStatement ps = connection.prepareStatement(query); //asignamos la consulta a un PreparedStatement
                //pedimos por teclado los valores y los sustituimos por cada ?        
                System.out.println("Nombre: ");
                ps.setString(1, scanner.nextLine());
                System.out.println("Género: ");
                ps.setString(2, scanner.nextLine());
                System.out.println("Fecha (Formato YYYY-MM-DD): ");
                ps.setString(3, scanner.nextLine());
                System.out.println("Compañía: ");
                ps.setString(4, scanner.nextLine());
                System.out.println("Precio: ");
                ps.setFloat(5, scanner.nextFloat());
                //ejecutamos el INSERT INTO
                ps.executeUpdate();
                
                if (connection != null)
                    connection.close(); //cerramos conexión
                
                System.out.println("Registro añadido con éxito.");
        } catch (SQLException ex){
            ex.printStackTrace();
        } catch (InputMismatchException ex){
            ex.printStackTrace();
        }
    }
    
    public static boolean eliminarRegistro(String nombre) { //método que elimina un registro por parámetro
        String query = "DELETE FROM videojuegos WHERE Nombre = ?;"; //? será sustituido por el nombre que le asignemos
        Connection connection = null;
        
        int result = 0;
        try {
                connection = pds.getConnection(); //establecemos conexión
                PreparedStatement ps = connection.prepareStatement(query); //asignamos el delete a un PreparedStatement
                
                ps.setString(1, nombre); //asignamos el parámetro al ?
                result = ps.executeUpdate(); //ejecutamos el DELETE, e incrementamos el valor de result si se realiza
                
                if (connection != null)
                    connection.close(); //cerramos conexión
                //System.out.println("Registro eliminado con éxito");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result > 0; //será true si se ha eliminado el registro
    }
}
