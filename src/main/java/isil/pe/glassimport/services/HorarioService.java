package isil.pe.glassimport.services;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isil.pe.glassimport.entity.Horario;
import isil.pe.glassimport.repository.HorarioRepository;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository repo;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final List<String> HORAS = Arrays.asList("09:00", "10:30", "12:00", "15:00", "17:00");

    private final DateTimeFormatter HORA_IN = DateTimeFormatter.ofPattern("H:mm");
    private final DateTimeFormatter HORA_OUT = DateTimeFormatter.ofPattern("HH:mm");

    private String normalizarHora(String horaCruda) {
        LocalTime t = LocalTime.parse(horaCruda.trim(), HORA_IN);
        return t.format(HORA_OUT);
    }

    public Map<String, String> getEstadosHorarios(String fecha) {
        Map<String, String> result = new HashMap<>();
        for (String hora : HORAS) {
            String estado = repo.findByFechaAndHora(fecha, hora)
                    .map(Horario::getEstado)
                    .orElse("LIBRE");
            result.put(hora, estado);
        }
        return result;
    }

    public boolean bloquearHorario(String fecha, String horaCruda) {
        String hora = normalizarHora(horaCruda);
        System.out.println("LOCK REQUEST => fecha=" + fecha + ", hora=" + hora);

        Optional<Horario> hOpt = repo.findByFechaAndHora(fecha, hora);

        if (hOpt.isPresent() && !"LIBRE".equals(hOpt.get().getEstado())) {
            System.out.println("NO SE PUEDE BLOQUEAR, YA NO ESTÁ LIBRE");
            return false;
        }

        Horario h = hOpt.orElse(Horario.builder().build());
        h.setFecha(fecha);
        h.setHora(hora);
        h.setEstado("EN_PROCESO");
        repo.save(h);
        System.out.println("GUARDADO EN_PROCESO => " + h.getFecha() + " " + h.getHora());

        scheduler.schedule(() -> {
            repo.findByFechaAndHora(fecha, hora).ifPresent(hh -> {
                if ("EN_PROCESO".equals(hh.getEstado())) {
                    hh.setEstado("LIBRE");
                    repo.save(hh);
                    System.out.println("AUTO-LIBERADO => " + hh.getFecha() + " " + hh.getHora());
                }
            });
        }, 3, TimeUnit.MINUTES);

        return true;
    }

    public void reservarHorario(String fecha, String horaCruda) {
        String hora = normalizarHora(horaCruda);
        System.out.println("RESERVA REQUEST => fecha=" + fecha + ", hora=" + hora);

        Horario h = repo.findByFechaAndHora(fecha, hora)
                .orElse(Horario.builder().build());
        h.setFecha(fecha);
        h.setHora(hora);
        h.setEstado("OCUPADO");
        repo.save(h);
        System.out.println("RESERVADO => " + h.getFecha() + " " + h.getHora());
    }
}
