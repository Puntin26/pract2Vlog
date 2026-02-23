package servicios;

import edu.pucmm.icc352.Articulo;
import edu.pucmm.icc352.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class GestionDb {
    private static GestionDb instancia;
    private EntityManagerFactory emf;

    // 1. En el constructor nos conectamos a la base de datos
    private GestionDb() {
        emf = Persistence.createEntityManagerFactory("MiBlogPU");
        crearUsuarioAdminPorDefecto();
    }

    public static GestionDb getInstancia() {
        if (instancia == null) {
            instancia = new GestionDb();
        }
        return instancia;
    }

    // Herramienta para hacer operaciones en la base de datos
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // 2. Método para crear el admin si la base de datos está vacía
    private void crearUsuarioAdminPorDefecto() {
        EntityManager em = getEntityManager();
        em.getTransaction().begin(); // Empezar transacción

        // Buscamos a ver si ya existe el usuario "admin"
        Usuario admin = em.find(Usuario.class, "admin");
        if (admin == null) {
            admin = new Usuario("admin", "Administrador", "admin", true, true, null);
            em.persist(admin); // Guardar en la base de datos
        }

        em.getTransaction().commit(); // Confirmar cambios
        em.close(); // Cerrar conexión
    }

    // 3. Buscar todos los artículos en la Base de Datos
    public List<Articulo> getListaArticulos() {
        EntityManager em = getEntityManager();
        // Hacemos una consulta (HQL) para traerlos ordenados del más nuevo al más viejo
        List<Articulo> lista = em.createQuery("SELECT a FROM Articulo a ORDER BY a.id DESC", Articulo.class).getResultList();
        em.close();
        return lista;
    }

    // 4. Autenticar usando la Base de Datos
    public Usuario autenticarUsuario(String username, String password) {
        EntityManager em = getEntityManager();
        Usuario u = em.find(Usuario.class, username); // Buscar por ID (username)
        em.close();

        if (u != null && u.getPassword().equals(password)) {
            return u;
        }
        return null; // Si no existe o la contraseña está mal
    }

    // 5. Guardar un artículo nuevo
    public void crearArticulo(Articulo articulo) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(articulo); // Guardar en tabla
        em.getTransaction().commit();
        em.close();
    }

    // 6. Buscar un artículo específico por su ID
    public Articulo getArticuloPorId(long id) {
        EntityManager em = getEntityManager();
        Articulo articulo = em.find(Articulo.class, id);
        em.close();
        return articulo;
    }

    // 7. Borrar artículo de la Base de Datos
    public void borrarArticulo(long id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        Articulo articulo = em.find(Articulo.class, id);
        if (articulo != null) {
            em.remove(articulo); // Eliminar de la tabla
        }

        em.getTransaction().commit();
        em.close();
    }
    // --- NUEVOS MÉTODOS PARA LA PAGINACIÓN (REQUISITO #3) ---

    // 1. Traer solo 5 artículos dependiendo de la página en la que estemos
    public List<Articulo> getArticulosPaginados(int pagina, int cantidadPorPagina) {
        EntityManager em = getEntityManager();

        // Matemática simple: Si es la página 1, empieza en 0. Si es la 2, empieza en 5, etc.
        int puntoDeInicio = (pagina - 1) * cantidadPorPagina;

        List<Articulo> lista = em.createQuery("SELECT a FROM Articulo a ORDER BY a.id DESC", Articulo.class)
                .setFirstResult(puntoDeInicio) // Desde dónde empieza a cortar
                .setMaxResults(cantidadPorPagina) // Cuántos artículos trae como máximo (5)
                .getResultList();

        em.close();
        return lista;
    }

    // 2. Contar cuántos artículos hay en TOTAL en la base de datos para saber cuántos botones dibujar
    public long getTotalArticulos() {
        EntityManager em = getEntityManager();
        long total = em.createQuery("SELECT COUNT(a) FROM Articulo a", Long.class).getSingleResult();
        em.close();
        return total;
    }
    // --- REQUISITO #4: FILTRAR POR ETIQUETA ---
    public List<Articulo> getArticulosPorEtiqueta(String nombreEtiqueta) {
        EntityManager em = getEntityManager();

        // Hacemos un JOIN para buscar dentro de la lista de etiquetas de los artículos
        List<Articulo> lista = em.createQuery(
                        "SELECT a FROM Articulo a JOIN a.listaEtiquetas e WHERE e.etiqueta = :nombre ORDER BY a.id DESC", Articulo.class)
                .setParameter("nombre", nombreEtiqueta)
                .getResultList();

        em.close();
        return lista;
    }
    // 8. Buscar un usuario por su username (Para el Requisito #5 de las Cookies)
    public Usuario getUsuarioPorUsername(String username) {
        EntityManager em = getEntityManager();
        Usuario u = em.find(Usuario.class, username); // Busca por la llave primaria
        em.close();
        return u;
    }
    // 9. Actualizar un usuario existente (Para guardar su nueva foto de perfil)
    public void actualizarUsuario(Usuario usuario) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        // merge() sirve para actualizar un registro que ya existe en la base de datos
        em.merge(usuario);

        em.getTransaction().commit();
        em.close();
    }
}