package mx.edu.uteq.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import mx.edu.uteq.backend.dto.PropertyDTO;
import mx.edu.uteq.backend.model.Property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import mx.edu.uteq.backend.repository.PropertyRepository;
import mx.edu.uteq.backend.repository.UserRepository;
import mx.edu.uteq.backend.repository.BookingRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Service
public class PropertyService {
    
    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired 
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<PropertyDTO> getProperties() {
        return propertyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca propiedades aplicando filtros sencillos por características. Si se proporcionan
     * startDate y endDate, para cada propiedad se evalúa si existe alguna
     * reserva que solape con ese período y se marca el campo 'available' en el DTO.
     */
    public List<PropertyDTO> searchProperties(String type, Boolean kidsAllowed, Boolean petsAllowed,
            Integer numberOfGuests, Double maxPrice, String startDateStr, String endDateStr) {

        Date startDate = null;
        Date endDate = null;
        if (startDateStr != null && endDateStr != null) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                startDate = fmt.parse(startDateStr);
                endDate = fmt.parse(endDateStr);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Fechas deben estar en formato dd-MM-yyyy");
            }
        }
        final Date fStart = startDate;
        final Date fEnd = endDate;

        return propertyRepository.findAll().stream()
                .filter(p -> type == null || (p.getType() != null && p.getType().equalsIgnoreCase(type)))
                .filter(p -> kidsAllowed == null || (p.getKidsAllowed() != null && p.getKidsAllowed().equals(kidsAllowed)))
                .filter(p -> petsAllowed == null || (p.getPetsAllowed() != null && p.getPetsAllowed().equals(petsAllowed)))
                .filter(p -> numberOfGuests == null || (p.getNumberOfGuests() != null && p.getNumberOfGuests() >= numberOfGuests))
                .filter(p -> maxPrice == null || (p.getPricePerNight() != null && p.getPricePerNight() <= maxPrice))
                .map(p -> {
                    PropertyDTO dto = convertToDto(p);
                    if (fStart != null && fEnd != null) {
                        boolean hasOverlap = bookingRepository
                                .existsByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(p.getId(), fEnd,
                                        fStart);
                        dto.setAvailable(!hasOverlap);
                    } else {
                        dto.setAvailable(null); // disponibilidad no consultada
                    }
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<PropertyDTO> getPropertyById(Long id){
        return propertyRepository.findById(id)
            .map(this::convertToDto);
    }

    private PropertyDTO convertToDto(Property property){
        PropertyDTO dto = new PropertyDTO();
        dto.setId(property.getId());
        dto.setOwnerId(property.getOwnerId() != null ? property.getOwnerId().getId() : null);
        dto.setName(property.getName());
        dto.setPricePerNight(property.getPricePerNight());
        dto.setLocation(property.getLocation());
        dto.setType(property.getType());
        dto.setKidsAllowed(property.getKidsAllowed());
        dto.setPetsAllowed(property.getPetsAllowed());
        dto.setNumberOfGuests(property.getNumberOfGuests());
        dto.setShowProperty(property.getShowProperty());
        dto.setDescription(property.getDescription());
        dto.setImagen(property.getImagen());
        return dto;
    }

    private Property convertoToEntity(PropertyDTO dto){
        Property property = new Property();
        property.setId(dto.getId());
        if (dto.getOwnerId() != null) {
            userRepository.findById(dto.getOwnerId())
            .ifPresent(property::setOwnerId);
        }
        property.setName(dto.getName());
        property.setPricePerNight(dto.getPricePerNight());
        property.setLocation(dto.getLocation());
        property.setType(dto.getType());
        property.setKidsAllowed(dto.getKidsAllowed());
        property.setPetsAllowed(dto.getPetsAllowed());
        property.setNumberOfGuests(dto.getNumberOfGuests());
        property.setShowProperty(dto.getShowProperty());
        property.setDescription(dto.getDescription());
        property.setImagen(dto.getImagen());
        return property;
    }

    @Transactional
    public void registerProperty(PropertyDTO dto){
        propertyRepository.save(convertoToEntity(dto));
    }

    @Transactional
    public void deleteProperty(Long id){
        Property property = propertyRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Propiedad no encontrada con el ID: " + id));
        propertyRepository.delete(property);
    }

    @Transactional
    public PropertyDTO updateProperty(Long id, PropertyDTO dto){
        Property property = propertyRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Propiedad no encontrada con ID: " + id));
        
        property.setName(dto.getName());
        property.setPricePerNight(dto.getPricePerNight());
        property.setLocation(dto.getLocation());
        property.setType(dto.getType());
        property.setKidsAllowed(dto.getKidsAllowed());
        property.setPetsAllowed(dto.getPetsAllowed());
        property.setNumberOfGuests(dto.getNumberOfGuests());
        property.setShowProperty(dto.getShowProperty());
        property.setDescription(dto.getDescription());
        property.setImagen(dto.getImagen());

        if (dto.getOwnerId() != null) {
            userRepository.findById(dto.getOwnerId())
            .ifPresent(property::setOwnerId);
        }

        Property updatedProperty = propertyRepository.save(property);

        return convertToDto(updatedProperty);
    }

    public List<PropertyDTO> getPropertiesByOwner(Long ownerId){
        return propertyRepository.findByOwnerId_Id(ownerId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
}
