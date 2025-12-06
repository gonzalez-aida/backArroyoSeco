package mx.edu.uteq.backend.dto;

public class RegisterRequestDTO {

    private String email;
    private String password;
    private String role;

    // Datos del perfil directamente aquí
    private String name;
    private String lastName;
    private String cellphone;
    private String country;

    // Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCellphone() { return cellphone; }
    public void setCellphone(String cellphone) { this.cellphone = cellphone; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
