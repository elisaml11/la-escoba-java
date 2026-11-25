package uva.tds.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.io.InputStream;
import java.sql.Connection;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import uva.tds.entidades.Partida;
import uva.tds.entidades.ResumenPartida;
import uva.tds.interfaces.RepositorioPartida;
import uva.tds.entidades.Jugador;

/**
 * Tests para la clase ServicioPartida.
 * 
 * @author Marta Pérez Alonso
 * @author Elisa Martínez Lafuente
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServicioPartidaConDBUnitTest {

    private RepositorioPartida repositorioPartida;
    private ServicioPartida servicioPartida;

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

            Connection jdbcConnection = session.doReturningWork(connection -> connection);
            IDatabaseConnection dbUnitConnection = new DatabaseConnection(jdbcConnection);

            // Cargar el dataset desde el archivo XML
            InputStream dataSetStream = getClass().getClassLoader().getResourceAsStream("dataset-partidas.xml");
            IDataSet dataSet = new FlatXmlDataSetBuilder().build(dataSetStream);

            // Limpiar las tablas y luego insertar los datos
            DatabaseOperation.CLEAN_INSERT.execute(dbUnitConnection, dataSet);

            session.getTransaction().commit();
            dbUnitConnection.close();

        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }

        servicioPartida = new ServicioPartida(repositorioPartida);

    }

    @AfterAll
    void tearDown() {
        HibernateUtil.shutdown();
    }

    @Test
    void testObtenerPartidaValidoIdValido() {
        Partida resultado = servicioPartida.obtenerPartida("p-db-1");
        ResumenPartida resumen = resultado.getResumenPartida();

        assertNotNull(resultado);
        assertEquals("p-db-1", resultado.getId());
        assertEquals(LocalDate.of(2025, 11, 25), resultado.getFecha());
        assertEquals("Alice", resultado.getJugador1());
        assertEquals("Bob", resultado.getJugador2());

        assertNotNull(resumen);
        assertEquals(0, resumen.getPuntosJugador1());
        assertEquals(5, resumen.getPuntosJugador2());
        assertTrue(resumen.isGuindisJugador2());
    }

    @Test
    void testObtenerPartidaNoValidoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> servicioPartida.obtenerPartida(null));
    }

    @Test
    void testObtenerPartidaNoValidoNoEncontrada() {
        assertThrows(IllegalStateException.class, () -> servicioPartida.obtenerPartida("p-pd-99"));
    }

    @Test
    void testObtenerEstadisticasEntreFechasValido() {
        LocalDate fechaInicio = LocalDate.of(2025, 11, 1);
        LocalDate fechaFin = LocalDate.of(2025, 11, 30);
        ArrayList<Integer> resultado = servicioPartida.obtenerEstadisticasEntreFechas(fechaInicio, fechaFin);

        assertEquals(2, resultado.size());
        assertEquals(6, resultado.get(0));
        assertEquals(11, resultado.get(1));
    }

    @Tag("Cobertura")
    @Test
    void testObtenerEstadisticasEntreFechasValidoSinPartidas() {
        LocalDate fechaInicio = LocalDate.of(2025, 1, 1);
        LocalDate fechaFin = LocalDate.of(2025, 8, 31);

        ArrayList<Integer> resultado = servicioPartida.obtenerEstadisticasEntreFechas(fechaInicio, fechaFin);

        // Debe devolver [0, 0] (0 partidas, 0 jugadores)
        assertEquals(2, resultado.size());
        assertEquals(0, resultado.get(0));
        assertEquals(0, resultado.get(1));
    }

    @Test
    void testNoValidoObtenerEstadisticasEntreFechasFechaInicioNula() {
        LocalDate fechaFin = LocalDate.of(2025, 12, 31);
        assertThrows(IllegalArgumentException.class,
                () -> servicioPartida.obtenerEstadisticasEntreFechas(null, fechaFin));
    }

    @Test
    void testNoValidoObtenerEstadisticasEntreFechasFechaFinNula() {
        LocalDate fechaInicio = LocalDate.of(2025, 1, 1);

        assertThrows(IllegalArgumentException.class,
                () -> servicioPartida.obtenerEstadisticasEntreFechas(fechaInicio, null));
    }

    @Test
    void testNoValidoObtenerEstadisticasEntreFechasFechaInicioPosteriorFechaFin() {
        LocalDate fechaInicio = LocalDate.of(2025, 12, 31);
        LocalDate fechaFin = LocalDate.of(2025, 1, 1);

        assertThrows(IllegalArgumentException.class,
                () -> servicioPartida.obtenerEstadisticasEntreFechas(fechaInicio, fechaFin));
    }

    @Test
    void testValidoObtenerEstadisticasJugador() {
        ArrayList<Integer> resultado = servicioPartida.obtenerEstadisticasJugador("Bob");

        assertEquals(4, resultado.size());
        assertEquals(3, resultado.get(0));// ganadas
        assertEquals(0, resultado.get(1));// perdidas
        assertEquals(4, resultado.get(2));// total
        assertEquals(3, resultado.get(3));// guindis
    }

    @Test
    @Tag("Cobertura")
    void testValidoObtenerEstadisticasJugadorSinPartidas() {
        String nombre = "Maria";

        ArrayList<Integer> resultado = servicioPartida.obtenerEstadisticasJugador(nombre);

        assertEquals(4, resultado.size());
        assertEquals(0, resultado.get(0));// ganadas
        assertEquals(0, resultado.get(1));// perdidas
        assertEquals(0, resultado.get(2));// total
        assertEquals(0, resultado.get(3));// guindis
    }

    @Test
    void testNoValidoObtenerEstadisticasJugadorNombreNulo() {
        assertThrows(IllegalArgumentException.class, () -> servicioPartida.obtenerEstadisticasJugador(null));
    }

    @Test
    void testNoValidoObtenerEstadisticasJugadorNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> servicioPartida.obtenerEstadisticasJugador(""));
    }


    @Test
    void testValidoObtenerTop10() {

        ArrayList<Jugador> top10 = servicioPartida.obtenerTop10();

        assertEquals(10, top10.size());
        assertEquals("Bob", top10.get(0).getNombre());
        assertEquals("Frank", top10.get(1).getNombre());
        assertEquals("Ivan", top10.get(2).getNombre());
        assertEquals("Alice", top10.get(3).getNombre());
        assertEquals("Charlie", top10.get(4).getNombre());
        assertEquals("Niaj", top10.get(5).getNombre());
        assertEquals("Dave", top10.get(6).getNombre());
        assertEquals("Eve", top10.get(7).getNombre());
        assertEquals("Grace", top10.get(8).getNombre());
        assertEquals("Heidi", top10.get(9).getNombre());

    }

}