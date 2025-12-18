package isil.pe.glassimport.services;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isil.pe.glassimport.entity.Horario;
import isil.pe.glassimport.entity.HorarioFijo;
import isil.pe.glassimport.repository.HorarioRepository;
import isil.pe.glassimport.repository.HorarioFijoRepository;
import isil.pe.glassimport.repository.ReservaRepository;

@Service
public class HorarioService {
    //ga

    @Autowired
    private HorarioRepository repo;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HorarioFijoRepository horarioFijoRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final DateTimeFormatter HORA_IN = DateTimeFormatter.ofPattern("H:mm");
    private final DateTimeFormatter HORA_OUT = DateTimeFormatter.ofPattern("HH:mm");

    private String normalizarHora(String horaCruda) {
        LocalTime t = LocalTime.parse(horaCruda.trim(), HORA_IN);
        return t.format(HORA_OUT);
    }

    private List<String> obtenerHorasHabilitadas() {
        return horarioFijoRepository.findAll()
                .stream()
                .filter(h -> Boolean.TRUE.equals(h.getHabilitado()))
                .map(HorarioFijo::getHora)
                .collect(Collectors.toList());
    }


    public Map<String, String> getEstadosHorarios(String fecha) {
        List<String> horasHabilitadas = obtenerHorasHabilitadas();
        Map<String, String> result = new HashMap<>();

        for (String hora : horasHabilitadas) {
            Optional<Horario> horarioOpt = repo.findByFechaAndHora(fecha, hora);

            if (horarioOpt.isPresent()) {
                Horario h = horarioOpt.get();

                if (reservaRepository.existsReservaActivaByHorarioId(h.getId())) {
                    result.put(hora, "OCUPADO");
                } else {
                    result.put(hora, h.getEstado());
                }
            } else {
                result.put(hora, "LIBRE");
            }
        }
        return result;
    }

    public boolean bloquearHorario(String fecha, String horaCruda) {
        String hora = normalizarHora(horaCruda);
        System.out.println("LOCK REQUEST => fecha=" + fecha + ", hora=" + hora);

        List<String> horasHabilitadas = obtenerHorasHabilitadas();
        if (!horasHabilitadas.contains(hora)) {
            System.out.println("NO SE PUEDE BLOQUEAR, HORA NO HABILITADA");
            return false;
        }

        Optional<Horario> hOpt = repo.findByFechaAndHora(fecha, hora);

        if (hOpt.isPresent()) {
            Horario h = hOpt.get();

            if (reservaRepository.existsReservaActivaByHorarioId(h.getId())) {
                System.out.println("NO SE PUEDE BLOQUEAR, YA TIENE RESERVA ACTIVA");
                return false;
            }

            if (!"LIBRE".equals(h.getEstado())) {
                System.out.println("NO SE PUEDE BLOQUEAR, YA NO ESTÁ LIBRE");
                return false;
            }
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
