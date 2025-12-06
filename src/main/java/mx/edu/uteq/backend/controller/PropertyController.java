package mx.edu.uteq.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mx.edu.uteq.backend.dto.PropertyDTO;
import mx.edu.uteq.backend.service.JwtService;
import mx.edu.uteq.backend.service.PropertyService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private JwtService jwtService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor(true));
    }

    @GetMapping
    public List<PropertyDTO> getProperties() {
        return propertyService.getProperties();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<PropertyDTO> opt = propertyService.getPropertyById(id);

            if (opt.isEmpty()) { 
                return ResponseEntity.notFound().build();
            }

            PropertyDTO property = opt.get();
            boolean isOwner = false;
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                
                try {
                    Long currentUserId = jwtService.getCurrentUserId();
                    
                    if (property.getOwnerId() != null && currentUserId != null && currentUserId.equals(property.getOwnerId())) {
                        isOwner = true;
                    }
                } catch (Exception e) {
                    isOwner = false;
                }
            }

            property.setOwner(isOwner);

            return ResponseEntity.ok(property);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerProperty(@RequestBody PropertyDTO propertyRegister) {
        try {
            Long ownerId = jwtService.getCurrentUserId();

            propertyRegister.setOwnerId(ownerId);

            propertyService.registerProperty(propertyRegister);
            return ResponseEntity.status(HttpStatus.CREATED).body("Propiedad registrada con exito");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No se pudo obtener el usuario autenticado" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al intentar registrar propiedad");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProperty(@PathVariable Long id) {
        try {
            propertyService.deleteProperty(id);
            return ResponseEntity.ok("Propiedad eliminada con exito");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al intentar eliminar propiedad");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProperty(@PathVariable Long id, @RequestBody PropertyDTO dto) {
        try {
            PropertyDTO updatedProperty = propertyService.updateProperty(id, dto);
            return ResponseEntity.ok(updatedProperty);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al intentar actualizar propiedad");
        }
    }

    // Búsqueda avanzada por características y verificación de disponibilidad
    @GetMapping("/search")
    public ResponseEntity<List<PropertyDTO>> searchProperties(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean kidsAllowed,
            @RequestParam(required = false) Boolean petsAllowed,
            @RequestParam(required = false) Integer numberOfGuests,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            List<PropertyDTO> results = propertyService.searchProperties(type, kidsAllowed, petsAllowed,
                    numberOfGuests, maxPrice, startDate, endDate);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/myproperties")
    public ResponseEntity<List<PropertyDTO>> getPropertyByOwnerId() {
        try {

            Long ownerId = jwtService.getCurrentUserId();

            List<PropertyDTO> properties = propertyService.getPropertiesByOwner(ownerId);
            if (properties.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(properties);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
