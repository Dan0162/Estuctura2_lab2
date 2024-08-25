package com.danield;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


class Reg implements Comparable<Reg> {
    int ID;
    String data;

    public Reg(int ID, String data) {
        this.ID = ID;
        this.data = data;
    }

    @Override
    public int compareTo(Reg other) {
        return Integer.compare(this.ID, other.ID);
    }

    @Override
    public String toString() {
        return ID + ":" + data;
    }

    public static Reg fromString(String s) {
        String[] parts = s.split(":");
        int id = Integer.parseInt(parts[0]);
        String data = parts[1];
        return new Reg(id, data);
    }
}

class BPlusTreeNode {
    boolean isLeaf; 
    List<Reg> keys; 
    List<BPlusTreeNode> children; 
    BPlusTreeNode next; 

    public BPlusTreeNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.next = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isLeaf ? "Leaf" : "Internal").append(" {");
        sb.append("keys=").append(keys);
        sb.append(", childrenCount=").append(children.size());
        if (isLeaf && next != null) {
            sb.append(", next=").append(next.keys);
        }
        sb.append('}');
        return sb.toString();
    }
}

class BPlusTree {
    private BPlusTreeNode root;
    Reg foundata;
    boolean found = false;
  
    private final int order; 

    public BPlusTree(int order) {

        
        if (order < 3) {
            throw new IllegalArgumentException("Order must be at least 3");
        }
        this.root = new BPlusTreeNode(true);
        this.order = order;
    }

    /**
     * Resetea los valores de foundata y found
     */
    public void resetFound(){
        foundata = null;
        found = false;
    }
    /**
     * Recorre todos los nodos hasta encontrar el nodo donde debería estar ubicado el registro con el ID 'order'
     * @param key
     * @return BplusTreeNode
     */
    private BPlusTreeNode findLeaf(Reg key) {
        BPlusTreeNode node = root;
        while (!node.isLeaf) {
            int i = 0;
            while (i < node.keys.size() && key.ID >= node.keys.get(i).ID) {
                i++;
            }
            node = node.children.get(i);
        }
        return node;

        
    }
    /**
     * Empleando el algoritmo 'findleaf', localiza el nodo apropiado para insertar el nuevo registro. Verifica si la capacidad del nodo se ha excedido y, en caso afirmativo, ejecuta una operación de división (split).
     * @param key
     **/
    public void insert(Reg key) {
        BPlusTreeNode leaf = findLeaf(key);
        insertIntoLeaf(leaf, key);

        if (leaf.keys.size() > order - 1) {
            splitLeaf(leaf);
        }
    }
    /**
     *En caso de que el algoritmo 'insert' determine que no es necesaria una operación de división, el registro será insertado en la posición correspondiente dentro del nodo hoja 
    * @param leaf
    * @param key
     **/
    private void insertIntoLeaf(BPlusTreeNode leaf, Reg key) {
        int pos = Collections.binarySearch(leaf.keys, key);
        if (pos < 0) {
            pos = -(pos + 1);
        }
        leaf.keys.add(pos, key);
    }
    /**
     * Genera dos nodos hijos, cada uno de los cuales contiene aproximadamente la mitad de los registros del nodo original. El registro que ocupa la posición central se promoverá al nodo padre, actuando como separador entre los dos nuevos nodos
     * @param leaf
     **/
    private void splitLeaf(BPlusTreeNode leaf) {
        int mid = (order + 1) / 2;
        BPlusTreeNode newLeaf = new BPlusTreeNode(true);

        newLeaf.keys.addAll(leaf.keys.subList(mid, leaf.keys.size()));
        leaf.keys.subList(mid, leaf.keys.size()).clear();

        newLeaf.next = leaf.next;
        leaf.next = newLeaf;

        if (leaf == root) {
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.keys.add(newLeaf.keys.get(0));
            newRoot.children.add(leaf);
            newRoot.children.add(newLeaf);
            root = newRoot;
        } else {
            insertIntoParent(leaf, newLeaf, newLeaf.keys.get(0));
        }
    }

    /**
     * Traslada un registro de un nodo hijo a su nodo padre. Para ello, se localiza primero el nodo padre y posteriormente se realiza el movimiento del registro.
     * @param left
     * @param right
     * @param key
     */
    private void insertIntoParent(BPlusTreeNode left, BPlusTreeNode right, Reg key) {
        BPlusTreeNode parent = findParent(root, left);

        if (parent == null) {
            throw new RuntimeException("Parent node not found for insertion");
        }

        int pos = Collections.binarySearch(parent.keys, key);
        if (pos < 0) {
            pos = -(pos + 1);
        }

        parent.keys.add(pos, key);
        parent.children.add(pos + 1, right);

        if (parent.keys.size() > order - 1) {
            splitInternal(parent);
        }
    }

    /**
     * Realiza una división de un nodo interno en dos nuevos nodos. El registro a insertar se coloca en la posición central de uno de los nuevos nodos y posteriormente se promueve al nodo padre.
     * @param internal
     */
    private void splitInternal(BPlusTreeNode internal) {
        int mid = (order - 1) / 2; 
        BPlusTreeNode newInternal = new BPlusTreeNode(false);
    
        newInternal.keys.addAll(internal.keys.subList(mid + 1, internal.keys.size()));
        internal.keys.subList(mid + 1, internal.keys.size()).clear();
    
        newInternal.children.addAll(internal.children.subList(mid + 1, internal.children.size()));
        internal.children.subList(mid + 1, internal.children.size()).clear();
    
        if (internal == root) {
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.keys.add(internal.keys.remove(mid));
            newRoot.children.add(internal);
            newRoot.children.add(newInternal);
            root = newRoot;
        } else {
            insertIntoParent(internal, newInternal, internal.keys.remove(mid));
        }
    }

    /**
     * Realiza un recorrido del árbol con el objetivo de hallar un nodo padre cuyos hijos coincidan exactamente con los valores especificados en los parámetros de entrada
     * @param current
     * @param target
     * @return BPlusTreeNode
     */
    private BPlusTreeNode findParent(BPlusTreeNode current, BPlusTreeNode target) {
        if (current.isLeaf || current.children.isEmpty()) {
            return null;
        }

        for (int i = 0; i < current.children.size(); i++) {
            BPlusTreeNode child = current.children.get(i);

            if (child == target) {
                return current; 
            }

            BPlusTreeNode possibleParent = findParent(child, target);
            if (possibleParent != null) {
                return possibleParent;
            }
        }

        return null; 
    }

    /**
     * Realiza una búsqueda para localizar la hoja potencial donde podría residir el registro buscado. Retorna un valor booleano que indica si la búsqueda fue exitosa o no
     * @param key
     * @return Boolean
     */
    public boolean search(Reg key) {
        BPlusTreeNode leaf = findLeaf(key);
        int pos = Collections.binarySearch(leaf.keys, key);
        if (pos >= 0) {
            found = true;
            foundata = leaf.keys.get(pos);
        }
        return pos >= 0;
    }    

    /**
     * Verifica si el nodo a evaluar es la raíz del árbol. En caso contrario, localiza su nodo padre y analiza si es necesario realizar una redistribución de registros entre los hijos del nodo padre o si es viable una fusión entre dos nodos hermanos para mantener las propiedades de la estructura de datos.
     * @param node
     */
    private void balanceAfterDeletion(BPlusTreeNode node) {
        if (node == root) {
            if (node.keys.isEmpty() && !node.isLeaf) {
                root = node.children.get(0);
            }
            return;
        }

        BPlusTreeNode parent = findParent(root, node);
        int index = parent.children.indexOf(node);
        BPlusTreeNode leftSibling = (index > 0) ? parent.children.get(index - 1) : null;
        BPlusTreeNode rightSibling = (index < parent.children.size() - 1) ? parent.children.get(index + 1) : null;

        if (leftSibling != null && leftSibling.keys.size() > (order + 1) / 2) {
            borrowFromLeftSibling(node, leftSibling, parent, index - 1);
        } else if (rightSibling != null && rightSibling.keys.size() > (order + 1) / 2) {
            borrowFromRightSibling(node, rightSibling, parent, index);
        } else {
            if (leftSibling != null) {
                mergeNodes(leftSibling, node, parent, index - 1);
            } else if (rightSibling != null) {
                mergeNodes(node, rightSibling, parent, index);
            }
        }
    }

    /**
     * Si el nodo a eliminar es una hoja, se obtiene el registro de menor valor del nodo hermano. Posteriormente, se elimina el registro de mayor valor del nodo original y se actualiza la posición de este registro de menor valor en el nodo padre.
     * @param node
     * @param leftSibling
     * @param parent
     * @param parentIndex
     */
    private void borrowFromLeftSibling(BPlusTreeNode node, BPlusTreeNode leftSibling, BPlusTreeNode parent, int parentIndex) {
        node.keys.add(0, leftSibling.keys.remove(leftSibling.keys.size() - 1));
        if (!leftSibling.isLeaf) {
            node.children.add(0, leftSibling.children.remove(leftSibling.children.size() - 1));
        }
        parent.keys.set(parentIndex, node.keys.get(0));
    }

    /**
     * Si el nodo a evaluar es una hoja, se inserta el nuevo registro en la primera posición (posición 0) del nodo hermano derecho. Posteriormente, se intercambia el registro recién insertado con un registro del nodo padre, ajustando así la estructura del árbol.
     * @param node
     * @param rightSibling
     * @param parent
     * @param parentIndex
     */
    private void borrowFromRightSibling(BPlusTreeNode node, BPlusTreeNode rightSibling, BPlusTreeNode parent, int parentIndex) {
        node.keys.add(rightSibling.keys.remove(0));
        if (!rightSibling.isLeaf) {
            node.children.add(rightSibling.children.remove(0));
        }
        parent.keys.set(parentIndex, rightSibling.keys.get(0));
    }

    /**
     * Recorre recursivamente los subárboles izquierdo y derecho del nodo, recolectando todos los nodos descendientes. Posteriormente, agrupa todos los nodos obtenidos en un único nodo.
     * @param left
     * @param right
     * @param parent
     * @param parentIndex
     */
    private void mergeNodes(BPlusTreeNode left, BPlusTreeNode right, BPlusTreeNode parent, int parentIndex) {
        left.keys.addAll(right.keys);
        if (!right.isLeaf) {
            left.children.addAll(right.children);
        }
        left.next = right.next;

        parent.keys.remove(parentIndex);
        parent.children.remove(right);

        if (parent.keys.size() < (order + 1) / 2) {
            balanceAfterDeletion(parent);
        }
    }
    
    /**
     * Utiliza las clases BufferedWriter y FileWriter para generar un archivo de salida. A través de un proceso recursivo, serializa cada nodo del árbol, invocando el método serializeNode para convertir cada nodo en una representación serializable. El resultado final es una representación del árbol completo almacenada en el archivo especificado
     * @param filename
     * @throws IOException
     */
    public void serializeToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            serializeNode(writer, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Serializa un nodo del árbol, almacenando su estructura y contenido en el archivo de salida
     * @param writer
     * @param node
     * @throws IOException
     */
    private void serializeNode(BufferedWriter writer, BPlusTreeNode node) throws IOException {
        writer.write(node.isLeaf ? "Leaf" : "Internal");
        writer.write(" { keys=");
        for (Reg key : node.keys) {
            writer.write(key.toString() + ",");
        }
        writer.write(" childrenCount=" + node.children.size());
        writer.newLine();

        if (!node.isLeaf) {
            for (BPlusTreeNode child : node.children) {
                serializeNode(writer, child);
            }
        }
    }
    

    /**
     * Utiliza las clases BufferedReader y FileReader para leer el contenido de un archivo de entrada. A través de un proceso recursivo, deserializa cada nodo del árbol, invocando el método deserializeNode para convertir cada representación serializable en un nodo del árbol. El resultado final es un árbol completo almacenado en la estructura de datos del programa.
     * @param filename
     * @throws IOException
     */
    public void deserializeFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            root = deserializeNode(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserializa un nodo del árbol, almacenando su estructura y contenido en la estructura de datos del programa
     * @param reader
     * @return BPlusTreeNode
     * @throws IOException
     */
    private BPlusTreeNode deserializeNode(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }

        boolean isLeaf = line.startsWith("Leaf");
        BPlusTreeNode node = new BPlusTreeNode(isLeaf);

        String[] parts = line.split("\\{ keys=|, childrenCount=");
        String[] keyParts = parts[1].split(",");
        for (String keyPart : keyParts) {
            if (!keyPart.isEmpty()) {
                node.keys.add(Reg.fromString(keyPart));
            }
        }

        int childrenCount = Integer.parseInt(parts[2].trim());
        if (!isLeaf) {
            for (int i = 0; i < childrenCount; i++) {
                BPlusTreeNode child = deserializeNode(reader);
                node.children.add(child);
            }
        }

        return node;
    }
}