package quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DisparaJobws implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            //llamada a la clase que implementa la tarea a ejecutar.
            DisparaGeneratorws.generator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
