package org.previmer.ichthyop.evol;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 *
 * @author mariem
 */
public class InitialSpawn {

    int nb;
    public static int nb_per_day;
    // static int excess;
    int spawn_frequency = 2;
    public static long last_spawn = SimulationManager.getInstance().getTimeManager().get_tO();

    public InitialSpawn() {
        nb_per_day = spawnPerDay();
        nb = SimulationManager.getInstance().getReleaseManager().getNbParticles();
    }

    public void InitialSpawnSetUp() {
        if (spawnDay(last_spawn)) {
            try {
                SimulationManager.getInstance().init();
            } catch (Exception ex) {
                Logger.getLogger(InitialSpawn.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /* 
         * A compléter correctement plus tard***********************************************
         * excess = verify(nb, nb_per_day, spawn_frequency);
        if (excess != 0) {      // les pontes en trop devraient être réparties aléatoirement
        excess--;
        nb_per_day++;
        }**********************************************************************************/
    }

    private boolean spawnDay(long last_spawn) {
        long current_time = SimulationManager.getInstance().getTimeManager().getTime();
        long x = last_spawn + 172800;
        // ponte tous ls 2 jours = 172800 secondes
        if (current_time == x) {
            last_spawn += 172800;
            return true;
        } else {
            return false;
        }
    }

    private int spawnPerDay() {
        int spawn_per_day;
        //I considered that a year=365 days.
        // x/(365/2) => x* 2/365        
        spawn_per_day = (int) Math.ceil(nb * spawn_frequency / 365);
        return spawn_per_day;
    }

    //x=number of particles     //a=spawn_per_day       //b=frequency_spawn
    private int verify(int x, int a, int b) {
        int c = 0;
        float v = x - a;
        if (x != 0) {       // si v < 1 //différence négligeable => c = 0
            if (v >= 1) {
                c = (int) Math.ceil(v);
            }
        }
        return c;
    }
}