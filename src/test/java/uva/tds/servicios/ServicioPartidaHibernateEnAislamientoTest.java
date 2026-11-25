package uva.tds.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import uva.tds.entidades.Partida;
import uva.tds.entidades.ResumenPartida;
import uva.tds.interfaces.RepositorioPartida;


/**
 * Tests para la clase ServicioPartida.
 * 
 * @author Marta Pérez Alonso
 * @author Elisa Martínez Lafuente
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServicioPartidaHibernateEnAislamientoTest {

    private RepositorioPartida repositorioPartida;
    private ServicioPartida servicioPartida;
    private Partida partidaCompleta;

    private final String ID_PARTIDA = "p-test-01";
    private final LocalDate FECHA_PARTIDA = LocalDate.of(2025, 11, 25);
    private final String JUGADOR1 = "Ana";
    private final String JUGADOR2 = "Luis";

    private static final String CONFIG_FILE = "hibernate-test.cfg.xml";
    // private static final String CONFIG_FILE="hibernate.cfg.xml";

    @BeforeAll
    void setUpAll() {
        // Primero inicializamos Hibernate para que cree el esquema
        repositorioPartida = new RepositorioPartidaHibernate(CONFIG_FILE);
    }

    @BeforeEach
    void setUp() throws Exception {
        var sessionFactory = HibernateUtil.getSessionFactory(CONFIG_FILE);
        var session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            // Truncar tablas en el orden correcto para evitar violaciones de FK
            session.createNativeQuery("TRUNCATE TABLE jugada_captura").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE jugada_mesa_resultante").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE mano_cartas").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE partida_mesa").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE ronda_mesa").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE turnos").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE jugadas").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE manos").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE rondas").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE resumenes").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE partidas").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE cartas").executeUpdate();



            session.getTransaction().commit();
            session.close();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }

        servicioPartida = new ServicioPartida(repositorioPartida);
        partidaCompleta = new Partida(ID_PARTIDA, FECHA_PARTIDA, JUGADOR1, JUGADOR2);
        ResumenPartida resumenCompleto = new ResumenPartida(ID_PARTIDA, FECHA_PARTIDA, JUGADOR1, JUGADOR2);
        partidaCompleta.setResumenPartida(resumenCompleto);

    }

    @AfterAll
    void tearDown() {
        HibernateUtil.shutdown();
    }

    @Test
    void testObtenerPartidaValidoIdValido() {
        repositorioPartida.guardar(partidaCompleta);
        Partida resultado = servicioPartida.obtenerPartida(ID_PARTIDA);

        assertEquals(ID_PARTIDA, resultado.getId());
    }

    @Test
    void testObtenerPartidaNoValidoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> servicioPartida.obtenerPartida(null));
    }

    @Test
    void testObtenerPartidaNoValidoNoEncontrada() {
        assertThrows(IllegalStateException.class, () -> servicioPartida.obtenerPartida("id-inexistente"));
    }

    @Tag("Cobertura")
    @Test
    void testCargarNoValidoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.cargar(null));
    }

    @Tag("Cobertura")
    @Test
    void testCargarNoValidoIdVacio() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.cargar(""));
    }

    @Test
    void testGuardarPartidaValidoPartidaCompleta() {
        servicioPartida.guardarPartida(partidaCompleta);
        assertEquals(partidaCompleta, servicioPartida.obtenerPartida(partidaCompleta.getId()));
    }

    @Test
    void testGuardarPartidaNoValidoPartidaNula() {
        assertThrows(IllegalArgumentException.class, () -> servicioPartida.guardarPartida(null));
    }

    @Tag("Cobertura")
    @Test
    void testGuardarNoVAlidoNula(){
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.guardar(null));
    }

    @Test
    void testGuardarPartidaNoValidoPartidaIncompleta() {
        Partida partidaIncompleta = new Partida(ID_PARTIDA, FECHA_PARTIDA, JUGADOR1, JUGADOR2);
        // No llamamos a establecerResultados, por lo que isCompleta() es false.
        assertThrows(IllegalStateException.class, () -> servicioPartida.guardarPartida(partidaIncompleta));
    }

    @Tag("Cobertura")
    @Test
    void testGuardarPartidaNoValidoYaGuardada() {
        servicioPartida.guardarPartida(partidaCompleta);
        assertThrows(IllegalStateException.class, () -> servicioPartida.guardarPartida(partidaCompleta));
    }

    @Test
    void testActualizarPartidaValidoCambioFecha() {
        repositorioPartida.guardar(partidaCompleta);
        Partida partidaGuardada = servicioPartida.obtenerPartida(ID_PARTIDA);

        LocalDate fechaAct = LocalDate.of(2025, 12, 19);
        partidaGuardada.setFecha(fechaAct);
        servicioPartida.actualizarPartida(partidaGuardada);
        Partida actualizada = servicioPartida.obtenerPartida(ID_PARTIDA);
        
        assertEquals(ID_PARTIDA, actualizada.getId());
        assertEquals(fechaAct, actualizada.getFecha());
    }

    @Test
    void testActualizarPartidaNoValidoNoEstaGuardada() {
        assertThrows(IllegalStateException.class, () -> servicioPartida.actualizarPartida(partidaCompleta));
    }

    @Test
    void testActualizarPartidaPartidaNula() {
        assertThrows(IllegalArgumentException.class, () -> servicioPartida.actualizarPartida(null));
    }

    @Tag("Cobertura")
    @Test
    void testActualizarNoValidoPartidaNula(){
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.actualizar(null));
    }

    @Test
    void testEliminarPartidaValidoIdValido() {
        servicioPartida.guardarPartida(partidaCompleta);
        assertEquals(partidaCompleta, servicioPartida.obtenerPartida(ID_PARTIDA));
        servicioPartida.eliminarPartida(ID_PARTIDA);

        assertThrows(IllegalStateException.class, () -> servicioPartida.obtenerPartida(ID_PARTIDA));

    }

    @Test
    void testEliminarPartidaNoValidoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> servicioPartida.eliminarPartida(null));    }

    @Test
    void testEliminarPartidaNoValidoIdVacio() {
        assertThrows(IllegalArgumentException.class, () -> servicioPartida.eliminarPartida(""));
    }

    @Test
    void testEliminarPartidaNoValidoNoExiste() {
        assertThrows(IllegalStateException.class, () -> servicioPartida.eliminarPartida(ID_PARTIDA));
    }

    @Test
    void testEliminarPartidaNoValidoYaEliminada() {
        repositorioPartida.guardar(partidaCompleta);
        repositorioPartida.eliminar(ID_PARTIDA);

        assertThrows(IllegalStateException.class, () -> servicioPartida.eliminarPartida(ID_PARTIDA));
    }

    @Tag("Cobertura")
    @Test
    void testEliminarNoValidoIdNulo(){
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.eliminar(null));
    }

    @Tag("Cobertura")
    @Test
    void testEliminarNoValidoIdVacio() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.eliminar(""));
    }

    @Tag("Cobertura")
    @Test
    void testObtenerPartidaPorFechaNoValidoFechaInicialNula() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.obtenerPartidasPorFecha(null,"2025-12-31"));
    }

    @Tag("Cobertura")
    @Test
    void testObtenerPartidaPorFechaNoValidoFechaFinalNula() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.obtenerPartidasPorFecha("2025-12-31", null));
    }

    @Tag("Cobertura")
    @Test
    void testObtenerPartidaPorFechaNoValidoFechaInicialVacia() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.obtenerPartidasPorFecha("","2025-12-31"));
    }

    @Tag("Cobertura")
    @Test
    void testObtenerPartidaPorFechaNoValidoFechaFinalVacia() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.obtenerPartidasPorFecha("2025-12-31", ""));
    }

    @Tag("Cobertura")
    @Test
    void testObtenerPartidaPorFechaNoValidoFormatoIncorrecto() {
        assertThrows(Exception.class, () -> repositorioPartida.obtenerPartidasPorFecha("31-12-2025", "2025-12-31"));
    }

    @Tag("Cobertura")
    @Test
    void testObtenerPartidasPorJugadorNoValidoNombreNulo() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.obtenerPartidasPorJugador(null));
    }

    @Tag("Cobertura")
    @Test
    void testObtenerPartidasPorJugadorNoValidoNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> repositorioPartida.obtenerPartidasPorJugador(""));
    }

    @Test
    void testNoValidoResumenPartidaIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servicioPartida.resumenPartida(null));
    }

    @Test
    void testNoValidoResumenPartidaIdVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> servicioPartida.resumenPartida(""));
    }

}