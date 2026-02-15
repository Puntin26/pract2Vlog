package servicios;

import edu.pucmm.icc352.Articulo;
import edu.pucmm.icc352.Usuario;
import edu.pucmm.icc352.Comentario;

import java.util.ArrayList;
import java.util.List;

public class GestionDb {

    private static GestionDb instancia;
    private List<Usuario> listaUsuarios;
    private List<Articulo> listaArticulos;

    private GestionDb() {
        listaUsuarios = new ArrayList<>();
        listaArticulos = new ArrayList<>();

        listaUsuarios.add(new Usuario("admin", "Administrador", "admin", true, true));

        // Creamos un autor de prueba
        listaUsuarios.add(new Usuario("autor", "Juan Perez", "1234", false, true));
    }

    public static GestionDb getInstancia() {
        if (instancia == null) {
            instancia = new GestionDb();
        }
        return instancia;
    }

    public Usuario getUsuario(String username) {
        for (Usuario user : listaUsuarios) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public Usuario autenticarUsuario(String username, String password) {
        Usuario user = getUsuario(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public List<Articulo> getListaArticulos() {
        return listaArticulos;
    }

    public void crearArticulo(Articulo articulo) {
        if (articulo.getId() == 0) {
            articulo.setId(listaArticulos.size() + 1);
        }
        listaArticulos.add(articulo);
    }

    public Articulo getArticuloPorId(long id) {
        for (Articulo art : listaArticulos) {
            if (art.getId() == id) {
                return art;
            }
        }
        return null;
    }

    public void borrarArticulo(long id) {
        Articulo art = getArticuloPorId(id);
        if (art != null) {
            listaArticulos.remove(art);
        }
    }

    public void agregarComentario(long idArticulo, Comentario comentario) {
        Articulo art = getArticuloPorId(idArticulo);
        if (art != null) {
            art.getListaComentarios().add(comentario);
        }
    }
}