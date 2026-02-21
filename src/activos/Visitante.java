package activos;

import compartidos.Parque;

public class Visitante extends Thread {

    private final Parque parque;
    private final int id;

    public Visitante(int xId, Parque xParque) {
        this.parque = xParque;
        this.id = xId;
    }

    public void run() {
        try {
            while(true){
                int m = parque.tomarMolinete();
                if(m!=-1){
                    System.out.println("El visitante N°"+id+" tomó el molinete "+m);
                    Thread.sleep(2000);
                    System.out.println("El visitante N°"+id+" solto el molinete "+m);
                    parque.dejarMolinete(m);
                }else{
                    System.out.println("PARQUE CERRADO");
                    Thread.sleep(10000);
                }
               
            }
        } catch (InterruptedException e) {
        }
    }
}