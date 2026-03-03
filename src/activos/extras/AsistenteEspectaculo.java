package activos.extras;

import compartidos.Parque;

public class AsistenteEspectaculo extends Thread {
    
    private final Parque parque;
    private final int id;
    
    public AsistenteEspectaculo(int id, Parque parque) {
        this.id = id;
        this.parque = parque;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                // Espera a que haya un espectáculo disponible
                System.out.println("[ASISTENTE_ESPECTACULO] Asistente " + id + " está durmiendo, esperando próximo espectáculo...");
                
                // Espera hasta las próximas horas de espectáculo (9, 12, 15, 18, 21)
                parque.esperarProximoEspectaculo();
                
                System.out.println("[ASISTENTE_ESPECTACULO] Asistente " + id + " se despertó. ¡Hay espectáculo!");
                
                // Los asistentes esperan a que los visitantes estén listos
                parque.asistenteEsperaVisitantes();
                
                // Entran al espectáculo
                System.out.println("[ASISTENTE_ESPECTACULO] Asistente " + id + " está ENTRANDO al espectáculo...");
                parque.entrarAsistentesEspectaculo(id);
                
                // Simulan el espectáculo (1 hora)
                System.out.println("[ASISTENTE_ESPECTACULO] Asistente " + id + " está presentando el espectáculo...");
                parque.simularEspectaculo();
                
                // El espectáculo terminó, el asistente vuelve a dormir
                System.out.println("[ASISTENTE_ESPECTACULO] Asistente " + id + " salió del espectáculo. Se volverá a dormir.");
            }
        } catch (InterruptedException e) {
            System.out.println("[ASISTENTE_ESPECTACULO] Asistente " + id + " fue interrumpido");
        }
    }
}
