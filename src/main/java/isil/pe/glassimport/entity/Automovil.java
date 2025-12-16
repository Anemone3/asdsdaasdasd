package isil.pe.glassimport.entity;

import java.util.List;

import isil.pe.glassimport.dto.response.AutomovilResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "automoviles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Automovil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marca;
    private String modelo;
    private int anio;
    private String placa;
    private String nota;

    // ✅ User es OPCIONAL (nullable = true)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @OneToMany(mappedBy = "automovil", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas;

    public static AutomovilResponseDto toDto(Automovil automovil) {
        return AutomovilResponseDto.builder()
                .id(automovil.getId())
                .marca(automovil.getMarca())
                .modelo(automovil.getModelo())
                .anio(automovil.getAnio())
                .placa(automovil.getPlaca())
                .nota(automovil.getNota())
                .build();
    }

    public static Automovil toEntity(AutomovilResponseDto automovilResponseDto) {
        return Automovil.builder()
                .id(automovilResponseDto.getId())
                .marca(automovilResponseDto.getMarca())
                .modelo(automovilResponseDto.getModelo())
                .anio(automovilResponseDto.getAnio())
                .placa(automovilResponseDto.getPlaca())
                .nota(automovilResponseDto.getNota())
                .build();
    }
}
