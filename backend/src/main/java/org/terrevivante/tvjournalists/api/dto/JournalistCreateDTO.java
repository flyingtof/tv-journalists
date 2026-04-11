package org.terrevivante.tvjournalists.api.dto;

import jakarta.validation.constraints.NotBlank;

public class JournalistCreateDTO {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String globalEmail;
    private String globalPhone;

    public JournalistCreateDTO() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGlobalEmail() { return globalEmail; }
    public void setGlobalEmail(String globalEmail) { this.globalEmail = globalEmail; }
    public String getGlobalPhone() { return globalPhone; }
    public void setGlobalPhone(String globalPhone) { this.globalPhone = globalPhone; }
}
