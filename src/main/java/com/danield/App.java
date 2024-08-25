package com.danield;import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileWriter;



public class App 
{
    public static void main( String[] args ) 
    {
        try{
            //Se inicializa un BufferedReader para leer la entrada del usuario
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            
            //Se solicita al usuario el grado del árbol
            System.out.println("Por favor ingresar el grado del árbol");
            int degree = Integer.parseInt(reader.readLine()); 

            //Se crea un nuevo árbol B+ con el grado ingresado
            BPlusTree newTree = new BPlusTree(degree);
            
            //Se crea una expresión regular para buscar los datos en el archivo
            String patternString = "Insert:\\{id:(\\d+),nombre:([^}]+)\\}";
            String line;            
            Pattern pattern = Pattern.compile(patternString);

            //Se inicializan variables tipo long para medir el tiempo de ejecución de las operaciones
            long starttime;
            long endtime;

            //Se pregunta al usuario si desea leer un árbol existente
            System.out.println("Desea leer un árbol existente? (y/n)");

            if (reader.readLine().equals("y")){
                //Si el usuario decide que quiere leer un árbol existente, se le solicita el filepath del archivo
                System.out.println("Por favor ingresar el filepath del archivo en cuestión");
                String filepath = reader.readLine();
                File file = new File(filepath);

                //Se lee el archivo y se carga el árbol utilizando el método deserializeFromFile de la clase BPlusTree
                starttime = System.currentTimeMillis();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                newTree.deserializeFromFile(filepath);
                bufferedReader.close();
                endtime = System.currentTimeMillis();
                System.out.println("El árbol se ha cargado en "+(endtime-starttime)+"ms");

                //Se pregunta al usuario si desea buscar un dato dentro del árbol B+ cargado.
                System.out.println("¿Desea buscar un dato? (y/n)");
                
                    if (reader.readLine().equals("y")){
                        do{
                            //Si el usuario decide buscar, se le solicita el id del dato a buscar
                            System.out.println("Por favor ingresar el id a buscar");
                            int id = Integer.parseInt(reader.readLine());

                            //Se verifica si el dato se encuentra en el árbol utilizando el método search de la clase BPlusTree                            
                            boolean result = newTree.search(new Reg (id, ""));

                            if (result == true){
                                //Si lo encuentra, muestra el resultado al usuario
                                System.out.println("El resultado de la búsqueda es: "+newTree.foundata.toString());
                                newTree.resetFound();
                            }
                            else{
                                //Si no lo encuentra, muestra al usuario que no se encontró el id
                                System.out.println("No se encontró el id");
                            }

                            //Se pregunta al usuario si desea buscar otro dato
                            System.out.println("¿Desea buscar otro dato? (y/n)");
                        }while(reader.readLine().equals("y"));
                        
                    }
                

            }
            else{
                //Si el usuario decide que no quiere leer un árbol existente, se le solicita el filepath del archivo
                System.out.println("Se leerá un arhivo nuevo. Por favor ingresar el filepath del archivo en cuestión");
                String filepath = reader.readLine();

                //Se lee el archivo y se insertan los datos en el árbol B+ utilizando el método insert de la clase BPlusTree, tomando el tiempo de ejecución                
                starttime = System.currentTimeMillis();
                File file = new File(filepath);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                
                while ((line = bufferedReader.readLine()) != null) {
                    // Se revisa si la línea coincide con el patrón esperado
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find() && matcher.group(1) != null) {
                        try {
                            int id = Integer.parseInt(matcher.group(1));
                            String nombre = matcher.group(2);
                            
                            if (nombre == null || nombre.trim().isEmpty()) {
                                throw new IOException("Formato de línea inválido: El nombre no puede estar vacío."); 
                            }
            
                            // Se inserta el registro en el árbol
                            newTree.insert(new Reg(id, nombre));
                        } catch (NumberFormatException e) {
                            bufferedReader.close();
                            throw new IOException("Formato de línea inválido: ID no es un número entero válido en la línea: " + line);
                        }
                    } else {
                        // Si la línea no coincide con el patrón esperado, se lanza una excepción
                        bufferedReader.close();
                        throw new IOException("Formato de línea inválido encontrado: " + line);
                    }
                }                

                bufferedReader.close();
                endtime = System.currentTimeMillis();

                long insertime = endtime-starttime;

                System.out.println("El árbol se ha creado en "+insertime+"ms");

                //Se exporta el árbol a un archivo de texto utilizando el método serializeToFile de la clase BPlusTree, tomando el timepo de ejecución.
                System.out.println("El árbol se exportará a" + file.getParent()+"/"+file.getName().substring(0, file.getName().length()-4)+"IDX_.txt");
                starttime = System.currentTimeMillis();
                try{
                    newTree.serializeToFile(file.getParent()+"/"+file.getName().substring(0, file.getName().length()-4)+"IDX_.txt");
                    endtime = System.currentTimeMillis();
    
                    long serializetime = endtime-starttime;
                    
                    System.out.println("El árbol se ha exportado en "+serializetime+"ms");
    
                    //Se crea un archivo de texto con el tiempo de ejecución de las operaciones.
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file.getParent()+"/"+"timelog.txt"));
    
                    writer.write("Tiempo de creación del árbol: "+insertime+"ms\n\n" + "Tiempo de exportación del árbol: "+serializetime+"ms\n");
                    writer.close();
                }
                catch(Exception e){                    
                    System.out.println("Error: "+e.getMessage());
                }
                
            }
        }catch(Exception e){
            System.out.println("Error: "+e.getMessage());
        }
    }
}
