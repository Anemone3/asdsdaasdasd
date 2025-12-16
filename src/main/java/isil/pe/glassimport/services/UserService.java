package isil.pe.glassimport.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import isil.pe.glassimport.dto.request.UserRequestDto;
import isil.pe.glassimport.dto.request.UserPatchDto;
import isil.pe.glassimport.dto.response.UserResponseDto;
import isil.pe.glassimport.entity.User;
import isil.pe.glassimport.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = User.builder()
            .username(userRequestDto.getUsername())
            .password(userRequestDto.getPassword())
            .email(userRequestDto.getEmail())
            .estado(userRequestDto.getEstado())
            .build();
        
        User savedUser = userRepository.save(user);
        return convertToResponseDto(savedUser);
    }
    
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::convertToResponseDto)
            .toList();
    }
    
    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
            .map(this::convertToResponseDto);
    }
    
    public Optional<UserResponseDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(this::convertToResponseDto);
    }
    
    public Optional<UserResponseDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(this::convertToResponseDto);
    }
    
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(userRequestDto.getUsername());
            user.setPassword(userRequestDto.getPassword());
            user.setEmail(userRequestDto.getEmail());
            user.setEstado(userRequestDto.getEstado());
            
            User updatedUser = userRepository.save(user);
            return convertToResponseDto(updatedUser);
        }
        throw new RuntimeException("Usuario no encontrado con id: " + id);
    }
    
    public UserResponseDto patchUser(Long id, UserPatchDto userPatchDto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            if (userPatchDto.getUsername() != null) {
                user.setUsername(userPatchDto.getUsername());
            }
            if (userPatchDto.getPassword() != null) {
                user.setPassword(userPatchDto.getPassword());
            }
            if (userPatchDto.getEmail() != null) {
                user.setEmail(userPatchDto.getEmail());
            }
            if (userPatchDto.getEstado() != null) {
                user.setEstado(userPatchDto.getEstado());
            }
            
            User updatedUser = userRepository.save(user);
            return convertToResponseDto(updatedUser);
        }
        throw new RuntimeException("Usuario no encontrado con id: " + id);
    }
    
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
    }
    
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
    
    public long countUsers() {
        return userRepository.count();
    }
    
    private UserResponseDto convertToResponseDto(User user) {
        return UserResponseDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .estado(user.getEstado())
            .build();
    }
}
