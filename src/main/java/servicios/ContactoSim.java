package servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;

import org.springframework.stereotype.Service;

import interfaces.InterfazContactoSim;
import modelo.DatosSimulation;
import modelo.DatosSolicitud;
import modelo.Entidad;
import modelo.Punto;
import org.springframework.web.client.RestTemplate;

@Service
public class ContactoSim implements InterfazContactoSim {

    private final List<Entidad> entities;
    private final Map<Integer, DatosSolicitud> solicitudes;
    private final Random random;
    private final Logger logger;


    public ContactoSim(Logger logger) {
        this.entities = new ArrayList<>();
        this.solicitudes = new HashMap<>();
        this.random = new Random();
        this.logger = logger;
        inicializarEntidades();
    }

    private void inicializarEntidades() {
        Entidad e1 = new Entidad();
        e1.setId(1);
        e1.setName("Parámetro 1");
        e1.setDescripcion("Controlan la temperatura ambiental.");
        entities.add(e1);

        Entidad e2 = new Entidad();
        e2.setId(2);
        e2.setName("Parámetro 2");
        e2.setDescripcion("Miden el nivel de humedad en el aire.");
        entities.add(e2);

        Entidad e3 = new Entidad();
        e3.setId(3);
        e3.setName("Parámetro 3");
        e3.setDescripcion("Sistemas de vigilancia por video.");
        entities.add(e3);
    }

    @Override
    public int solicitarSimulation(DatosSolicitud sol) {
        int token = 1000 + random.nextInt(9000);
        solicitudes.put(token, sol);
        logger.info("Simulation requested, token generated: {}", token);
        return token;
    }

    @Override
    public DatosSimulation descargarDatos(int ticket) {
        // 1. Validar si el ticket existe en nuestras solicitudes locales
        DatosSolicitud solicitud = solicitudes.get(ticket);
        if (solicitud == null) {
            logger.warn("Se intentó descargar datos para un ticket inexistente: {}", ticket);
            return new DatosSimulation(); // Devolvemos objeto vacío para evitar NullPointerException
        }

        try {
            /* * NOTA PARA TU TFG: Aquí deberías hacer la llamada a la MÁQUINA VIRTUAL externa.
             * NO uses localhost:8080, usa la IP de la VM.
             * Si por ahora solo tienes datos simulados, usa la lógica de abajo:
             */

            DatosSimulation sim = new DatosSimulation();

            // Simulamos el ancho del tablero basado en la solicitud original
            int ancho = 10; // Valor por defecto o sacado de 'solicitud'
            sim.setAnchoTablero(ancho);

            Map<Integer, List<Punto>> puntosMap = new HashMap<>();
            int maxT = 0;

            // Generamos datos de prueba para que veas algo en el /grid
            for (int t = 0; t < 10; t++) { // 10 pasos de tiempo
                List<Punto> puntosEnT = new ArrayList<>();

                // Creamos 3 puntos aleatorios por cada instante de tiempo
                for (int i = 0; i < 3; i++) {
                    Punto p = new Punto();
                    p.setX(random.nextInt(ancho));
                    p.setY(random.nextInt(ancho));
                    // Asignamos un color según el tiempo para ver cambios
                    p.setColor(t % 2 == 0 ? "#FF0000" : "#0000FF");
                    puntosEnT.add(p);
                }
                puntosMap.put(t, puntosEnT);
                maxT = t;
            }

            sim.setPuntos(puntosMap);
            sim.setMaxSegundos(maxT + 1);

            logger.info("Datos de simulación procesados localmente para el ticket {}", ticket);
            return sim;

        } catch (Exception e) {
            logger.error("Error crítico al procesar la simulación {}: {}", ticket, e.getMessage());
            return new DatosSimulation();
        }
    }

    @Override
    public List<Entidad> getEntities() {
        return entities;
    }

    @Override
    public boolean isValidEntityId(int id) {
        return entities.stream().anyMatch(e -> e.getId() == id);
    }

    @Override
    public String obtenerGrid(String tok) {
        DatosSimulation ds = this.descargarDatos(Integer.parseInt(tok));
        return "Datos descargados para token: " + tok;
    }


}
