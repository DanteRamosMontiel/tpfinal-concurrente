package activos.simulaciones;

import compartidos.Parque;

public class simulacionMontania extends Thread{
    private Parque parque;

    public simulacionMontania(Parque xParque){
        this.parque = xParque;
    }

    public void run(){
        while(true){
            try{
               parque.iniciarViajeMontania();;
               Thread.sleep(7000);
               Thread.sleep(2000);
               parque.terminarViajeMontania();
            }catch(InterruptedException e){}
        }
    }
}