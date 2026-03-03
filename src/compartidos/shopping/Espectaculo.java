package compartidos.shopping;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Espectaculo {

    private final Lock lock = new ReentrantLock();
    // Condición para agrupar de a 5
    private final Condition grupoCompletoCond = lock.newCondition();
    // Condición para esperar a que el show termine
    private final Condition showTerminadoCond = lock.newCondition();
    // Condición para que los asistentes sepan que las puertas cerraron
    private final Condition asistentesListos = lock.newCondition();

    private static final int CAPACIDAD_TOTAL = 20;
    private static final int TAMAÑO_GRUPO = 5;

    private int visitantesEntrados;
    private int esperandoGrupo; // Contador de visitantes esperando para formar 5
    
    private boolean hayEspectaculo;
    private boolean puertasCerradas;
    private boolean espectaculoEnCurso;

    public Espectaculo() {
        this.visitantesEntrados = 0;
        this.esperandoGrupo = 0;
        this.hayEspectaculo = false;
        this.puertasCerradas = false;
        this.espectaculoEnCurso = false;
    }

    public void iniciarEspectaculo() {
        lock.lock();
        try {
            hayEspectaculo = true;
            puertasCerradas = false;
            espectaculoEnCurso = false;
            visitantesEntrados = 0;
            esperandoGrupo = 0;
            System.out.println("[ESPECTACULO] ¡NUEVO ESPECTÁCULO PREPARÁNDOSE! Las puertas se abren.");
        } finally {
            lock.unlock();
        }
    }

    public void empezarEspectaculo() {
        lock.lock();
        try {
            puertasCerradas = true;
            System.out.println("[ESPECTACULO] Hora de la función. Se cierran las puertas del teatro.");
            
            // Si quedaron visitantes esperando que no llegaron a formar un grupo de 5, 
            // los despertamos para que vean que las puertas cerraron y se vayan.
            grupoCompletoCond.signalAll();
            
            // Avisamos a los asistentes que ya pueden entrar e iniciar el show
            asistentesListos.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void entrarVisitante(int idVisitante) throws InterruptedException {
        lock.lock();
        boolean viaja = false; 
        boolean puedeIntentar = true; 

        try {
            // 1. Verificamos si las condiciones iniciales permiten el ingreso
            if (!hayEspectaculo || puertasCerradas || espectaculoEnCurso || visitantesEntrados >= CAPACIDAD_TOTAL) {
                puedeIntentar = false;
                System.out.println("[ESPECTACULO] Visitante " + idVisitante + " no puede entrar al espectaculo. Se va a deambular.");
            }

            // 2. Intentamos formar el grupo
            if (puedeIntentar) {
                esperandoGrupo++;
                System.out.println("[ESPECTACULO] Visitante " + idVisitante + " en sala de espera. (" + esperandoGrupo + "/" + TAMAÑO_GRUPO + ")");

                if (esperandoGrupo == TAMAÑO_GRUPO) {
                    // Soy el 5to integrante, se formó el grupo
                    visitantesEntrados += TAMAÑO_GRUPO;
                    esperandoGrupo = 0; // Reiniciamos el contador para los próximos 5
                    System.out.println("[ESPECTACULO] ¡Grupo completo! Entran 5 al teatro. Total sentados: " + visitantesEntrados + "/" + CAPACIDAD_TOTAL);
                    
                    if (visitantesEntrados >= CAPACIDAD_TOTAL) {
                        puertasCerradas = true;
                        System.out.println("[ESPECTACULO] CAPACIDAD MÁXIMA ALCANZADA. Se cierran las puertas.");
                    }
                    
                    // Despierto a los 4 que llegaron antes que yo
                    grupoCompletoCond.signalAll(); 
                    viaja = true;
                } else {
                    // No somos 5 todavía, esperamos
                    while (esperandoGrupo > 0 && esperandoGrupo < TAMAÑO_GRUPO && !puertasCerradas) {
                        grupoCompletoCond.await();
                    }

                    // Al despertar, verificamos si nos despertaron porque el grupo se formó, 
                    // o porque el show empezó y no llegamos a ser 5
                    if (puertasCerradas && esperandoGrupo > 0) {
                        esperandoGrupo--; // Me retiro de la cola triste
                        System.out.println("[ESPECTACULO] Visitante " + idVisitante + " se retira, no se completó su grupo a tiempo.");
                    } else {
                        // El grupo sí se formó con éxito
                        viaja = true;
                    }
                }
            }

            // 3. Fase de ver el show (solo si logró formar grupo y entrar)
            if (viaja) {
                // El hilo se queda bloqueado aquí disfrutando del espectáculo
                // hasta que finEspectaculo() cambie el flag y haga signalAll()
                while (hayEspectaculo) {
                    showTerminadoCond.await();
                }
            }

        } finally {
            System.out.println("[VISITANTE] El visitante N°" + idVisitante + " salió del espectáculo");
            lock.unlock();
        }
    }

    public void asistenteEsperaVisitantes() throws InterruptedException {
        lock.lock();
        try {
            // El asistente espera a que sea la hora (puertas cerradas)
            while (!puertasCerradas) {
                asistentesListos.await();
            }
            System.out.println("[ESPECTACULO] Asistentes pueden ingresar. Hay " + visitantesEntrados + " visitantes acomodados.");
        } finally {
            lock.unlock();
        }
    }

    public void entrarAsistentes() {
        lock.lock();
        try {
            espectaculoEnCurso = true;
            System.out.println("[ESPECTACULO] Asistentes en posición. Preparando el show...");
        } finally {
            lock.unlock();
        }
    }

    public void simularEspectaculo() throws InterruptedException {
        lock.lock();
        try {
            System.out.println("[ESPECTACULO] EL SHOW ESTÁ EN CURSO...");
            // El asistente también espera a que la Hora marque el fin del espectáculo
            while (hayEspectaculo) {
                showTerminadoCond.await();
            }
            System.out.println("[ESPECTACULO] Asistente termina su turno y se retira.");
        } finally {
            lock.unlock();
        }
    }

    public void finEspectaculo() {
        lock.lock();
        try {
            hayEspectaculo = false;
            espectaculoEnCurso = false;
            System.out.println("[ESPECTACULO] FIN DEL ESPECTÁCULO. Se abren salidas.");
            
            // Este es el momento mágico que despierta a todo el mundo 
            // (visitantes dentro del show y asistentes simulando)
            showTerminadoCond.signalAll(); 
        } finally {
            lock.unlock();
        }
    }

    public boolean hayEspectaculoDisponible() {
        boolean disponible = false;
        lock.lock();
        try {
            if (hayEspectaculo && !espectaculoEnCurso) {
                disponible = true;
            }
        } finally {
            lock.unlock();
        }
        return disponible;
    }
}