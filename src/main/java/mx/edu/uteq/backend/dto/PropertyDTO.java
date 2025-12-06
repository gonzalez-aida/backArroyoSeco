package mx.edu.uteq.backend.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyDTO {
    private Long id;
    private Long ownerId;
    private String name;
    private Double pricePerNight;
    private Map<String, Object> location;
    private String type;
    private Boolean kidsAllowed;
    private Boolean petsAllowed;
    private Integer numberOfGuests;
    private Boolean showProperty;
    private Map<String, Object> description;
    private List<String> imagen;
    private Boolean available;
    private boolean isOwner;
}
